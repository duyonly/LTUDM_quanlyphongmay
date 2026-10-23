package com.phongmay.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import com.phongmay.common.CommandType;
import com.phongmay.common.JsonUtil;
import com.phongmay.common.Message;
import com.phongmay.server.config.DatabaseConfig;
import com.phongmay.server.dao.MachineDao;

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
    //quét xem máy offline lấu quá sẽ chuyển về offline
    private static void chuyenOffline(){
        MachineDao machineDao=new MachineDao();
        Thread thread=new Thread(()->{
            while (true) {
                try{
                    Thread.sleep(5000);
                    int n=machineDao.changeStatus(15);
                    if(n>0){
                        System.out.println("Đã chuyển " + n + " máy sang OFFLINE");
                    }
                }catch(InterruptedException e){
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    public static  void main(String[] args){
        DatabaseConfig.testConnection();
        int port=12345;
        ClientManager clientManager=new ClientManager();
       
        try(ServerSocket server=new ServerSocket(port)) {
            System.out.println("server đang chờ port: "+port);
            System.out.println("server đang chờ client..");
             //chuyển trạng thái
        chuyenOffline();
        // Thread nhập lệnh từ Server
        serverSMClient(clientManager);
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
