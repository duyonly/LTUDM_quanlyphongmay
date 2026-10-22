package com.phongmay.machine;

import com.phongmay.common.MachineInfo;
import com.phongmay.common.MachineSpecs;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SystemInfoService {
    private final SystemInfo si = new SystemInfo();
    private final CentralProcessor cpu = si.getHardware().getProcessor();
    private final GlobalMemory memory = si.getHardware().getMemory();
    private long[] prevTicks = cpu.getSystemCpuLoadTicks();

    public synchronized MachineInfo collect(String machineId) {
        double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = cpu.getSystemCpuLoadTicks();

        long totalRam = memory.getTotal();
        double ramUsage = totalRam == 0
                ? 0
                : (totalRam - memory.getAvailable()) * 100.0 / totalRam;

        String hostname = "unknown";
        String ip = "unknown";
        try {
            InetAddress local = InetAddress.getLocalHost();
            hostname = local.getHostName();
            ip = local.getHostAddress();
        } catch (Exception ignored) {
        }

        MachineInfo info = new MachineInfo();
        info.setMachineId(machineId);
        info.setMachineName(hostname);
        info.setIpAddress(ip);
        info.setOs(System.getProperty("os.name"));
        info.setCpuUsage(round(cpuLoad));
        info.setRamUsage(round(ramUsage));
        info.setGpuUsage(0.0); // OSHI 6.6.5 chua ho tro GPU utilization
        info.setDiskUsage(round(diskUsage()));
        info.setOnline(true);
        info.setSpecs(collectSpecs());
        return info;
    }

    /**
     * Cau hinh phan cung tinh, gui kem khi Machine CONNECT/GET_SYSTEM_INFO.
     * GPU lay bang OSHI: ten card + VRAM. Neu co nhieu GPU thi noi bang " | ".
     */
    private MachineSpecs collectSpecs() {
        MachineSpecs specs = new MachineSpecs();
        specs.setCpu(cpuName());
        specs.setRam(formatBytes(memory.getTotal()));
        specs.setGpu(gpuInfo());
        specs.setDisk(formatBytes(totalDiskBytes()));
        return specs;
    }

    private String cpuName() {
        String name = cpu.getProcessorIdentifier().getName();
        return name == null || name.isBlank() ? "Unknown CPU" : name.trim();
    }

    private String gpuInfo() {
        try {
            List<GraphicsCard> cards = si.getHardware().getGraphicsCards();
            if (cards == null || cards.isEmpty()) {
                return "Unknown GPU";
            }

            return cards.stream()
                    .map(this::formatGpu)
                    .collect(Collectors.joining(" | "));
        } catch (Exception e) {
            return "Unknown GPU";
        }
    }

    private String formatGpu(GraphicsCard card) {
        String name = card.getName();
        if (name == null || name.isBlank()) {
            name = "Unknown GPU";
        }

        long vram = card.getVRam();
        if (vram > 0) {
            return name.trim() + " (VRAM " + formatBytes(vram) + ")";
        }
        return name.trim();
    }

    private double diskUsage() {
        long total = 0;
        long free = 0;
        for (File root : File.listRoots()) {
            total += root.getTotalSpace();
            free += root.getUsableSpace();
        }
        return total == 0 ? 0 : (total - free) * 100.0 / total;
    }

    private long totalDiskBytes() {
        long total = 0;
        for (File root : File.listRoots()) {
            total += root.getTotalSpace();
        }
        return total;
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "Unknown";
        }

        double gb = bytes / (1024.0 * 1024.0 * 1024.0);
        if (gb >= 1) {
            return String.format(Locale.US, "%.1f GB", gb);
        }

        double mb = bytes / (1024.0 * 1024.0);
        return String.format(Locale.US, "%.0f MB", mb);
    }

    private static double round(double v) {
        return Double.isNaN(v) ? 0 : Math.round(v * 10) / 10.0;
    }
}
