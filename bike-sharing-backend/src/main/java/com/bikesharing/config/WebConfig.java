package com.bikesharing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Web配置类
 * 配置静态资源访问路径
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-path}")
    private String uploadPath;
    
    private String actualUploadPath;
    
    /**
     * 初始化上传路径
     */
    @PostConstruct
    public void init() {
        // 获取项目根目录
        String projectPath = System.getProperty("user.dir");
        
        // 构建实际的上传路径
        actualUploadPath = projectPath + File.separator + "bike-sharing-backend" + File.separator + uploadPath;
        
        // 如果不存在bike-sharing-backend目录，说明当前就在项目根目录
        File testDir = new File(actualUploadPath);
        if (!testDir.getParentFile().exists()) {
            actualUploadPath = projectPath + File.separator + uploadPath;
        }
        
        System.out.println("静态资源路径配置完成：" + actualUploadPath);
    }
    
    /**
     * 配置静态资源处理器
     * 将/upload/**映射到实际的文件系统路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + actualUploadPath + File.separator);
        
        System.out.println("静态资源映射：/upload/** -> file:" + actualUploadPath + File.separator);
    }
}
