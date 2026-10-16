package com.phongmay.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import org.json.JSONObject;

import com.phongmay.common.JsonUtil;
import com.phongmay.common.MachineInfo;
import com.phongmay.common.MachineStatus;
import com.phongmay.common.Message;
import com.phongmay.common.Response;
import com.phongmay.server.dao.MachineDao;
import com.phongmay.server.dao.UserDao;

public class ClientHandle implements Runnable {
    private final Socket socket;
    private final ClientManager clientManager;
    private PrintWriter writer;
    private final MachineDao machineDao = new MachineDao();
    private final UserDao userDao = new UserDao();
    private String currentMachineId;
    public ClientHandle (Socket socket,ClientManager clientManager){
        this.socket=socket;
        this.clientManager=clientManager;
    }

    @Override
    public void run() {
        String ip = socket.getInetAddress().getHostAddress();
        System.out.println("Đang xử lý client: " + ip);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            String json;
            while ((json = reader.readLine()) != null) {
                System.out.println("JSON nhận được: " + json);
                JSONObject obj=new JSONObject(json);
                if(obj.has("success")){
                    Response response=JsonUtil.jsonToResponse(json);
                    if(currentMachineId!=null){
                        System.out.println("Nhận kết quả từ máy: " + currentMachineId);
                        System.out.println("Response: " + json);
                        clientManager.completeRequest(response);
                        
                    }
                    continue;
                }
                Message message = JsonUtil.jsonToMessage(json);
                System.out.println("Command: " + message.getCommand());
                System.out.println("Target: " + message.getTarget());

                switch (message.getCommand()) {

                    case CONNECT: {
                        MachineInfo info = JsonUtil.jsonToMachineInfo(message.getData());
                        String machineId = info.getMachineId();

                        boolean success = machineDao.connectMachine(
                                machineId,
                                info.getMachineName(),
                                info.getIpAddress(),
                                info.getOs());

                        Response res;
                        if (success) {
                            currentMachineId = machineId;
                            clientManager.addClient(machineId, this);
                            System.out.println("Máy " + machineId + " đã đăng ký");
                            res = new Response(message.getRequestId(), true, "connect thành công", machineId);
                        } else {
                            res = new Response(message.getRequestId(), false, "Không thể đăng ký máy", null);
                        }
                        writer.println(JsonUtil.responseToJson(res));
                        break;
                    }

                    case LOGIN: {
                        String username = "";
                        String password = "";

                        if (message.getData() instanceof JSONObject data) {
                            username = data.optString("username", "");
                            password = data.optString("password", "");
                        }

                        boolean ok = userDao.checkLogin(username, password);

                        Response res = new Response(
                                message.getRequestId(),
                                ok,
                                ok ? "Đăng nhập thành công" : "Tên đăng nhập hoặc mật khẩu không đúng",
                                null);
                        writer.println(JsonUtil.responseToJson(res));
                        break;
                    }

                    case HEARTBEAT: {
                        String machineId = String.valueOf(message.getData());
                        boolean updated =  machineDao.updateHeartbeat(machineId);
                        System.out.println("Máy " + machineId + " đang online");

                        Response res = new Response(message.getRequestId(), updated , updated ?"Heartbeat OK":"không tìm thấy máy này", machineId);
                        writer.println(JsonUtil.responseToJson(res));
                        break;
                    }

                    case GET_CLIENTS: {
                        System.out.println("Admin yêu cầu danh sách máy");
                        List<MachineStatus> machines=machineDao.getAllMachines();
                        System.out.println("Số máy lấy được: " + machines.size());
                        Response res = new Response(message.getRequestId(), true,
                                "Danh sách máy đang kết nối", machines);

    String jsonResponse = JsonUtil.responseToJson(res);
    System.out.println("JSON trả về admin: " + jsonResponse);
                        writer.println(jsonResponse);
                        
                        break;
                    }
                    case GET_SYSTEM_INFO:
                    case GET_PROCESSES:
                    case KILL_PROCESS:
                    case SCREENSHOT: {
                        //không cho machines gửi lệnh điều khiển
                        if(currentMachineId!=null){
                            Response response=new Response(message.getRequestId(),false, 
                             "Máy tính không được phép gửi lệnh điều khiển",
                            null);
                            writer.println(JsonUtil.responseToJson(response));
                            break;
                        }
                        String target=message.getTarget();
                        if(target==null || target.isBlank()){
                            writer.println(JsonUtil.responseToJson(new Response(message.getRequestId(),
                            false, "Chưa chỉ định máy cần điều khiển",
                            null)));
                            break;
                        }
                       ClientHandle machine=clientManager.getClient(target);
                       if(machine==null){
                        writer.println(JsonUtil.responseToJson(
                            new Response(
                                    message.getRequestId(),
                                    false,
                                    "Máy không kết nối",
                                    null
                            )
                    ));
                    break;
                       }
                       //lưu admin đang chờ kết quả 
                       clientManager.adminRequest(message.getRequestId(), this);
                       //chuyển lệnh đến máy đích 
                       machine.send(JsonUtil.messageToJson(message));
                       break;
                    }

                    default: {
                        Response res = new Response(message.getRequestId(), false,
                                "Command chưa được hỗ trợ", null);
                        writer.println(JsonUtil.responseToJson(res));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Client đã ngắt kết nối: " + ip);
            e.printStackTrace();
        } finally {
            clientManager.removePendingRequest(this);
            if(currentMachineId!=null  && clientManager.getClient(currentMachineId) == this){
                machineDao.disconnectMachine(currentMachineId);
                clientManager.removeClient(currentMachineId, this);
            }
            try {
                socket.close();
            } catch (java.io.IOException ignored) {
            }
        }
    }

    private void handleLogin(Message message) {
        if (!(message.getData() instanceof JSONObject loginData)) {
            sendResponse(new Response(message.getRequestId(), false, "Dữ liệu đăng nhập không hợp lệ", null));
            return;
        }

        try {
            boolean success = userDao.authenticate(
                    loginData.optString("username", ""),
                    loginData.optString("password", ""));
            sendResponse(new Response(message.getRequestId(), success,
                    success ? "Đăng nhập thành công" : "Tên đăng nhập hoặc mật khẩu không đúng", null));
        } catch (java.sql.SQLException e) {
            sendResponse(new Response(message.getRequestId(), false,
                    "Không thể xác thực tài khoản với database", null));
            e.printStackTrace();
        }
    }

    private void handleGetMachines(Message message) {
        try {
            sendResponse(new Response(message.getRequestId(), true,
                    "Danh sách máy trong database", machineDao.getMachines()));
        } catch (java.sql.SQLException e) {
            sendResponse(new Response(message.getRequestId(), false,
                    "Không thể tải danh sách máy từ database", null));
            e.printStackTrace();
        }
    }

    private void handleAddMachine(Message message) {
        if (!(message.getData() instanceof JSONObject machine)) {
            sendResponse(new Response(message.getRequestId(), false,
                    "Dữ liệu thiết bị không hợp lệ", null));
            return;
        }

        try {
            machineDao.addMachine(
                    machine.optString("machineCode", ""),
                    machine.optString("hostname", ""),
                    machine.optString("ipAddress", ""),
                    machine.optString("os", ""),
                    machine.optString("cpu", ""),
                    machine.optString("gpu", ""),
                    machine.optString("ram", ""),
                    machine.optString("disk", ""));
            sendResponse(new Response(message.getRequestId(), true,
                    "Đã lưu thiết bị vào database", null));
        } catch (java.sql.SQLException e) {
            String errorMessage = "23505".equals(e.getSQLState())
                    ? "Mã máy đã tồn tại trong database"
                    : "Không thể lưu thiết bị vào database";
            sendResponse(new Response(message.getRequestId(), false, errorMessage, null));
            e.printStackTrace();
        }
    }

    private void handleUpdateMachine(Message message) {
        if (!(message.getData() instanceof JSONObject machine)) {
            sendResponse(new Response(message.getRequestId(), false,
                    "Dữ liệu thiết bị không hợp lệ", null));
            return;
        }

        try {
            boolean updated = machineDao.updateMachine(
                    machine.optString("originalMachineCode", ""),
                    machine.optString("machineCode", ""),
                    machine.optString("hostname", ""),
                    machine.optString("os", ""),
                    machine.optString("cpu", ""),
                    machine.optString("gpu", ""),
                    machine.optString("ram", ""),
                    machine.optString("disk", ""));
            sendResponse(new Response(message.getRequestId(), updated,
                    updated ? "Đã cập nhật thông tin thiết bị"
                            : "Không tìm thấy máy cần cập nhật", null));
        } catch (java.sql.SQLException e) {
            String errorMessage = "23505".equals(e.getSQLState())
                    ? "Mã máy đã tồn tại trong database"
                    : "Không thể cập nhật thiết bị trong database";
            sendResponse(new Response(message.getRequestId(), false, errorMessage, null));
            e.printStackTrace();
        }
    }

    private void handleSetMachineOpen(Message message, boolean open) {
        String machineCode = String.valueOf(message.getData());
        try {
            boolean updated = open
                    ? machineDao.openMachine(machineCode)
                    : machineDao.closeMachine(machineCode);
            sendResponse(new Response(message.getRequestId(), updated,
                    updated
                            ? (open ? "Đã bật máy và bắt đầu tính thời gian" : "Đã tắt máy và đặt lại thời gian")
                            : "Không tìm thấy máy", machineCode));
        } catch (java.sql.SQLException e) {
            sendResponse(new Response(message.getRequestId(), false,
                    "Không thể cập nhật trạng thái máy trong database", machineCode));
            e.printStackTrace();
        }
    }

    private void handleConnect(Message message) {
        MachineInfo machineInfo = JsonUtil.jsonToMachineInfo(message.getData());
        String machineId = machineInfo.getMachineId();
        boolean success = machineDao.connectMachine(machineId, machineInfo.getMachineName(),
                machineInfo.getIpAddress(), machineInfo.getOs());

        if (success) {
            currentMachineId = machineId;
            clientManager.addClient(machineId, this);
            System.out.println("Máy " + machineId + " đã đăng ký");
            sendResponse(new Response(message.getRequestId(), true, "connect thành công", machineId));
        } else {
            sendResponse(new Response(message.getRequestId(), false, "Không thể đăng ký máy", null));
        }
    }

    private void handleHeartbeat(Message message) {
        String machineId = String.valueOf(message.getData());
        boolean updated = machineDao.updateHeartbeat(machineId);
        if (updated) {
            System.out.println("Máy " + machineId + " đang online");
        }
        sendResponse(new Response(message.getRequestId(), updated,
                updated ? "Heartbeat OK" : "Không tìm thấy máy này", machineId));
    }

    private void handleGetClients(Message message) {
        List<MachineStatus> machines = machineDao.getAllMachines();
        sendResponse(new Response(message.getRequestId(), true,
                "Danh sách máy", machines));
    }

    private void sendResponse(Response response) {
        writer.println(JsonUtil.responseToJson(response));
    }

    public void send(String json) {
        writer.println(json);
    }
}