package com.phongmay.machine;

import com.google.gson.Gson;
import com.phongmay.common.CommandType;
import com.phongmay.common.Message;
import com.phongmay.common.Response;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Protocol: mỗi message là 1 dòng JSON (kết thúc bằng \n), UTF-8.
 *  Machine -> Server : Message (CONNECT, HEARTBEAT)
 *  Server  -> Machine: Message (GET_SYSTEM_INFO, GET_PROCESSES, KILL_PROCESS, SCREENSHOT, ...)
 *  Machine -> Server : Response (cùng requestId với Message được hỏi)
 */
public class MachineClient {
    private static final Gson GSON = new Gson();
    private static final String SERVER = "SERVER";
    private static final int HEARTBEAT_SECONDS = 5;
    private static final int RECONNECT_MS = 3000;

    private final String host;
    private final int port;
    private final String machineId;
    private final SystemInfoService systemInfo = new SystemInfoService();
    private final ProcessManager processManager = new ProcessManager();
    private final ScreenshotService screenshotService = new ScreenshotService();

    private volatile Socket socket;
    private volatile boolean running = true;
    private BufferedWriter out;

    public MachineClient(String host, int port, String machineId) {
        this.host = host;
        this.port = port;
        this.machineId = machineId;
    }

    public void runForever() {
        while (running) {
            try {
                runOnce();
            } catch (IOException e) {
                if (running) System.out.println("Connection lost: " + e.getMessage());
            }
            if (!running) break;
            System.out.println("Reconnecting in " + RECONNECT_MS / 1000 + "s...");
            try {
                Thread.sleep(RECONNECT_MS);
            } catch (InterruptedException e) {
                return;
            }
        }
        System.out.println("Machine client stopped");
    }

    /** Stop the client: close the connection and end runForever(). */
    public void stop() {
        running = false;
        Socket s = socket;
        if (s != null) {
            try {
                s.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void runOnce() throws IOException {
        ScheduledExecutorService heartbeat = null;
        try (Socket s = new Socket(host, port)) {
            this.socket = s;
            out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));

            // Đăng ký máy
            send(new Message(newId(), CommandType.CONNECT, SERVER, systemInfo.collect(machineId)));
            System.out.println(machineId + " connected to " + host + ":" + port);

            heartbeat = Executors.newSingleThreadScheduledExecutor();
            heartbeat.scheduleAtFixedRate(this::sendHeartbeat, HEARTBEAT_SECONDS, HEARTBEAT_SECONDS, TimeUnit.SECONDS);

            String line;
            while ((line = in.readLine()) != null) {
                handle(line);
            }
            throw new IOException("Server closed the connection");
        } finally {
            if (heartbeat != null) heartbeat.shutdownNow();
        }
    }

    private void sendHeartbeat() {
        try {
            send(new Message(newId(), CommandType.HEARTBEAT, SERVER, machineId));
        } catch (IOException e) {
            try {
                socket.close(); // làm vòng đọc thoát để reconnect
            } catch (IOException ignored) {
            }
        }
    }

    private void handle(String line) {
        Message msg;
        try {
            msg = GSON.fromJson(line, Message.class);
        } catch (Exception e) {
            System.out.println("Invalid JSON: " + e.getMessage());
            return;
        }
        if (msg == null || msg.getCommand() == null) return;

        Response res;
        try {
            res = switch (msg.getCommand()) {
                case GET_SYSTEM_INFO -> ok(msg, systemInfo.collect(machineId));
                case GET_PROCESSES -> ok(msg, processManager.list());
                case KILL_PROCESS -> kill(msg);
                case SCREENSHOT -> screenshot(msg);
                default -> new Response(msg.getRequestId(), false,
                        "Unsupported command " + msg.getCommand(), null);
            };
        } catch (Exception e) {
            res = new Response(msg.getRequestId(), false, "Error: " + e.getMessage(), null);
        }

        try {
            send(res);
        } catch (IOException e) {
            System.out.println("Failed to send response: " + e.getMessage());
        }
    }

    private Response kill(Message msg) {
        long pid = extractPid(msg.getData());
        String error = processManager.kill(pid);
        return error == null
                ? new Response(msg.getRequestId(), true, "Killed PID " + pid, null)
                : new Response(msg.getRequestId(), false, error, null);
    }

    /** data (tùy chọn): {"maxWidth":640,"quality":0.5}. Mặc định 1280 và 0.6. */
    private Response screenshot(Message msg) throws Exception {
        int maxWidth = 1280;
        float quality = 0.6f;
        if (msg.getData() instanceof Map<?, ?> m) {
            if (m.get("maxWidth") instanceof Number n) maxWidth = n.intValue();
            if (m.get("quality") instanceof Number n) quality = n.floatValue();
        }
        return ok(msg, screenshotService.capture(maxWidth, quality));
    }

    /** data có thể là số 1234 hoặc {"pid":1234} (Gson đọc số thành Double). */
    private static long extractPid(Object data) {
        if (data instanceof Number n) return n.longValue();
        if (data instanceof Map<?, ?> m && m.get("pid") instanceof Number n) return n.longValue();
        return -1;
    }

    private static Response ok(Message req, Object data) {
        return new Response(req.getRequestId(), true, "OK", data);
    }

    private synchronized void send(Object obj) throws IOException {
        out.write(GSON.toJson(obj));
        out.newLine();
        out.flush();
    }

    private static String newId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}