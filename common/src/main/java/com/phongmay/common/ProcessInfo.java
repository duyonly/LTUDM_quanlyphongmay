package com.phongmay.common;

public class ProcessInfo {
    private int pid;
    private String name;
    private long memoryMb;
    private double cpuPercent;

    public ProcessInfo() {
    }

    public ProcessInfo(int pid, String name, long memoryMb, double cpuPercent) {
        this.pid = pid;
        this.name = name;
        this.memoryMb = memoryMb;
        this.cpuPercent = cpuPercent;
    }

    public int getPid() { return pid; }
    public void setPid(int pid) { this.pid = pid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getMemoryMb() { return memoryMb; }
    public void setMemoryMb(long memoryMb) { this.memoryMb = memoryMb; }
    public double getCpuPercent() { return cpuPercent; }
    public void setCpuPercent(double cpuPercent) { this.cpuPercent = cpuPercent; }
}