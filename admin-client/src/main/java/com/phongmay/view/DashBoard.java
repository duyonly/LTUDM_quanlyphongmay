package com.phongmay.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class DashBoard extends JPanel {
    private static final Color CANVAS = new Color(243, 246, 244);
    private static final Color INK = new Color(28, 43, 40);
    private static final Color MUTED = new Color(105, 119, 114);
    private static final Color GREEN = new Color(30, 123, 91);

    private final DefaultTableModel machineTableModel;
    private final JTable machineTable;
    private final TableRowSorter<DefaultTableModel> tableSorter;
    private final JTextField searchField;
    private final JComboBox<String> statusFilter;
    private final JLabel totalValue = new JLabel("0");
    private final JLabel onlineValue = new JLabel("0");
    private final JLabel offlineValue = new JLabel("0");
    private final JLabel lockedValue = new JLabel("0");
    private final JLabel selectionSummary = new JLabel("Chọn một máy để xem nhanh thông tin");

    public DashBoard() {
        setLayout(new BorderLayout(0, 18));
        setBackground(CANVAS);
        setBorder(new EmptyBorder(24, 28, 22, 28));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new javax.swing.BoxLayout(titleBlock, javax.swing.BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        JLabel eyebrow = new JLabel("ĐIỀU HÀNH PHÒNG MÁY");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(GREEN);
        JLabel titleLabel = new JLabel("Tổng quan hệ thống");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(INK);
        JLabel dateLabel = new JLabel(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(MUTED);
        titleBlock.add(eyebrow);
        titleBlock.add(titleLabel);
        titleBlock.add(dateLabel);

        JLabel adminLabel = new JLabel("AD   QUẢN TRỊ VIÊN");
        adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminLabel.setForeground(INK);
        adminLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 223)),
                new EmptyBorder(10, 14, 10, 14)));
        heading.add(titleBlock, BorderLayout.WEST);
        heading.add(adminLabel, BorderLayout.EAST);

        machineTableModel = new DefaultTableModel(
                new Object[] { "Mã máy", "Tên máy / Host", "Địa chỉ IP", "Phòng", "Người dùng",
                        "Trạng thái", "CPU", "RAM", "Hoạt động gần nhất" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        machineTable = new JTable(machineTableModel);
        machineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        machineTable.setRowHeight(40);
        machineTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        machineTable.setForeground(INK);
        machineTable.setBackground(Color.WHITE);
        machineTable.setGridColor(new Color(232, 237, 233));
        machineTable.setShowVerticalLines(false);
        machineTable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        machineTable.setFillsViewportHeight(true);
        machineTable.setSelectionBackground(new Color(225, 241, 234));
        machineTable.setSelectionForeground(INK);
        machineTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        machineTable.getTableHeader().setForeground(MUTED);
        machineTable.getTableHeader().setBackground(new Color(248, 250, 248));
        machineTable.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 40));
        machineTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                new Color(225, 232, 227)));
        machineTable.getColumnModel().getColumn(5).setCellRenderer(new MachineStatusRenderer());
        machineTable.getColumnModel().getColumn(0).setPreferredWidth(72);
        machineTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        machineTable.getColumnModel().getColumn(2).setPreferredWidth(105);
        machineTable.getColumnModel().getColumn(3).setPreferredWidth(78);
        machineTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        machineTable.getColumnModel().getColumn(5).setPreferredWidth(112);
        machineTable.getColumnModel().getColumn(6).setPreferredWidth(55);
        machineTable.getColumnModel().getColumn(7).setPreferredWidth(55);
        machineTable.getColumnModel().getColumn(8).setPreferredWidth(132);
        tableSorter = new TableRowSorter<>(machineTableModel);
        machineTable.setRowSorter(tableSorter);

        addSampleMachines();

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setToolTipText("Tìm theo mã máy, hostname, IP hoặc người dùng");
        searchField.setPreferredSize(new java.awt.Dimension(240, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 225, 218)),
                new EmptyBorder(7, 10, 7, 10)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                applyFilters();
            }
        });
        statusFilter = new JComboBox<>(new String[] { "Tất cả trạng thái", "ONLINE", "OFFLINE", "LOCKED" });
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setPreferredSize(new java.awt.Dimension(165, 36));
        statusFilter.addActionListener(event -> applyFilters());

        JButton addButton = createButton("+  Thêm máy", GREEN, Color.WHITE);
        addButton.addActionListener(event -> addMachine());
        JButton editButton = createButton("Sửa", Color.WHITE, INK);
        editButton.addActionListener(event -> editMachine());
        JButton deleteButton = createButton("Xóa", Color.WHITE, new Color(171, 67, 59));
        deleteButton.addActionListener(event -> deleteMachine());
        JButton openButton = createButton("Bật", Color.WHITE, GREEN);
        openButton.addActionListener(event -> setMachineOpen(true));
        JButton closeButton = createButton("Tắt", Color.WHITE, new Color(171, 67, 59));
        closeButton.addActionListener(event -> setMachineOpen(false));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(openButton);
        actionPanel.add(closeButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(addButton);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Tìm kiếm");
        searchLabel.setForeground(MUTED);
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(statusFilter);

        JPanel tableHeading = new JPanel(new BorderLayout(0, 10));
        tableHeading.setOpaque(false);
        JPanel sectionTitle = new JPanel(new BorderLayout());
        sectionTitle.setOpaque(false);
        JLabel machinesTitle = new JLabel("Máy trạm");
        machinesTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        machinesTitle.setForeground(INK);
        JLabel inventoryLabel = new JLabel("DANH SÁCH THIẾT BỊ");
        inventoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        inventoryLabel.setForeground(MUTED);
        sectionTitle.add(machinesTitle, BorderLayout.WEST);
        sectionTitle.add(inventoryLabel, BorderLayout.EAST);
        tableHeading.add(sectionTitle, BorderLayout.NORTH);
        tableHeading.add(filterPanel, BorderLayout.CENTER);
        tableHeading.add(actionPanel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(machineTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(224, 231, 226)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        selectionSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectionSummary.setForeground(MUTED);
        selectionSummary.setBorder(new EmptyBorder(8, 2, 0, 2));
        machineTable.getSelectionModel().addListSelectionListener(event -> updateSelectionSummary());

        JPanel machineSection = new JPanel(new BorderLayout(0, 12));
        machineSection.setOpaque(false);
        machineSection.add(tableHeading, BorderLayout.NORTH);
        machineSection.add(scrollPane, BorderLayout.CENTER);
        machineSection.add(selectionSummary, BorderLayout.SOUTH);

        JPanel northContent = new JPanel(new BorderLayout(0, 18));
        northContent.setOpaque(false);
        northContent.add(heading, BorderLayout.NORTH);
        northContent.add(createMetricsPanel(), BorderLayout.CENTER);

        add(northContent, BorderLayout.NORTH);
        add(machineSection, BorderLayout.CENTER);
        refreshMetrics();
    }

    private JPanel createMetricsPanel() {
        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0));
        metrics.setOpaque(false);
        metrics.add(createMetricCard("TỔNG SỐ MÁY", totalValue, new Color(46, 106, 145), "Thiết bị trong phòng"));
        metrics.add(createMetricCard("ĐANG HOẠT ĐỘNG", onlineValue, GREEN, "Có kết nối tới server"));
        metrics.add(createMetricCard("MẤT KẾT NỐI", offlineValue, new Color(190, 87, 72), "Cần kiểm tra trạng thái"));
        metrics.add(createMetricCard("ĐANG KHÓA", lockedValue, new Color(180, 126, 39), "Đang giới hạn sử dụng"));
        return metrics;
    }

    private JPanel createMetricCard(String title, JLabel value, Color accent, String note) {
        JPanel card = new JPanel(new BorderLayout(0, 7));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(229, 235, 230)),
                        new EmptyBorder(12, 14, 12, 14))));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(MUTED);
        value.setFont(new Font("Segoe UI", Font.BOLD, 25));
        value.setForeground(INK);
        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        noteLabel.setForeground(MUTED);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(noteLabel, BorderLayout.SOUTH);
        card.setPreferredSize(new java.awt.Dimension(180, 94));
        return card;
    }

    private JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setMargin(new Insets(8, 12, 8, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background == Color.WHITE
                        ? new Color(215, 225, 218) : background),
                new EmptyBorder(2, 3, 2, 3)));
        return button;
    }

    private void addSampleMachines() {
        machineTableModel.addRow(new Object[] { "PM-A01", "LAB-A01", "10.10.1.11", "A-101", "Trống", "ONLINE", "18%", "42%", "1 phút trước" });
        machineTableModel.addRow(new Object[] { "PM-A02", "LAB-A02", "10.10.1.12", "A-101", "Nguyễn An", "ONLINE", "34%", "67%", "Vừa cập nhật" });
        machineTableModel.addRow(new Object[] { "PM-A03", "LAB-A03", "10.10.1.13", "A-101", "Trần Minh", "ONLINE", "52%", "71%", "Vừa cập nhật" });
        machineTableModel.addRow(new Object[] { "PM-A04", "LAB-A04", "10.10.1.14", "A-101", "Trống", "OFFLINE", "—", "—", "Hôm qua, 17:42" });
        machineTableModel.addRow(new Object[] { "PM-B01", "LAB-B01", "10.10.2.21", "B-203", "Lê Hà", "ONLINE", "26%", "58%", "Vừa cập nhật" });
        machineTableModel.addRow(new Object[] { "PM-B02", "LAB-B02", "10.10.2.22", "B-203", "Trống", "LOCKED", "4%", "39%", "2 phút trước" });
        machineTableModel.addRow(new Object[] { "PM-B03", "LAB-B03", "10.10.2.23", "B-203", "Phạm Long", "ONLINE", "61%", "76%", "Vừa cập nhật" });
        machineTableModel.addRow(new Object[] { "PM-B04", "LAB-B04", "10.10.2.24", "B-203", "Trống", "OFFLINE", "—", "—", "Hôm qua, 18:05" });
    }

    private void applyFilters() {
        String query = searchField.getText().trim();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        java.util.List<RowFilter<DefaultTableModel, Integer>> filters = new java.util.ArrayList<>();
        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
        }
        if (selectedStatus != null && !selectedStatus.equals("Tất cả trạng thái")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(selectedStatus) + "$", 5));
        }
        tableSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void refreshMetrics() {
        int online = 0;
        int offline = 0;
        int locked = 0;
        for (int row = 0; row < machineTableModel.getRowCount(); row++) {
            String status = machineTableModel.getValueAt(row, 5).toString();
            if ("ONLINE".equals(status)) {
                online++;
            } else if ("OFFLINE".equals(status)) {
                offline++;
            } else if ("LOCKED".equals(status)) {
                locked++;
            }
        }
        totalValue.setText(Integer.toString(machineTableModel.getRowCount()));
        onlineValue.setText(Integer.toString(online));
        offlineValue.setText(Integer.toString(offline));
        lockedValue.setText(Integer.toString(locked));
    }

    private void updateSelectionSummary() {
        int viewRow = machineTable.getSelectedRow();
        if (viewRow < 0) {
            selectionSummary.setText("Chọn một máy để xem nhanh thông tin");
            return;
        }
        int row = machineTable.convertRowIndexToModel(viewRow);
        selectionSummary.setText("Đang chọn: " + machineTableModel.getValueAt(row, 0)
                + "   •   " + machineTableModel.getValueAt(row, 2)
                + "   •   Phòng " + machineTableModel.getValueAt(row, 3));
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
        machineTableModel.addRow(new Object[] {
                details[0], details[1], "—", "Chưa phân phòng", "Trống", "OFFLINE", "—", "—", "Chưa kết nối" });
        refreshMetrics();
        applyFilters();
    }

    private void editMachine() {
        int viewRow = machineTable.getSelectedRow();
        if (viewRow < 0) {
            showError("Vui lòng chọn máy cần sửa.");
            return;
        }
        int selectedRow = machineTable.convertRowIndexToModel(viewRow);

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
        applyFilters();
    }

    private void deleteMachine() {
        int viewRow = machineTable.getSelectedRow();
        if (viewRow < 0) {
            showError("Vui lòng chọn máy cần xóa.");
            return;
        }
        int selectedRow = machineTable.convertRowIndexToModel(viewRow);

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Xóa máy " + machineTableModel.getValueAt(selectedRow, 0) + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            machineTableModel.removeRow(selectedRow);
            refreshMetrics();
        }
    }

    private void setMachineOpen(boolean open) {
        int viewRow = machineTable.getSelectedRow();
        if (viewRow < 0) {
            showError("Vui lòng chọn máy cần thay đổi trạng thái.");
            return;
        }
        int selectedRow = machineTable.convertRowIndexToModel(viewRow);
        machineTableModel.setValueAt(open ? "ONLINE" : "OFFLINE", selectedRow, 5);
        refreshMetrics();
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
                String status = value == null ? "" : value.toString();
                boolean isOnline = "ONLINE".equals(status);
                boolean isLocked = "LOCKED".equals(status);
                Color color = isOnline ? GREEN : isLocked
                    ? new Color(180, 126, 39) : new Color(190, 87, 72);
                label.setText(isOnline ? "Đang hoạt động" : isLocked ? "Đang khóa" : "Mất kết nối");
                label.setForeground(color);
                label.setIcon(new StatusIcon(color));
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
