package com.dong.djudge.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 初始化SqlLite数据库
 */
@Component
public class SqliteConfig {
    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 初始化SqlLite数据库
     */
    @PostConstruct
    public void init() {
        String createUser = "CREATE TABLE IF NOT EXISTS file (" +
                "    id BIGINT PRIMARY KEY," +
                "    file_id CHAR(12) UNIQUE," +
                "    file_path VARCHAR(512)," +
                "    create_time TIMESTAMP DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))," +
                "    update_time TIMESTAMP DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))" +
                ");";
        jdbcTemplate.update(createUser);
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM file");
        System.out.println(maps);

    }
}
