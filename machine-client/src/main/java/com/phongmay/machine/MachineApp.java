package com.phongmay.machine;

import java.util.Scanner;

public class MachineApp {
    // Chạy: MachineApp [host] [port] [machineId]. Gõ "quit" trong console để thoát.
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 12345;
        String machineId = args.length > 2 ? args[2] : "PC01";

        MachineClient client = new MachineClient(host, port, machineId);

        Thread console = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                String cmd = sc.nextLine().trim();
                if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit")) {
                    client.stop();
                    return;
                }
            }
        });
        console.setDaemon(true);
        console.start();

        client.runForever();
        System.out.println("Type 'quit' to exit.");
    }
}