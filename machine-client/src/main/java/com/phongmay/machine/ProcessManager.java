package com.phongmay.machine;

import com.phongmay.common.ProcessInfo;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.List;

public class ProcessManager {
    private final OperatingSystem os = new SystemInfo().getOperatingSystem();

    /** Top 100 process dùng RAM nhiều nhất. CPU là trung bình từ lúc process khởi động. */
    public List<ProcessInfo> list() {
        List<ProcessInfo> result = new ArrayList<>();
        for (OSProcess p : os.getProcesses(OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                OperatingSystem.ProcessSorting.RSS_DESC, 100)) {
            double cpu = p.getProcessCpuLoadCumulative() * 100;
            result.add(new ProcessInfo(
                    p.getProcessID(),
                    p.getName(),
                    p.getResidentSetSize() / (1024 * 1024),
                    Double.isNaN(cpu) ? 0 : Math.round(cpu * 10) / 10.0));
        }
        return result;
    }

    /** @return null nếu OK, ngược lại là thông báo lỗi */
    public String kill(long pid) {
        if (pid <= 0) return "Invalid PID";
        if (pid == ProcessHandle.current().pid()) return "Cannot kill the Machine Client itself";
        return ProcessHandle.of(pid)
                .map(h -> h.destroyForcibly() ? (String) null : "No permission to kill PID " + pid)
                .orElse("PID not found: " + pid);
    }
}