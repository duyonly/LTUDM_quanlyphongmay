package com.phongmay.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DashBoard extends JPanel {
    private final DefaultTableModel machineTableModel;
    private final JTable machineTable;

    public DashBoard() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 248, 252));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("QUẢN LÝ PHÒNG MÁY", SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(24, 87, 155));

        machineTableModel = new DefaultTableModel(
                new Object[] { "Mã máy", "Tên máy", "Trạng thái" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        machineTable = new JTable(machineTableModel);
        machineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        machineTable.setRowHeight(36);
        machineTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        machineTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        machineTable.setFillsViewportHeight(true);
        machineTable.getColumnModel().getColumn(2).setCellRenderer(new MachineStatusRenderer());

        machineTableModel.addRow(new Object[] { "PC01", "Máy 01", "Đang mở" });
        machineTableModel.addRow(new Object[] { "PC02", "Máy 02", "Đang mở" });
        machineTableModel.addRow(new Object[] { "PC03", "Máy 03", "Đang đóng" });
        machineTableModel.addRow(new Object[] { "PC04", "Máy 04", "Đang mở" });

        JButton addButton = new JButton("Thêm máy");
        addButton.addActionListener(event -> addMachine());
        JButton editButton = new JButton("Sửa máy");
        editButton.addActionListener(event -> editMachine());
        JButton deleteButton = new JButton("Xóa máy");
        deleteButton.addActionListener(event -> deleteMachine());
        JButton openButton = new JButton("Mở máy");
        openButton.addActionListener(event -> setMachineOpen(true));
        JButton closeButton = new JButton("Đóng máy");
        closeButton.addActionListener(event -> setMachineOpen(false));

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(addButton);
        actionsPanel.add(editButton);
        actionsPanel.add(deleteButton);
        actionsPanel.add(openButton);
        actionsPanel.add(closeButton);

        JPanel headerPanel = new JPanel(new BorderLayout(0, 16));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(actionsPanel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(machineTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(214, 223, 235)));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addMachine() {
        String[] details = promptMachineDetails("Thêm máy", "", "");
        if (details == null) {
            return;
        }
        if (!validateMachineDetails(details)) {
            return;
        }
        if (findMachineRow(details[0]) >= 0) {
            showError("Mã máy đã tồn tại.");
            return;
        }
        machineTableModel.addRow(new Object[] { details[0], details[1], "Đang đóng" });
    }

    private void editMachine() {
        int selectedRow = machineTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Vui lòng chọn máy cần sửa.");
            return;
        }

        String[] details = promptMachineDetails(
                "Sửa máy",
                machineTableModel.getValueAt(selectedRow, 0).toString(),
                machineTableModel.getValueAt(selectedRow, 1).toString());
        if (details == null) {
            return;
        }
        if (!validateMachineDetails(details)) {
            return;
        }
        int duplicateRow = findMachineRow(details[0]);
        if (duplicateRow >= 0 && duplicateRow != selectedRow) {
            showError("Mã máy đã tồn tại.");
            return;
        }

        machineTableModel.setValueAt(details[0], selectedRow, 0);
        machineTableModel.setValueAt(details[1], selectedRow, 1);
    }

    private void deleteMachine() {
        int selectedRow = machineTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Vui lòng chọn máy cần xóa.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Xóa máy " + machineTableModel.getValueAt(selectedRow, 0) + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            machineTableModel.removeRow(selectedRow);
        }
    }

    private void setMachineOpen(boolean open) {
        int selectedRow = machineTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Vui lòng chọn máy cần thay đổi trạng thái.");
            return;
        }
        machineTableModel.setValueAt(open ? "Đang mở" : "Đang đóng", selectedRow, 2);
    }

    private String[] promptMachineDetails(String title, String machineId, String machineName) {
        JTextField machineIdField = new JTextField(machineId, 16);
        JTextField machineNameField = new JTextField(machineName, 16);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Mã máy:"));
        form.add(machineIdField);
        form.add(new JLabel("Tên máy:"));
        form.add(machineNameField);

        int result = JOptionPane.showConfirmDialog(
                this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String[] { machineIdField.getText().trim(), machineNameField.getText().trim() };
    }

    private boolean validateMachineDetails(String[] details) {
        if (details[0].isEmpty() || details[1].isEmpty()) {
            showError("Mã máy và tên máy không được để trống.");
            return false;
        }
        return true;
    }

    private int findMachineRow(String machineId) {
        for (int row = 0; row < machineTableModel.getRowCount(); row++) {
            if (machineId.equalsIgnoreCase(machineTableModel.getValueAt(row, 0).toString())) {
                return row;
            }
        }
        return -1;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Quản lý máy", JOptionPane.ERROR_MESSAGE);
    }

    private static class MachineStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            boolean isOpen = "Đang mở".equals(value);
            label.setIcon(new StatusIcon(isOpen
                    ? new Color(34, 197, 94)
                    : new Color(220, 53, 69)));
            label.setIconTextGap(8);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setBorder(new EmptyBorder(0, 12, 0, 0));
            return label;
        }
    }

    private static class StatusIcon implements Icon {
        private final Color color;

        private StatusIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            graphics.setColor(color);
            graphics.fillOval(x, y + 2, 12, 12);
        }

        @Override
        public int getIconWidth() {
            return 12;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new DashBoard());
            frame.setSize(500, 420);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
