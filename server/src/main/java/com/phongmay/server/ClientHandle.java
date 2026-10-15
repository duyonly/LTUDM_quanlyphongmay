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
                Message message = JsonUtil.jsonToMessage(json);
                System.out.println("Command: " + message.getCommand());
                System.out.println("Target: " + message.getTarget());

                switch (message.getCommand()) {
                    case LOGIN -> handleLogin(message);
                    case GET_MACHINES -> handleGetMachines(message);
                    case ADD_MACHINE -> handleAddMachine(message);
                    case UPDATE_MACHINE -> handleUpdateMachine(message);
                    case OPEN_MACHINE -> handleSetMachineOpen(message, true);
                    case CLOSE_MACHINE -> handleSetMachineOpen(message, false);
                    case CONNECT -> handleConnect(message);
                    case HEARTBEAT -> handleHeartbeat(message);
                    case GET_CLIENTS -> handleGetClients(message);
                    default -> sendResponse(new Response(message.getRequestId(), false,
                            "Command chưa được hỗ trợ", null));
                }
            }
        } catch (Exception e) {
            System.out.println("Client đã ngắt kết nối: " + ip);
            e.printStackTrace();
        } finally {
            if (currentMachineId != null) {
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