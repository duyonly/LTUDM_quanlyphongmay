package com.phongmay.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

import com.phongmay.common.JsonUtil;
import com.phongmay.common.MachineInfo;
import com.phongmay.common.Message;
import com.phongmay.common.Response;
import com.phongmay.server.dao.MachineDao;
import com.phongmay.server.dao.UserDao;

public class ClientHandle implements Runnable {
    private final Socket socket;
    private final ClientManager clientManager;
    private PrintWriter writer;
    private final MachineDao machineDao = new MachineDao();
    private  final UserDao userDao=new UserDao();
    public ClientHandle(Socket socket, ClientManager clientManager) {
        this.socket = socket;
        this.clientManager = clientManager;
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
                        machineDao.updateHeartbeat(machineId);
                        System.out.println("Máy " + machineId + " đang online");

                        Response res = new Response(message.getRequestId(), true, "Heartbeat OK", machineId);
                        writer.println(JsonUtil.responseToJson(res));
                        break;
                    }

                    case GET_CLIENTS: {
                        String clients = clientManager.getClientId().toString();
                        Response res = new Response(message.getRequestId(), true,
                                "Danh sách máy đang kết nối", clients);
                        writer.println(JsonUtil.responseToJson(res));
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
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void send(String json) {
        writer.println(json);
    }
}