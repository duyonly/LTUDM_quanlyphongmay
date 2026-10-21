package com.phongmay.common;

public class MachineInfo {
    private String machineId;
    private String machineName;
    private String ipAddress;
    private String os;
    private double cpuUsage;
    private double ramUsage;
    private double gpuUsage;
    private double diskUsage;
    private boolean online;
    private MachineSpecs specs;
    public MachineInfo() {
    }

    public MachineInfo(String machineId, String machineName, String ipAddress, String os,
                       double cpuUsage, double ramUsage,double gpuUsage,double diskUsage, boolean online,MachineSpecs specs) {
        this.machineId = machineId;
        this.machineName = machineName;
        this.ipAddress = ipAddress;
        this.os = os;
        this.cpuUsage = cpuUsage;
        this.ramUsage = ramUsage;
        this.gpuUsage=gpuUsage;
        this.diskUsage = diskUsage;
        this.online = online;
        this.specs=specs;
    }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
    public double getRamUsage() { return ramUsage; }
    public void setRamUsage(double ramUsage) { this.ramUsage = ramUsage; }
    public double getDiskUsage() { return diskUsage; }
    public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
    public MachineSpecs getSpecs() {return specs; }
    public void setSpecs(MachineSpecs specs) {this.specs = specs;}
    public double getGpuUsage() {
        return gpuUsage;
    }

    public void setGpuUsage(double gpuUsage) {
        this.gpuUsage = gpuUsage;
    }
}