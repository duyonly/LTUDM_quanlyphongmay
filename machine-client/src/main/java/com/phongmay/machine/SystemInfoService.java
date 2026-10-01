package com.phongmay.machine;

import com.phongmay.common.MachineInfo;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.io.File;
import java.net.InetAddress;

public class SystemInfoService {
    private final SystemInfo si = new SystemInfo();
    private final CentralProcessor cpu = si.getHardware().getProcessor();
    private final GlobalMemory memory = si.getHardware().getMemory();
    private long[] prevTicks = cpu.getSystemCpuLoadTicks();

    public synchronized MachineInfo collect(String machineId) {
        double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = cpu.getSystemCpuLoadTicks();

        long total = memory.getTotal();
        double ram = total == 0 ? 0 : (total - memory.getAvailable()) * 100.0 / total;

        String hostname = "unknown";
        String ip = "unknown";
        try {
            InetAddress local = InetAddress.getLocalHost();
            hostname = local.getHostName();
            ip = local.getHostAddress();
        } catch (Exception ignored) {
        }

        MachineInfo info = new MachineInfo(machineId, hostname, ip,
                System.getProperty("os.name"), round(cpuLoad), round(ram), true);
        info.setDiskUsage(round(diskUsage()));
        return info;
    }

    private double diskUsage() {
        long total = 0, free = 0;
        for (File root : File.listRoots()) {
            total += root.getTotalSpace();
            free += root.getUsableSpace();
        }
        return total == 0 ? 0 : (total - free) * 100.0 / total;
    }

    private static double round(double v) {
        return Double.isNaN(v) ? 0 : Math.round(v * 10) / 10.0;
    }
}
