package com.phongmay.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import com.phongmay.common.JsonUtil;
import com.phongmay.common.Message;
import com.phongmay.common.Response;


public class ClientHandle  implements  Runnable{
    private Socket socket;
    private ClientManager clientManager;
    private PrintWriter writer;
    public ClientHandle (Socket socket,ClientManager clientManager){
        this.socket=socket;
        this.clientManager=clientManager;
    }
 
    @Override 
    public  void run(){
        String ip=socket.getInetAddress().getHostAddress();
        System.out.println("Đang xử lý client: " + ip);
        try {
            BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
             writer=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
            String json;
            while ((json=reader.readLine())!=null) {
                System.out.println(
                    "JSON nhận được: " + json
            );
            Message message=JsonUtil.jsonToMessage(json);
            System.out.println("Command: "+message.getCommand());
            System.out.println("Target: "+message.getTarget());
            switch (message.getCommand()) {
                case CONNECT:
                     // ClientManager clientManager=new ClientManager();
                clientManager.addClient(message.getTarget(), this);
                System.out.println("Máy "+message.getTarget()+" đã đăng ký");
                Response connectResponse=new Response(message.getRequestId(),true,"connect thành công",null);
                writer.println(JsonUtil.responseToJson(connectResponse));
                    break;
                    case HEARTBEAT: 
                    System.out.println("Máy "+message.getTarget()+" đang online");
                    Response heartResponse=new  Response(message.getRequestId(),true,
                    "Heartbeat OK",
                    null);
                    String responseJson=JsonUtil.responseToJson(heartResponse);
                    writer.println(responseJson);
                    break;
                    case GET_CLIENTS:
                        String clients=clientManager.getClientId().toString();
                        Response getClientResponse=new Response( message.getRequestId(),true,
                        "Danh sách máy đang kết nối",clients);
                        writer.println(JsonUtil.responseToJson(getClientResponse));
                        break;
            
                default:
                    Response response = new Response(message.getRequestId(),false,"Command chưa được hỗ trợ",null);
                writer.println(JsonUtil.responseToJson(response));
                    break;
            }
            }
        } catch (Exception e) {
            System.out.println(
                "Client đã ngắt kết nối: " + ip
        );
        e.printStackTrace();
        }
    }
    public  void send(String json){
        writer.println(json);
    }
    
}
