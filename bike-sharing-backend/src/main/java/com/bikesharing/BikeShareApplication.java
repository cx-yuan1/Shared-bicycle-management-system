package com.bikesharing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 共享单车管理系统启动类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@SpringBootApplication
@MapperScan("com.bikesharing.mapper")
public class BikeShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(BikeShareApplication.class, args);
        System.out.println("========================================");
        System.out.println("共享单车管理系统启动成功！");
        System.out.println("访问地址：http://localhost:8080");
        System.out.println("========================================");
    }
}
