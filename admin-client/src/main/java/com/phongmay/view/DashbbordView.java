package com.phongmay.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONObject;

import com.phongmay.client.ServerConnection;
import com.phongmay.common.CommandType;
import com.phongmay.common.Message;
import com.phongmay.common.Response;

public class DashbbordView extends JPanel {
    private final ServerConnection serverConnection;
    private final JPanel machineListPanel;
    private final JLabel feedbackLabel;

    public DashbbordView(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 248, 252));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("QUẢN LÝ PHÒNG MÁY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(24, 87, 155));
        add(titleLabel, BorderLayout.NORTH);

        machineListPanel = new JPanel();
        machineListPanel.setLayout(new BoxLayout(machineListPanel, BoxLayout.Y_AXIS));
        machineListPanel.setOpaque(false);
        add(new JScrollPane(machineListPanel), BorderLayout.CENTER);

        feedbackLabel = new JLabel("Đang tải dữ liệu máy...", SwingConstants.CENTER);
        add(feedbackLabel, BorderLayout.SOUTH);
        loadMachines();
    }

    private void loadMachines() {
        if (serverConnection == null) {
            feedbackLabel.setText("Không có kết nối tới server.");
            return;
        }

        new SwingWorker<JSONArray, Void>() {
            @Override
            protected JSONArray doInBackground() throws IOException {
                Message request = new Message(
                        UUID.randomUUID().toString(), CommandType.GET_MACHINES, null, null);
                Response response = serverConnection.sendRequest(request);
                if (response == null || !response.isSuccess()) {
                    throw new IOException(response == null
                            ? "Server không gửi phản hồi."
                            : response.getMessage());
                }
                if (response.getData() instanceof JSONArray machines) {
                    return machines;
                }
                throw new IOException("Server trả về dữ liệu máy không hợp lệ.");
            }

            @Override
            protected void done() {
                machineListPanel.removeAll();
                try {
                    JSONArray machines = get();
                    for (int index = 0; index < machines.length(); index++) {
                        JSONObject machine = machines.getJSONObject(index);
                        machineListPanel.add(createMachineRow(
                                machine.optString("machine_code", "Không rõ mã"),
                                machine.optString("hostname", ""),
                                machine.optString("ip_address", ""),
                                machine.optString("status", "UNKNOWN")));
                    }
                    feedbackLabel.setText(machines.isEmpty()
                            ? "Chưa có máy tính trong database."
                            : "Tổng số máy: " + machines.length());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    feedbackLabel.setText("Đã hủy tải dữ liệu máy.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    feedbackLabel.setText("Không tải được dữ liệu: "
                            + (cause == null ? e.getMessage() : cause.getMessage()));
                }
                machineListPanel.revalidate();
                machineListPanel.repaint();
            }
        }.execute();
    }

    private JPanel createMachineRow(String machineCode, String hostname, String ipAddress, String status) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 235), 1),
                new EmptyBorder(12, 16, 12, 16)));
        row.setPreferredSize(new Dimension(0, 56));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);
        JLabel machineLabel = new JLabel(machineCode);
        machineLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        machineLabel.setForeground(new Color(30, 41, 59));
        JLabel detailsLabel = new JLabel((hostname.isBlank() ? "" : hostname)
                + (ipAddress.isBlank() ? "" : "  |  " + ipAddress));
        detailsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        detailsLabel.setForeground(new Color(100, 116, 139));
        detailsPanel.add(machineLabel);
        detailsPanel.add(detailsLabel);

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setForeground("ONLINE".equalsIgnoreCase(status)
                ? new Color(34, 150, 90)
                : "OFFLINE".equalsIgnoreCase(status)
                        ? new Color(200, 55, 65)
                        : new Color(190, 120, 30));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(detailsPanel, BorderLayout.WEST);
        row.add(statusLabel, BorderLayout.EAST);

        return row;
    }
}
