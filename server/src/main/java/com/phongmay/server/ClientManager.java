package com.phongmay.server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {
    private final Map<String, ClientHandle> clients=new ConcurrentHashMap<>();
    public void addClient(String machineId,ClientHandle client){
        clients.put(machineId, client);
        System.out.println("đã thêm máy: "+machineId);

    }
    public void removeClient(String machineId,ClientHandle client){
        clients.remove(machineId);
        System.out.println("đã xóa máy: "+machineId);
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
            System.out.println(
                "Không tìm thấy máy: " + machineId
        );
        }
    }
    public Set<String> getClientId(){
        return clients.keySet();
    }
}
