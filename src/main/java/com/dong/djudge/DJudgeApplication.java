package com.dong.djudge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ADong
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.dong.djudge.mapper")
public class DJudgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DJudgeApplication.class, args);
    }

}
