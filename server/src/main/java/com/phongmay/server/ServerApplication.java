package com.phongmay.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import com.phongmay.common.CommandType;
import com.phongmay.common.JsonUtil;
import com.phongmay.common.Message;

public class ServerApplication {
    public static  void main(String[] args){
        int port=12345;
     
        try(ServerSocket server=new ServerSocket(port)) {
            System.out.println("server đang chờ port: "+port);
            System.out.println("server đang chờ client..");
            ClientManager clientManager=new ClientManager();
            // Thread nhập lệnh từ Server
        @SuppressWarnings("resource")
        Thread consoleThread = new Thread(() -> {

            Scanner scanner = new Scanner(System.in);

            while (true) {

                System.out.print("Nhập machineId: ");
                String machineId = scanner.nextLine();

                System.out.print("Nhập nội dung: ");
                String content = scanner.nextLine();

                Message message = new Message(
                        "req-server-001",
                        CommandType.MESSAGE,
                        machineId,
                        content
                );

                String json =
                        JsonUtil.messageToJson(message);

                clientManager.sendToClient(
                        machineId,
                        json
                );
            }
        });

        consoleThread.start();
            while (true) {
                Socket socket=server.accept();
                System.out.println("client connected: "+ socket.getInetAddress().getHostAddress());
                ClientHandle clientHandle=new ClientHandle(socket,clientManager);
                Thread thread=new Thread(clientHandle);
                thread.start();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
