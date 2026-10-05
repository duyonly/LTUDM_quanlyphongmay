package com.phongmay.cilent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {
    // Server mà client sẽ kết nối tới.
    private final String host;
    // Port TCP của server.
    private final int port;
    // Socket sống của kết nối hiện tại.
    private Socket socket;
    // Đọc dữ liệu trả về từ server.
    private BufferedReader reader;
    // Gửi dữ liệu lên server.
    private PrintWriter writer;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Thiết lập kết nối tới server và mở stream đọc/ghi.
    public boolean connect() throws IOException {
        if (isConnected()) {
            return true;
        }

        socket = new Socket(host, port);
        socket.setSoTimeout(15000);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        return isConnected();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    // Gửi Request JSON tới server và chờ phản hồi Response JSON.
    public com.phongmay.cilent.Response sendRequest(com.phongmay.cilent.Request request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (!isConnected()) {
            throw new IllegalStateException("Server connection is not established");
        }

        writer.println(request.toJson());
        String responseJson = reader.readLine();
        if (responseJson == null || responseJson.isBlank()) {
            return null;
        }

        return com.phongmay.cilent.Response.fromJson(responseJson);
    }

    // Gửi một chuỗi JSON thô nếu cần dùng trực tiếp.
    public void sendRaw(String json) {
        if (!isConnected()) {
            throw new IllegalStateException("Server connection is not established");
        }
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("JSON payload cannot be empty");
        }
        writer.println(json);
    }

    // Đọc một dòng JSON từ server.
    public String receiveRaw() throws IOException {
        if (!isConnected()) {
            throw new IllegalStateException("Server connection is not established");
        }
        String line = reader.readLine();
        return line == null || line.isBlank() ? null : line;
    }

    // Đóng kết nối an toàn sau khi không dùng nữa.
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
        if (writer != null) {
            writer.close();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
