package com.bikesharing.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传工具类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Component
public class FileUtil {
    
    @Value("${file.upload-path}")
    private String uploadPath;
    
    /**
     * 实际的上传根目录（项目根目录下的upload文件夹）
     */
    private String actualUploadPath;
    
    /**
     * 初始化上传路径
     * 使用项目根目录而不是临时目录
     */
    @PostConstruct
    public void init() {
        // 获取项目根目录
        String projectPath = System.getProperty("user.dir");
        
        // 构建实际的上传路径：项目根目录/bike-sharing-backend/upload/
        actualUploadPath = projectPath + File.separator + "bike-sharing-backend" + File.separator + uploadPath;
        
        // 如果不存在bike-sharing-backend目录，说明当前就在项目根目录
        File testDir = new File(actualUploadPath);
        if (!testDir.getParentFile().exists()) {
            actualUploadPath = projectPath + File.separator + uploadPath;
        }
        
        // 创建上传根目录
        File uploadDir = new File(actualUploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        System.out.println("文件上传路径初始化完成：" + actualUploadPath);
    }
    
    /**
     * 上传文件
     * 
     * @param file 上传的文件
     * @param subPath 子路径（如：avatar、bike）
     * @return 文件相对路径
     * @throws IOException IO异常
     */
    public String uploadFile(MultipartFile file, String subPath) throws IOException {
        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 获取文件扩展名
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        // 生成唯一文件名（UUID + 扩展名）
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        
        // 确保子路径以/结尾
        if (!subPath.endsWith("/") && !subPath.endsWith(File.separator)) {
            subPath = subPath + File.separator;
        }
        
        // 构建完整的上传路径
        String fullPath = actualUploadPath + subPath;
        File dir = new File(fullPath);
        
        // 如果目录不存在，创建目录
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("创建目录失败：" + fullPath);
            }
        }
        
        // 保存文件
        File destFile = new File(fullPath + fileName);
        file.transferTo(destFile);
        
        // 返回相对路径（用于存储到数据库和前端访问）
        // 格式：/upload/avatar/xxx.jpg
        return "/upload/" + subPath.replace(File.separator, "/") + fileName;
    }
    
    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        // 移除路径前缀"/upload/"
        if (filePath.startsWith("/upload/")) {
            filePath = filePath.substring(8);
        }
        
        // 构建完整路径
        String fullPath = actualUploadPath + filePath;
        File file = new File(fullPath);
        
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        
        return false;
    }
    
    /**
     * 校验文件类型是否为图片
     * 
     * @param file 上传的文件
     * @return 是否为图片
     */
    public boolean isImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        // 支持的图片类型
        return contentType.equals("image/jpeg") 
            || contentType.equals("image/png") 
            || contentType.equals("image/gif")
            || contentType.equals("image/jpg");
    }
    
    /**
     * 校验文件大小
     * 
     * @param file 上传的文件
     * @param maxSize 最大大小（字节）
     * @return 是否符合大小要求
     */
    public boolean checkFileSize(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        return file.getSize() <= maxSize;
    }
}
