    package com.phongmay.server;

    import java.util.Map;
    import java.util.Set;
    import java.util.concurrent.ConcurrentHashMap;

import com.phongmay.common.JsonUtil;
import com.phongmay.common.Response;

    public class ClientManager {
        private final Map<String, ClientHandle> clients=new ConcurrentHashMap<>();
        private final Map<String, ClientHandle> pendingRequest=new ConcurrentHashMap<>();
        public void addClient(String machineId,ClientHandle client){
            clients.put(machineId, client);
            System.out.println("đã thêm máy: "+machineId);

        }

        public void removeClient(String machineId,ClientHandle client){
            if (clients.remove(machineId, client)) {
                System.out.println("đã xóa máy: " + machineId);
            }
        }
        public ClientHandle getClient(String machineId){
            return clients.get(machineId);
        }
        public Map<String, ClientHandle> getClients(){
            return clients;
        }
        public void sendToClient(String machineId, String json){
            ClientHandle client=clients.get(machineId);
            if (client!=null) {
                client.send(json);
            }
            else{
                System.out.println("Không tìm thấy máy: " + machineId);
            }
        }
        public Set<String> getClientId(){
            return clients.keySet();
        }
        public void adminRequest(String request,ClientHandle admin){
            pendingRequest.put(request, admin);
        }
        //hoàn thành yêu cầu của admin gửi
        public void completeRequest(Response response){
            String requestId= response.getRequestId();
            ClientHandle admin=pendingRequest.get(requestId);
            pendingRequest.remove(requestId);
            if(admin!=null){
                admin.send(JsonUtil.responseToJson(response));
            }
            else{
                System.out.println("không tìm thấy admin đang chờ nào");
            }
            
        }
        //xóa những yêu cầu của admin khi admin đóng kết nối
        public void removePendingRequest(ClientHandle admin){
            for(String requestId : pendingRequest.keySet()){
                ClientHandle adminCho=pendingRequest.get(requestId);
                if(adminCho==admin){
                    pendingRequest.values().removeIf(h -> h == admin);
                }
            }
        }
    }
