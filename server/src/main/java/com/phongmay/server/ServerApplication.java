package com.phongmay.server;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerApplication {
    public void main(String[] args){
        int port=12345;
        try(ServerSocket server=new ServerSocket(port)) {
            System.out.println("server đang chờ port: "+port);
            System.out.println("server đang chờ client..");
            while (true) {
                Socket socket=server.accept();
                System.out.println("client connected: "+ socket.getInetAddress().getHostAddress());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
