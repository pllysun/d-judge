package com.dong.djudge.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
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
        jdbcTemplate.update(dropTrigger1());
        jdbcTemplate.update(createFileTrigger1());
        jdbcTemplate.update(createStandardCode());
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM test_group");
        log.info(Arrays.toString(maps.toArray()));
    }

    private String createFile() {
        return "CREATE TABLE IF NOT EXISTS test_group (" +
                "    id BIGINT PRIMARY KEY," +
                "    test_group_id CHAR(12) UNIQUE," +
                "    test_group_path VARCHAR(512)," +
                "    create_time TIMESTAMP DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))," +
                "    update_time TIMESTAMP DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))" +
                ");";
    }

    private String dropTrigger1() {
        return "DROP TRIGGER IF EXISTS update_file_trigger;";
    }

    private String createFileTrigger1() {
        return "CREATE TRIGGER update_file_trigger AFTER UPDATE ON test_group BEGIN UPDATE file SET update_time = strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime') WHERE id = NEW.id; END;";
    }


    private String createStandardCode() {
        return """
                CREATE TABLE IF NOT EXISTS standard_code (
                    id BIGINT PRIMARY KEY,
                    code_id VARCHAR(12) NOT NULL,
                    test_group_id VARCHAR(12) NOT NULL,
                    code_path VARCHAR(512) NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (code_id)
                );
                """;
    }
}
