package com.phongmay.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
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
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.json.JSONArray;
import org.json.JSONObject;

import com.phongmay.client.ServerConnection;
import com.phongmay.common.CommandType;
import com.phongmay.common.Message;
import com.phongmay.common.Response;

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
    private final JLabel feedbackLabel = new JLabel("Đang tải dữ liệu máy...");
    private final ServerConnection serverConnection;
    private final Map<String, Long> uptimeStartByMachine = new HashMap<>();
    private final Timer uptimeTimer;
    private boolean loadingMachines;
    private boolean savingMachine;
    private boolean updatingMachineStatus;

    public DashBoard() {
        this(new ServerConnection("127.0.0.1", 12345));
    }

    public DashBoard(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
        uptimeTimer = new Timer(1000, event -> refreshUptimeDisplays());
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
            new Object[] { "Mã máy", "Tên máy / Host", "Địa chỉ IP", "Hệ điều hành", "CPU",
                "GPU", "RAM", "Trạng thái", "Ổ đĩa", "Thời gian hoạt động" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        machineTable = new JTable(machineTableModel);
        machineTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
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
        machineTable.getColumnModel().getColumn(7).setCellRenderer(new MachineStatusRenderer());
        machineTable.getColumnModel().getColumn(0).setPreferredWidth(72);
        machineTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        machineTable.getColumnModel().getColumn(2).setPreferredWidth(105);
        machineTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        machineTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        machineTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        machineTable.getColumnModel().getColumn(6).setPreferredWidth(65);
        machineTable.getColumnModel().getColumn(7).setPreferredWidth(112);
        machineTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        machineTable.getColumnModel().getColumn(9).setPreferredWidth(132);
        tableSorter = new TableRowSorter<>(machineTableModel);
        machineTable.setRowSorter(tableSorter);

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
        JButton refreshButton = createButton("Làm mới", Color.WHITE, INK);
        refreshButton.addActionListener(event -> loadMachines());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(refreshButton);
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
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        selectionSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectionSummary.setForeground(MUTED);
        selectionSummary.setBorder(new EmptyBorder(8, 2, 0, 2));
        feedbackLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        feedbackLabel.setForeground(MUTED);
        footer.add(selectionSummary, BorderLayout.WEST);
        footer.add(feedbackLabel, BorderLayout.EAST);
        machineSection.add(footer, BorderLayout.SOUTH);

        JPanel northContent = new JPanel(new BorderLayout(0, 18));
        northContent.setOpaque(false);
        northContent.add(heading, BorderLayout.NORTH);
        northContent.add(createMetricsPanel(), BorderLayout.CENTER);

        add(northContent, BorderLayout.NORTH);
        add(machineSection, BorderLayout.CENTER);
        loadMachines();
        uptimeTimer.start();
    }

    @Override
    public void removeNotify() {
        uptimeTimer.stop();
        super.removeNotify();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        uptimeTimer.start();
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

    private void loadMachines() {
        if (loadingMachines) {
            return;
        }
        loadingMachines = true;
        feedbackLabel.setText("Đang tải dữ liệu máy...");

        new SwingWorker<JSONArray, Void>() {
            @Override
            protected JSONArray doInBackground() throws IOException {
                if (!serverConnection.isConnected() && !serverConnection.connect()) {
                    throw new IOException("Không thể kết nối tới server.");
                }
                Message request = new Message(UUID.randomUUID().toString(), CommandType.GET_MACHINES, null, null);
                Response response = serverConnection.sendRequest(request);
                if (response == null || !response.isSuccess()) {
                    throw new IOException(response == null
                            ? "Server không gửi phản hồi."
                            : response.getMessage());
                }
                if (response.getData() instanceof JSONArray machines) {
                    return machines;
                }
                throw new IOException("Dữ liệu máy server trả về không hợp lệ.");
            }

            @Override
            protected void done() {
                loadingMachines = false;
                try {
                    JSONArray machines = get();
                    machineTableModel.setRowCount(0);
                    uptimeStartByMachine.clear();
                    for (int index = 0; index < machines.length(); index++) {
                        JSONObject machine = machines.getJSONObject(index);
                        String machineCode = machine.optString("machine_code", "—");
                        long activeSince = machine.optLong("active_since_ms", 0L);
                        long uptimeSeconds = machine.optLong("uptime_seconds", 0L);
                        if (activeSince > 0) {
                            uptimeStartByMachine.put(machineCode, activeSince);
                        }
                        machineTableModel.addRow(new Object[] {
                                machineCode,
                                machine.optString("hostname", "—"),
                                machine.optString("ip_address", "—"),
                                machine.optString("os", "—"),
                                machine.optString("cpu", "—"),
                                machine.optString("gpu", "—"),
                                machine.optString("ram", "—"),
                                machine.optString("status", "UNKNOWN"),
                                machine.optString("disk", "—"),
                                formatDuration(uptimeSeconds)
                        });
                    }
                    adjustColumnWidths();
                    refreshMetrics();
                    applyFilters();
                    feedbackLabel.setText("Đã tải " + machines.length() + " máy từ database");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    feedbackLabel.setText("Đã hủy tải dữ liệu máy.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    feedbackLabel.setText("Không tải được dữ liệu: "
                            + (cause == null ? e.getMessage() : cause.getMessage()));
                }
            }
        }.execute();
    }

    private void applyFilters() {
        String query = searchField.getText().trim();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        java.util.List<RowFilter<DefaultTableModel, Integer>> filters = new java.util.ArrayList<>();
        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
        }
        if (selectedStatus != null && !selectedStatus.equals("Tất cả trạng thái")) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(selectedStatus) + "$", 7));
        }
        tableSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void adjustColumnWidths() {
        for (int columnIndex = 0; columnIndex < machineTable.getColumnCount(); columnIndex++) {
            TableColumn column = machineTable.getColumnModel().getColumn(columnIndex);
            Component header = machineTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(
                            machineTable, column.getHeaderValue(), false, false, -1, columnIndex);
            int preferredWidth = header.getPreferredSize().width;
            for (int row = 0; row < machineTable.getRowCount(); row++) {
                Component cell = machineTable.prepareRenderer(
                        machineTable.getCellRenderer(row, columnIndex), row, columnIndex);
                preferredWidth = Math.max(preferredWidth, cell.getPreferredSize().width);
            }
            int width = Math.max(72, Math.min(380, preferredWidth + 24));
            column.setPreferredWidth(width);
            column.setWidth(width);
        }
    }

    private void refreshMetrics() {
        int online = 0;
        int offline = 0;
        int locked = 0;
        for (int row = 0; row < machineTableModel.getRowCount(); row++) {
            String status = machineTableModel.getValueAt(row, 7).toString();
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

    private void refreshUptimeDisplays() {
        long now = System.currentTimeMillis();
        for (int row = 0; row < machineTableModel.getRowCount(); row++) {
            String machineCode = machineTableModel.getValueAt(row, 0).toString();
            Long startedAt = uptimeStartByMachine.get(machineCode);
            if (startedAt != null) {
                machineTableModel.setValueAt(formatUptime(startedAt, now), row, 9);
            }
        }
    }

    private String formatDuration(long elapsedSeconds) {
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String formatUptime(long startedAt, long now) {
        long elapsedSeconds = Math.max(0, (now - startedAt) / 1000);
        return formatDuration(elapsedSeconds);
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
            + "   •   " + machineTableModel.getValueAt(row, 3));
    }

    private void addMachine() {
        if (savingMachine) {
            return;
        }
        String[] details = promptNewMachine();
        if (details == null) {
            return;
        }
        if (!validateNewMachineDetails(details)) {
            return;
        }
        if (findMachineRow(details[0]) >= 0) {
            showError("Mã máy đã tồn tại.");
            return;
        }

        savingMachine = true;
        feedbackLabel.setText("Đang lưu thiết bị...");
        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws IOException {
                if (!serverConnection.isConnected() && !serverConnection.connect()) {
                    throw new IOException("Không thể kết nối tới server.");
                }
                JSONObject data = new JSONObject()
                        .put("machineCode", details[0])
                        .put("hostname", details[1])
                        .put("ipAddress", details[2])
                        .put("os", details[3])
                        .put("cpu", details[4])
                        .put("gpu", details[5])
                        .put("ram", details[6])
                        .put("disk", details[7]);
                Message request = new Message(UUID.randomUUID().toString(),
                        CommandType.ADD_MACHINE, null, data);
                return serverConnection.sendRequest(request);
            }

            @Override
            protected void done() {
                savingMachine = false;
                try {
                    Response response = get();
                    if (response == null || !response.isSuccess()) {
                        String message = response == null
                                ? "Server không gửi phản hồi."
                                : response.getMessage();
                        feedbackLabel.setText("Không lưu được thiết bị.");
                        showError(message);
                        return;
                    }
                    feedbackLabel.setText(response.getMessage());
                    loadMachines();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    feedbackLabel.setText("Đã hủy lưu thiết bị.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String message = cause == null ? e.getMessage() : cause.getMessage();
                    feedbackLabel.setText("Không lưu được thiết bị.");
                    showError(message);
                }
            }
        }.execute();
    }

    private String[] promptNewMachine() {
        JTextField machineCodeField = new JTextField(16);
        JTextField hostnameField = new JTextField(16);
        JTextField ipAddressField = new JTextField(getCurrentIpAddress(), 16);
        JTextField osField = new JTextField(16);
        JTextField cpuField = new JTextField(16);
        JTextField gpuField = new JTextField(16);
        JTextField ramField = new JTextField(16);
        JTextField diskField = new JTextField(16);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Mã máy:"));
        form.add(machineCodeField);
        form.add(new JLabel("Tên máy / Host:"));
        form.add(hostnameField);
        form.add(new JLabel("Địa chỉ IP:"));
        form.add(ipAddressField);
        form.add(new JLabel("Hệ điều hành:"));
        form.add(osField);
        form.add(new JLabel("CPU:"));
        form.add(cpuField);
        form.add(new JLabel("GPU:"));
        form.add(gpuField);
        form.add(new JLabel("RAM:"));
        form.add(ramField);
        form.add(new JLabel("Ổ đĩa:"));
        form.add(diskField);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Thêm thiết bị", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String[] {
                machineCodeField.getText().trim(),
                hostnameField.getText().trim(),
                ipAddressField.getText().trim(),
                osField.getText().trim(),
                cpuField.getText().trim(),
                gpuField.getText().trim(),
                ramField.getText().trim(),
                diskField.getText().trim()
        };
    }

    private String getCurrentIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (IOException e) {
            return "127.0.0.1";
        }
    }

    private boolean validateNewMachineDetails(String[] details) {
        for (String detail : details) {
            if (detail.isBlank()) {
                showError("Vui lòng nhập đầy đủ thông tin thiết bị.");
                return false;
            }
        }
        return true;
    }

    private void editMachine() {
        if (savingMachine) {
            return;
        }
        int viewRow = machineTable.getSelectedRow();
        if (viewRow < 0) {
            showError("Vui lòng chọn máy cần sửa.");
            return;
        }
        int selectedRow = machineTable.convertRowIndexToModel(viewRow);

        String[] details = promptMachineDetails(
                "Sửa máy",
                machineTableModel.getValueAt(selectedRow, 0).toString(),
                machineTableModel.getValueAt(selectedRow, 1).toString(),
                machineTableModel.getValueAt(selectedRow, 3).toString(),
                machineTableModel.getValueAt(selectedRow, 4).toString(),
                machineTableModel.getValueAt(selectedRow, 5).toString(),
                machineTableModel.getValueAt(selectedRow, 6).toString(),
                machineTableModel.getValueAt(selectedRow, 8).toString());
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

        String originalMachineCode = machineTableModel.getValueAt(selectedRow, 0).toString();
        savingMachine = true;
        feedbackLabel.setText("Đang cập nhật thông tin máy...");
        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws IOException {
                if (!serverConnection.isConnected() && !serverConnection.connect()) {
                    throw new IOException("Không thể kết nối tới server.");
                }
                JSONObject data = new JSONObject()
                        .put("originalMachineCode", originalMachineCode)
                        .put("machineCode", details[0])
                        .put("hostname", details[1])
                        .put("os", details[2])
                        .put("cpu", details[3])
                        .put("gpu", details[4])
                        .put("ram", details[5])
                        .put("disk", details[6]);
                Message request = new Message(UUID.randomUUID().toString(),
                        CommandType.UPDATE_MACHINE, null, data);
                return serverConnection.sendRequest(request);
            }

            @Override
            protected void done() {
                savingMachine = false;
                try {
                    Response response = get();
                    if (response == null || !response.isSuccess()) {
                        String message = response == null
                                ? "Server không gửi phản hồi."
                                : response.getMessage();
                        feedbackLabel.setText("Không cập nhật được thông tin máy.");
                        showError(message);
                        return;
                    }
                    feedbackLabel.setText(response.getMessage());
                    loadMachines();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    feedbackLabel.setText("Đã hủy cập nhật thông tin máy.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String message = cause == null ? e.getMessage() : cause.getMessage();
                    feedbackLabel.setText("Không cập nhật được thông tin máy.");
                    showError(message);
                }
            }
        }.execute();
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
        if (updatingMachineStatus) {
            return;
        }
        int viewRow = machineTable.getSelectedRow();
        if (viewRow < 0) {
            showError("Vui lòng chọn máy cần thay đổi trạng thái.");
            return;
        }
        int selectedRow = machineTable.convertRowIndexToModel(viewRow);
        String machineCode = machineTableModel.getValueAt(selectedRow, 0).toString();
        updatingMachineStatus = true;
        feedbackLabel.setText(open ? "Đang bật máy..." : "Đang tắt máy...");

        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws IOException {
                if (!serverConnection.isConnected() && !serverConnection.connect()) {
                    throw new IOException("Không thể kết nối tới server.");
                }
                CommandType command = open ? CommandType.OPEN_MACHINE : CommandType.CLOSE_MACHINE;
                Message request = new Message(UUID.randomUUID().toString(), command, null, machineCode);
                return serverConnection.sendRequest(request);
            }

            @Override
            protected void done() {
                updatingMachineStatus = false;
                try {
                    Response response = get();
                    if (response == null || !response.isSuccess()) {
                        String message = response == null
                                ? "Server không gửi phản hồi."
                                : response.getMessage();
                        feedbackLabel.setText("Không cập nhật được trạng thái máy.");
                        showError(message);
                        return;
                    }
                    feedbackLabel.setText(response.getMessage());
                    loadMachines();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    feedbackLabel.setText("Đã hủy cập nhật trạng thái máy.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String message = cause == null ? e.getMessage() : cause.getMessage();
                    feedbackLabel.setText("Không cập nhật được trạng thái máy.");
                    showError(message);
                }
            }
        }.execute();
    }

    private String[] promptMachineDetails(String title, String machineId, String machineName,
            String os, String cpu, String gpu, String ram, String disk) {
        JTextField machineIdField = new JTextField(machineId, 16);
        JTextField machineNameField = new JTextField(machineName, 16);
        JTextField osField = new JTextField(os, 16);
        JTextField cpuField = new JTextField(cpu, 16);
        JTextField gpuField = new JTextField(gpu, 16);
        JTextField ramField = new JTextField(ram, 16);
        JTextField diskField = new JTextField(disk, 16);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Mã máy:"));
        form.add(machineIdField);
        form.add(new JLabel("Tên máy:"));
        form.add(machineNameField);
        form.add(new JLabel("Hệ điều hành:"));
        form.add(osField);
        form.add(new JLabel("CPU:"));
        form.add(cpuField);
        form.add(new JLabel("GPU:"));
        form.add(gpuField);
        form.add(new JLabel("RAM:"));
        form.add(ramField);
        form.add(new JLabel("Bộ nhớ / Ổ đĩa:"));
        form.add(diskField);

        int result = JOptionPane.showConfirmDialog(
                this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String[] {
            machineIdField.getText().trim(),
            machineNameField.getText().trim(),
            osField.getText().trim(),
            cpuField.getText().trim(),
            gpuField.getText().trim(),
            ramField.getText().trim(),
            diskField.getText().trim()
        };
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
