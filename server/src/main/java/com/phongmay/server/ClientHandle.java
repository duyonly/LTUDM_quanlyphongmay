package com.phongmay.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

import com.phongmay.common.JsonUtil;
import com.phongmay.common.MachineInfo;
import com.phongmay.common.Message;
import com.phongmay.common.Response;
import com.phongmay.server.dao.MachineDao;


public class ClientHandle  implements  Runnable{
    private Socket socket;
    private ClientManager clientManager;
    private PrintWriter writer;
    private  final MachineDao machineDao=new MachineDao();
    public ClientHandle (Socket socket,ClientManager clientManager){
        this.socket=socket;
        this.clientManager=clientManager;
    }
 
    @Override 
    public  void run(){
        String ipAddress=socket.getInetAddress().getHostAddress();
        System.out.println("Đang xử lý client: " + ipAddress);
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
                     MachineInfo machineInfo=JsonUtil.jsonToMachineInfo(message.getData());
                     String machineId=machineInfo.getMachineId();
                     String machineName = machineInfo.getMachineName();
                     String machineIp = machineInfo.getIpAddress();
                     String os = machineInfo.getOs();
                     boolean success= machineDao.connectMachine(machineId, 
                        machineName, machineIp,  os);
                if(success){
                    clientManager.addClient(machineId, this);
                    System.out.println("Máy "+machineId+" đã đăng ký");
                    Response connectResponse=new Response(message.getRequestId(),true,"connect thành công",machineId);
                    writer.println(JsonUtil.responseToJson(connectResponse));
                }else {
        Response response = new Response(
                message.getRequestId(),
                false,
                "Không thể đăng ký máy",
                null);
        writer.println( JsonUtil.responseToJson(response));}
                    break;
                    case HEARTBEAT: 
                    String machineId2=String.valueOf(message.getData());
                    machineDao.updateHeartbeat(machineId2);
                    System.out.println("Máy "+machineId2+" đang online");
                    Response heartResponse=new  Response(message.getRequestId(),true,
                    "Heartbeat OK",
                    machineId2);
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
                "Client đã ngắt kết nối: " + ipAddress);
        e.printStackTrace();
        }
    }
    public  void send(String json){
        writer.println(json);
    }
    
}
