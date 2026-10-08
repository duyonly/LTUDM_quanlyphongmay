package com.phongmay.common;

import org.json.JSONObject;

public class JsonUtil {
    //chuyển message thành json
    public static String messageToJson(Message message){
        JSONObject json=new JSONObject();
        json.put("requestId", message.getRequestId());
        json.put("command", message.getCommand().name());
        json.put("target", message.getTarget());
        json.put("data", message.getData());
        return json.toString();
    }
    //chuyển từ json sang message
    public static  Message jsonToMessage(String jsonString){
        JSONObject json=new JSONObject(jsonString);
        String requestId=json.getString("requestId");
        CommandType command=CommandType.valueOf(json.getString("command"));
        String target=json.optString("target",null);
        Object data=json.opt("data");
        return new Message(requestId, command, target, data);
    }
    public static String responseToJson(Response response){
        JSONObject json=new  JSONObject();
        json.put("requestId", response.getRequestId());
        json.put("success", response.isSuccess());
        json.put("message", response.getMessage());
        json.put("data", response.getData());
        return json.toString();
    }
    public static Response jsonToResponse(String jsonString){
        JSONObject json=new JSONObject(jsonString);
        String requestId =json.getString("requestId");
        boolean success=json.getBoolean("success");
        String message=json.getString("message");
        Object data=json.opt("data");
        return new Response(
            requestId,
            success,
            message,
            data
    );
    }
    public static MachineInfo jsonToMachineInfo(Object  data){
        JSONObject json=(JSONObject) data;
        MachineInfo info= new MachineInfo();
        info.setMachineId(json.getString("machineId"));
    info.setMachineName(json.getString("machineName"));
    info.setIpAddress(json.getString("ipAddress"));
    info.setOs(json.getString("os"));
    info.setCpuUsage(json.getDouble("cpuUsage"));
    info.setRamUsage(json.getDouble("ramUsage"));
    info.setDiskUsage(json.getDouble("diskUsage"));
    info.setOnline(json.getBoolean("online"));
    return info;
    }

  
}
