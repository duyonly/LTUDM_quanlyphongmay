package com.phongmay.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import com.phongmay.common.JsonUtil;
import com.phongmay.common.Message;
import com.phongmay.common.Response;

public class ServerConnection {
    // Địa chỉ server mà admin client cần kết nối.
    private final String host;
    // Port TCP của server.
    private final int port;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Kết nối tới server và khởi tạo luồng gửi/nhận dữ liệu.
    public boolean connect() {
        try {
            if (isConnected()) {
                return true;
            }

            socket = new Socket(host, port);
            socket.setSoTimeout(15000);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            System.out.println("Admin Client connected to Server.");
            return true;
        } catch (IOException e) {
            System.out.println("Cannot connect to Server.");
            System.out.println(e.getMessage());
            return false;
        }
    }

    // Gửi chuỗi JSON hoặc text thô tới server.
    public void send(String message) {
        if (writer != null && message != null) {
            writer.println(message);
        }
    }

    // Nhận một dòng dữ liệu từ server.
    public String receive() throws IOException {
        if (reader != null) {
            return reader.readLine();
        }
        return null;
    }

    // Gửi request theo protocol của dự án và trả về response tương ứng.
    public Response sendRequest(Message request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        if (!isConnected()) {
            throw new IllegalStateException("Server connection is not established");
        }

        writer.println(JsonUtil.messageToJson(request));
        String responseJson = reader.readLine();

        if (responseJson == null || responseJson.isBlank()) {
            return null;
        }

        return JsonUtil.jsonToResponse(responseJson);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    // Đóng kết nối sau khi làm việc xong.
    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

