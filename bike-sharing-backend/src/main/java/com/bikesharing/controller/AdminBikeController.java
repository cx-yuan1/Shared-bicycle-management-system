package com.bikesharing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Constants;
import com.bikesharing.common.Result;
import com.bikesharing.entity.Bike;
import com.bikesharing.service.BikeService;
import com.bikesharing.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理员-单车管理控制器
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/admin/bike")
public class AdminBikeController {
    
    @Autowired
    private BikeService bikeService;
    
    @Autowired
    private FileUtil fileUtil;
    
    /**
     * 添加单车
     * 
     * @param bike 单车信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<Bike> addBike(@RequestBody Bike bike) {
        // 校验必填字段
        if (bike.getBikeNo() == null || bike.getBikeNo().isEmpty()) {
            return Result.error("单车编号不能为空");
        }
        if (bike.getBikeType() == null || bike.getBikeType().isEmpty()) {
            return Result.error("单车类型不能为空");
        }
        
        return bikeService.addBike(bike);
    }
    
    /**
     * 分页查询单车列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param status 状态筛选
     * @return 单车列表
     */
    @GetMapping("/list")
    public Result<Page<Bike>> getBikeList(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String status) {
        return bikeService.getBikeList(page, size, keyword, status);
    }
    
    /**
     * 获取单车详情
     * 
     * @param bikeId 单车ID
     * @return 单车详情
     */
    @GetMapping("/detail/{bikeId}")
    public Result<Bike> getBikeDetail(@PathVariable Long bikeId) {
        return bikeService.getBikeDetail(bikeId);
    }
    
    /**
     * 修改单车信息
     * 
     * @param bike 单车信息
     * @return 修改结果
     */
    @PutMapping("/update")
    public Result<Bike> updateBike(@RequestBody Bike bike) {
        if (bike.getBikeId() == null) {
            return Result.error("单车ID不能为空");
        }
        
        return bikeService.updateBike(bike);
    }
    
    /**
     * 删除单车
     * 
     * @param bikeId 单车ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{bikeId}")
    public Result<Void> deleteBike(@PathVariable Long bikeId) {
        return bikeService.deleteBike(bikeId);
    }
    
    /**
     * 上传单车图片
     * 
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/upload/image")
    public Result<String> uploadBikeImage(@RequestParam("file") MultipartFile file) {
        try {
            // 校验文件类型
            if (!fileUtil.isImage(file)) {
                return Result.error("只能上传图片文件（jpg/png/gif）");
            }
            
            // 校验文件大小（5MB）
            if (!fileUtil.checkFileSize(file, 5 * 1024 * 1024)) {
                return Result.error("图片大小不能超过5MB");
            }
            
            // 上传文件
            String filePath = fileUtil.uploadFile(file, Constants.UPLOAD_PATH_BIKE);
            
            return Result.success("上传成功", filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 生成单车二维码
     * 
     * @param bikeId 单车ID
     * @return 二维码图片
     */
    @PostMapping("/generate-qr")
    public ResponseEntity<byte[]> generateQRCode(@RequestParam Long bikeId) {
        Result<byte[]> result = bikeService.generateQRCode(bikeId);
        
        if (result.getCode() != 200) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "bike_qr_" + bikeId + ".png");
        
        return new ResponseEntity<>(result.getData(), headers, HttpStatus.OK);
    }
}
