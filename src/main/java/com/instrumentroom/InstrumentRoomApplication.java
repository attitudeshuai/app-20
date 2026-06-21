package com.instrumentroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 乐器练习室预约管理系统启动类
 */
@SpringBootApplication
@EnableScheduling
public class InstrumentRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstrumentRoomApplication.class, args);
    }
}
