package com.phongmay.model;

public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest() {
        // Constructor mặc định để thuận tiện khi deserialize hoặc khởi tạo đối tượng rỗng.
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isValid() {
        return username != null && !username.trim().isEmpty()
                && password != null && !password.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
