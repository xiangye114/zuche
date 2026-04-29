package com.zuche;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.zuche.mapper")
@EnableScheduling
public class ZucheApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZucheApplication.class, args);
    }
}
