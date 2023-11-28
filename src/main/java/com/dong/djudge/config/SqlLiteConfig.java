package com.dong.djudge.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 初始化SqlLite数据库
 */
@Component
public class SqlLiteConfig {
    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 初始化SqlLite数据库
     */
    @PostConstruct
    public void init() {
        String createUser = "CREATE TABLE IF NOT EXISTS file (" +
                "    id INTEGER PRIMARY KEY," +
                "    file_id CHAR(12) UNIQUE," +
                "    file_path VARCHAR(512)," +
                "    create_time TIMESTAMP" +
                ");";
        jdbcTemplate.update(createUser);


    }
}
