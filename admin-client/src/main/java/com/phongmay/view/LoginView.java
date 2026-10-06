package com.phongmay.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.phongmay.client.ServerConnection;
import com.phongmay.common.CommandType;
import com.phongmay.common.Message;
import com.phongmay.common.Response;
import com.phongmay.model.LoginRequest;

public class LoginView extends JFrame {
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JButton btnLogin;
    private ServerConnection serverConnection;

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 12345;

    public LoginView() {
        super("Đăng nhập hệ thống quản lý phòng máy");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 248, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("HỆ THỐNG QUẢN LÝ PHÒNG MÁY");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(24, 87, 155));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        lblUsername.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(lblUsername, gbc);

        txtUsername = new JTextField();
        txtUsername.setPreferredSize(new Dimension(220, 36));
        txtUsername.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtUsername, gbc);

        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblPassword, gbc);

        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(220, 36));
        txtPassword.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtPassword, gbc);

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setBackground(new Color(33, 120, 255));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(180, 40));
        btnLogin.addActionListener(e -> handleLogin());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnLogin, gbc);

        setContentPane(panel);
    }

    private void handleLogin() {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.",
                    "Lỗi đăng nhập",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        LoginRequest request = new LoginRequest(username, password);
        if (!request.isValid()) {
            return;
        }

        btnLogin.setEnabled(false);
        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws IOException {
                if (serverConnection == null || !serverConnection.isConnected()) {
                    serverConnection = new ServerConnection(SERVER_HOST, SERVER_PORT);
                    if (!serverConnection.connect()) {
                        throw new IOException("Không thể kết nối tới server "
                                + SERVER_HOST + ":" + SERVER_PORT);
                    }
                }

                Message message = new Message(
                        UUID.randomUUID().toString(),
                        CommandType.GET_CLIENTS,
                        null,
                        null);
                return serverConnection.sendRequest(message);
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                try {
                    Response response = get();
                    if (response == null || !response.isSuccess()) {
                        String errorMessage = response == null
                                ? "Server không gửi phản hồi."
                                : response.getMessage();
                        showConnectionError(errorMessage);
                        return;
                    }

                    JOptionPane.showMessageDialog(LoginView.this,
                            "Đã kết nối server. " + response.getMessage(),
                            "Kết nối thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException | ExecutionException e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    Throwable cause = e.getCause();
                    String errorMessage = cause == null ? e.getMessage() : cause.getMessage();
                    showConnectionError(errorMessage);
                }
            }
        }.execute();
    }

    private void showConnectionError(String message) {
        if (serverConnection != null) {
            serverConnection.close();
            serverConnection = null;
        }
        JOptionPane.showMessageDialog(this,
                "Không thể kết nối server: " + message,
                "Lỗi kết nối",
                JOptionPane.ERROR_MESSAGE);
    }

    public LoginRequest getLoginRequest() {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        return new LoginRequest(username, password);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}
