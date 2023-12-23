package com.dong.djudge.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 初始化SqlLite数据库
 */
@Component
@Slf4j
public class SqliteConfig {
    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 初始化SqlLite数据库
     */
    @PostConstruct
    public void init() {
        jdbcTemplate.update(createFile());
        jdbcTemplate.update(createStandardCode());
        jdbcTemplate.update(createSandboxSetting());
        jdbcTemplate.update(createSandboxRun());
        jdbcTemplate.update(createSetting());
        jdbcTemplate.update(createSystemMetricsTable());
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM test_group");
        log.info(Arrays.toString(maps.toArray()));
    }

    private String createFile() {
        return "CREATE TABLE IF NOT EXISTS test_group (" +
                "    id BIGINT PRIMARY KEY," +
                "    test_group_id CHAR(12) UNIQUE," +
                "    create_time TIMESTAMP DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))," +
                "    update_time TIMESTAMP DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))" +
                ");";
    }

    private String createStandardCode() {
        return """
                CREATE TABLE IF NOT EXISTS standard_code (
                    id BIGINT PRIMARY KEY,
                    code_id VARCHAR(12) NOT NULL,
                    test_group_id VARCHAR(12) NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (code_id)
                );
                """;
    }


    private String createSandboxSetting() {
        return """
                CREATE TABLE IF NOT EXISTS sandbox_setting (
                    id BIGINT PRIMARY KEY,
                    base_url VARCHAR(64) NOT NULL,
                    name VARCHAR(64),
                    state INT NOT NULL,
                    level INT NOT NULL,
                    frequency INT NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (base_url)
                );
                """;
    }

    private String createSandboxRun() {
        return """
                CREATE TABLE IF NOT EXISTS sandbox_run (
                    id BIGINT PRIMARY KEY,
                    file_id VARCHAR(64) NOT NULL,
                    base_url VARCHAR(64) NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (file_id)
                );
                """;
    }

    private String createSetting() {
        return """
                CREATE TABLE IF NOT EXISTS setting (
                    id BIGINT PRIMARY KEY,
                    key VARCHAR(64) NOT NULL,
                    value VARCHAR(8192) NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (key)
                );
                """;
    }

    private String createSystemMetricsTable() {
        return """
                    CREATE TABLE IF NOT EXISTS system_metrics (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          SandboxSetting_id BIGINT NOT NULL,
                          cpu_core_usage VARCHAR(255), -- 存储为 JSON 格式的字符串
                          cpu_logical_cores INT,
                          cpu_physical_cores INT,
                          cpu_total_usage DOUBLE,
                          disk_read_kbps DOUBLE,
                          disk_write_kbps DOUBLE,
                          memory_total_mb DOUBLE,
                          memory_usage_percent DOUBLE,
                          memory_used_mb DOUBLE,
                          network_download_mbps DOUBLE,
                          network_upload_mbps DOUBLE,
                          create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                      );
                                        
                """;
    }

}
