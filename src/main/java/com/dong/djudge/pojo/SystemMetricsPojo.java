package com.dong.djudge.pojo;

import com.alibaba.fastjson2.JSON;
import com.dong.djudge.entity.setting.SystemMetrics;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Setter;

@Data
@TableName("system_metrics")
public class SystemMetricsPojo extends AbstractPojo {


    @TableField("cpu_core_usage")
    private String cpuCoreUsage; // JSON 格式的字符串

    @TableField("SandboxSetting_id")
    private Long sandboxSettingId;

    @TableField("cpu_logical_cores")
    private Integer cpuLogicalCores;

    @TableField("cpu_physical_cores")
    private Integer cpuPhysicalCores;

    @TableField("cpu_total_usage")
    private Double cpuTotalUsage;

    @TableField("disk_read_kbps")
    private Double diskReadKbps;

    @TableField("disk_write_kbps")
    private Double diskWriteKbps;

    @TableField("memory_total_mb")
    private Double memoryTotalMb;

    @TableField("memory_usage_percent")
    private Double memoryUsagePercent;

    @TableField("memory_used_mb")
    private Double memoryUsedMb;

    @TableField("network_download_mbps")
    private Double networkDownloadMbps;

    @TableField("network_upload_mbps")
    private Double networkUploadMbps;
    @Setter
    @TableField(exist = false)
    private String createTimeFormatted;
    @Setter
    @TableField(exist = false)
    private String updateTimeFormatted;

    public SystemMetricsPojo(SystemMetrics source, Long sandboxSettingId) {
        this.cpuLogicalCores = source.getCpuLogicalCores();
        this.sandboxSettingId = sandboxSettingId;
        this.cpuPhysicalCores = source.getCpuPhysicalCores();
        this.cpuTotalUsage = source.getCpuTotalUsage();
        this.diskReadKbps = source.getDiskReadKbps();
        this.diskWriteKbps = source.getDiskWriteKbps();
        this.memoryTotalMb = source.getMemoryTotalMb();
        this.memoryUsagePercent = source.getMemoryUsagePercent();
        this.memoryUsedMb = source.getMemoryUsedMb();
        this.networkDownloadMbps = source.getNetworkDownloadMbps();
        this.networkUploadMbps = source.getNetworkUploadMbps();

        // 将 List<Double> 转换为 JSON 字符串
        if (source.getCpuCoreUsage() != null) {
            this.cpuCoreUsage = JSON.toJSONString(source.getCpuCoreUsage());
        }
    }

    public SystemMetricsPojo() {
    }
}

