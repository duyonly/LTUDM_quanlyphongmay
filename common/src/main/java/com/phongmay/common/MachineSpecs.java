package com.phongmay.common;

public class MachineSpecs {
    private String cpu;
    private String ram;
    private String gpu;
    private String disk;

    public MachineSpecs() {
    }

    public MachineSpecs(String cpu, String ram, String gpu, String disk) {
        this.cpu = cpu;
        this.ram = ram;
        this.gpu = gpu;
        this.disk = disk;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getGpu() {
        return gpu;
    }

    public void setGpu(String gpu) {
        this.gpu = gpu;
    }

    public String getDisk() {
        return disk;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }
}
