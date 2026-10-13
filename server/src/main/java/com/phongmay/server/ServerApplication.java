package com.phongmay.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import com.phongmay.common.CommandType;
import com.phongmay.common.JsonUtil;
import com.phongmay.common.Message;
import com.phongmay.server.config.DatabaseConfig;

public class ServerApplication {
    //luồng server gửi tin nhắn xuống client sendMessage
    @SuppressWarnings("resource")
    private static void serverSMClient(ClientManager clientManager){
        Thread t=new Thread(()->{
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Nhập ID máy: ");
                String machineId = scanner.nextLine();

                System.out.print("Nhập nội dung: ");
                String content = scanner.nextLine();
                Message message=new Message("req-server-001",CommandType.MESSAGE , machineId, content);
                clientManager.sendToClient(machineId, JsonUtil.messageToJson(message));
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    public static  void main(String[] args){
        
        DatabaseConfig.testConnection();
        int port=12345;
        ClientManager clientManager=new ClientManager();
        // Thread nhập lệnh từ Server
        serverSMClient(clientManager);
        try(ServerSocket server=new ServerSocket(port)) {
            System.out.println("server đang chờ port: "+port);
            System.out.println("server đang chờ client..");
            
            while (true) {
                Socket socket = server.accept();
                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
                ClientHandle clientHandle=new ClientHandle(socket, clientManager);
                Thread thread=new Thread(clientHandle);
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
