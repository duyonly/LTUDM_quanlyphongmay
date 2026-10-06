package com.phongmay.machine.dev;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.phongmay.common.CommandType;
import com.phongmay.common.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

/** Chỉ để test Machine Client. Gõ: info | ps | kill <pid> | shot [maxWidth] | quit. Xóa khi có server thật. */
public class FakeServer {
    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        try (ServerSocket ss = new ServerSocket(12345)) {
            System.out.println("FakeServer waiting on port 12345...");
            Socket s = ss.accept();
            System.out.println("Machine connected");
            System.out.println("Commands: info | ps | kill <pid> | shot [maxWidth] | quit");
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));

            Thread reader = new Thread(() -> {
                try {
                    String l;
                    while ((l = in.readLine()) != null) print(l);
                    System.out.println("Machine disconnected");
                } catch (IOException ignored) {
                }
            });
            reader.setDaemon(true);
            reader.start();

            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().trim().split("\\s+");
                if (p[0].equals("quit") || p[0].equals("exit")) break;
                Message m = switch (p[0]) {
                    case "info" -> new Message("t1", CommandType.GET_SYSTEM_INFO, "PC01", null);
                    case "ps" -> new Message("t2", CommandType.GET_PROCESSES, "PC01", null);
                    case "kill" -> new Message("t3", CommandType.KILL_PROCESS, "PC01", Long.parseLong(p[1]));
                    case "shot" -> new Message("t4", CommandType.SCREENSHOT, "PC01",
                            Map.of("maxWidth", p.length > 1 ? Integer.parseInt(p[1]) : 1280, "quality", 0.6));
                    default -> null;
                };
                if (m != null) {
                    out.write(gson.toJson(m));
                    out.newLine();
                    out.flush();
                }
            }
            s.close();
            System.out.println("FakeServer stopped");
        }
    }

    private static void print(String line) {
        try {
            JsonObject o = JsonParser.parseString(line).getAsJsonObject();

            if (o.has("command")) { // Message từ Machine: CONNECT / HEARTBEAT
                String cmd = o.get("command").getAsString();
                JsonElement data = o.get("data");
                if (cmd.equals("HEARTBEAT")) {
                    System.out.println("[HEARTBEAT] " + data.getAsString());
                } else {
                    System.out.println("[" + cmd + "]");
                    printData(data);
                }
                return;
            }

            // Response từ Machine
            boolean ok = o.get("success").getAsBoolean();
            System.out.println("== Response " + o.get("requestId").getAsString()
                    + " | " + (ok ? "OK" : "ERROR") + " | " + o.get("message").getAsString());
            printData(o.get("data"));
        } catch (Exception e) {
            System.out.println("<< " + line);
        }
    }

    private static void printData(JsonElement data) {
        if (data == null || data.isJsonNull()) return;

        if (data.isJsonArray()) { // danh sách process
            System.out.printf("%-8s %-45s %10s %8s%n", "PID", "NAME", "RAM(MB)", "CPU(%)");
            System.out.println("-".repeat(75));
            for (JsonElement e : data.getAsJsonArray()) {
                JsonObject p = e.getAsJsonObject();
                String name = p.get("name").getAsString();
                if (name.length() > 45) name = name.substring(0, 42) + "...";
                System.out.printf("%-8d %-45s %10d %8.1f%n",
                        p.get("pid").getAsInt(), name,
                        p.get("memoryMb").getAsLong(), p.get("cpuPercent").getAsDouble());
            }
            System.out.println("Total: " + data.getAsJsonArray().size() + " process\n");
        } else if (data.isJsonObject()) { // MachineInfo hoặc Screenshot
            for (Map.Entry<String, JsonElement> en : data.getAsJsonObject().entrySet()) {
                if (en.getKey().equals("image")) {
                    saveImage(en.getValue().getAsString());
                } else {
                    System.out.printf("  %-12s %s%n", en.getKey(), en.getValue().getAsString());
                }
            }
            System.out.println();
        }
    }

    private static void saveImage(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            Path file = Path.of("shot.jpg").toAbsolutePath();
            Files.write(file, bytes);
            System.out.printf("  %-12s %d KB -> saved to %s%n", "image", bytes.length / 1024, file);
        } catch (Exception e) {
            System.out.println("  image        failed to save file: " + e.getMessage());
        }
    }
}