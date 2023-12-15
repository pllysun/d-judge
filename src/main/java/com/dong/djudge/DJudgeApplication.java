package com.dong.djudge;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

/**
 * @author ADong
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.dong.djudge.mapper")
@Slf4j
public class DJudgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DJudgeApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // 创建db目录（如果不存在）
        File db = new File("db");
        if (!db.exists()) {
            if (!db.mkdir()) {
                log.error("创建db目录失败");
            }
        }
        File file = new File("file");
        if (!file.exists()) {
            if (!file.mkdir()) {
                log.error("创建file目录失败");
            }
        }
    }

}
