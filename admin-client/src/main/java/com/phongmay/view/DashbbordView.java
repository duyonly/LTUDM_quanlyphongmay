package com.phongmay.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class DashbbordView extends JPanel {

    public DashbbordView() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 248, 252));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("QUẢN LÝ PHÒNG MÁY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(24, 87, 155));
        add(titleLabel, BorderLayout.NORTH);

        JPanel machineListPanel = new JPanel();
        machineListPanel.setLayout(new BoxLayout(machineListPanel, BoxLayout.Y_AXIS));
        machineListPanel.setOpaque(false);

        machineListPanel.add(createMachineRow("PC01", "ONLINE", new Color(34, 197, 94)));
        machineListPanel.add(createMachineRow("PC02", "ONLINE", new Color(34, 197, 94)));
        machineListPanel.add(createMachineRow("PC03", "OFFLINE", new Color(220, 53, 69)));
        machineListPanel.add(createMachineRow("PC04", "ONLINE", new Color(34, 197, 94)));

        add(machineListPanel, BorderLayout.CENTER);
    }

    private JPanel createMachineRow(String machineName, String status, Color statusColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 235), 1),
                new EmptyBorder(12, 16, 12, 16)));
        row.setPreferredSize(new Dimension(0, 56));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel machineLabel = new JLabel("🟢 " + machineName);
        machineLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        machineLabel.setForeground(new Color(30, 41, 59));

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setForeground(statusColor);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(machineLabel, BorderLayout.WEST);
        row.add(statusLabel, BorderLayout.EAST);

        return row;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new DashbbordView());
            frame.setSize(500, 420);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
