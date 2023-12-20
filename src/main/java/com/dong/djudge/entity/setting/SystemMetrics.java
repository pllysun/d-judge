package com.dong.djudge.entity.setting;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import java.util.List;

@Data
public class SystemMetrics {
    @JSONField(name = "cpu_core_usage")
    private List<Double> cpuCoreUsage;

    @JSONField(name = "cpu_logical_cores")
    private Integer cpuLogicalCores;

    @JSONField(name = "cpu_physical_cores")
    private Integer cpuPhysicalCores;

    @JSONField(name = "cpu_total_usage")
    private Double cpuTotalUsage;

    @JSONField(name = "disk_read_kbps")
    private Double diskReadKbps;

    @JSONField(name = "disk_write_kbps")
    private Double diskWriteKbps;

    @JSONField(name = "memory_total_mb")
    private Double memoryTotalMb;

    @JSONField(name = "memory_usage_percent")
    private Double memoryUsagePercent;

    @JSONField(name = "memory_used_mb")
    private Double memoryUsedMb;

    @JSONField(name = "network_download_mbps")
    private Double networkDownloadMbps;

    @JSONField(name = "network_upload_mbps")
    private Double networkUploadMbps;
}
