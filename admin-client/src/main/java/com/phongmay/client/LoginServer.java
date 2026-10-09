package com.phongmay.client;

import java.io.IOException;
import java.util.UUID;

import org.json.JSONObject;

import com.phongmay.common.CommandType;
import com.phongmay.common.Message;
import com.phongmay.common.Response;
import com.phongmay.model.LoginRequest;

public class LoginServer {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 12345;

    private final ServerConnection connection;

    public LoginServer() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public LoginServer(String host, int port) {
        this.connection = new ServerConnection(host, port);
    }

    public boolean connect() {
        return connection.connect();
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public Response login(String username, String password) throws IOException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }

        LoginRequest loginRequest = new LoginRequest(username.trim(), password);
        return login(loginRequest);
    }

    public Response login(LoginRequest loginRequest) throws IOException {
        if (loginRequest == null) {
            throw new IllegalArgumentException("LoginRequest không được null.");
        }
        if (!loginRequest.isValid()) {
            throw new IllegalArgumentException("Tên đăng nhập và mật khẩu không hợp lệ.");
        }

        if (!connection.isConnected()) {
            if (!connect()) {
                throw new IOException("Không thể kết nối tới server " + connection.toString());
            }
        }

        JSONObject data = new JSONObject();
        data.put("username", loginRequest.getUsername().trim());
        data.put("password", loginRequest.getPassword());

        Message message = new Message(
                UUID.randomUUID().toString(),
                CommandType.LOGIN,
                "admin-client",
                data
        );

        return connection.sendRequest(message);
    }

    public void close() {
        connection.close();
    }
}

