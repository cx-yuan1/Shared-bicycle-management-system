package com.bikesharing.controller;

import com.bikesharing.common.Result;
import com.bikesharing.entity.Bike;
import com.bikesharing.service.BikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 单车控制器（用户端）
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/bike")
public class BikeController {
    
    @Autowired
    private BikeService bikeService;
    
    /**
     * 根据二维码查询单车信息
     * 
     * @param qrCode 二维码
     * @return 单车信息
     */
    @GetMapping("/info/{qrCode}")
    public Result<Bike> getBikeInfo(@PathVariable String qrCode) {
        return bikeService.getBikeByQrCode(qrCode);
    }
    
    /**
     * 获取单车列表（用户端）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param status 状态筛选
     * @return 单车列表
     */
    @GetMapping("/list")
    public Result<?> getBikeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return bikeService.getBikeList(page, size, null, status);
    }
}
