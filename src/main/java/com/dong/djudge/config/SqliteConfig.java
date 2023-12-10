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


    private String createSandboxSetting(){
        return """
                CREATE TABLE IF NOT EXISTS sandbox_setting (
                    id BIGINT PRIMARY KEY,
                    base_url VARCHAR(64) NOT NULL,
                    state INT NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (base_url)
                );
                """;
    }

    private String createSandboxRun(){
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
}
