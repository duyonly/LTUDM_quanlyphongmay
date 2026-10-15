package com.phongmay.common;

public class MachineStatus {
    private String machineId;
    private  String machineName;
    private  String ipAddress;
    private  String os;
    private  String status;
    public MachineStatus(){}
    public MachineStatus(String machineId,String machineName,String ipAddress,String os,String status){
        this.machineId=machineId;
        this.machineName=machineName;
        this.ipAddress=ipAddress;
        this.os=os;
        this.status=status;
    }
    public String getMachineId(){
        return machineId;
    }
    public void setMachineId(String machineId) { this.machineId = machineId; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
