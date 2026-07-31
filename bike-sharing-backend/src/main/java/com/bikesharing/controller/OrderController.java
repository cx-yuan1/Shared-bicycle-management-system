package com.bikesharing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Result;
import com.bikesharing.entity.Order;
import com.bikesharing.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 订单控制器（用户端）
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 开始用车
     * 
     * @param data 请求数据（包含userId和bikeId）
     * @param userId 用户ID（从请求头获取）
     * @return 订单信息
     */
    @PostMapping("/start")
    public Result<Order> startRide(@RequestBody Map<String, Object> data,
                                    @RequestHeader(value = "User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        // 支持两种方式：通过bikeId或qrCode
        Long bikeId = null;
        String qrCode = null;
        Integer plannedDuration = null;
        BigDecimal prepaidFee = null;
        
        if (data.get("bikeId") != null) {
            bikeId = Long.valueOf(data.get("bikeId").toString());
        }
        if (data.get("qrCode") != null) {
            qrCode = data.get("qrCode").toString();
        }
        if (data.get("plannedDuration") != null) {
            plannedDuration = Integer.valueOf(data.get("plannedDuration").toString());
        }
        if (data.get("prepaidFee") != null) {
            prepaidFee = new BigDecimal(data.get("prepaidFee").toString());
        }
        
        if (bikeId == null && (qrCode == null || qrCode.isEmpty())) {
            return Result.error("请选择单车或输入二维码");
        }
        
        // 如果提供了bikeId，直接使用bikeId开始用车
        if (bikeId != null) {
            return orderService.startRideByBikeId(userId, bikeId, plannedDuration, prepaidFee);
        }
        
        // 否则使用二维码
        return orderService.startRide(userId, qrCode);
    }
    
    /**
     * 还车
     * 
     * @param data 请求数据
     * @param userId 用户ID（从请求头获取）
     * @return 订单信息
     */
    @PostMapping("/end")
    public Result<Order> endRide(@RequestBody Map<String, Object> data,
                                  @RequestHeader(value = "User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        Long endStationId = Long.valueOf(data.get("endStationId").toString());
        if (endStationId == null) {
            return Result.error("请选择还车站点");
        }
        
        return orderService.endRide(userId, endStationId);
    }
    
    /**
     * 查询个人订单列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param status 状态筛选
     * @param userId 用户ID（从请求头获取）
     * @return 订单列表
     */
    @GetMapping("/list")
    public Result<Page<Order>> getOrderList(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String status,
                                             @RequestHeader(value = "User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        return orderService.getUserOrderList(userId, page, size, status);
    }
    
    /**
     * 查询订单详情
     * 
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/detail/{orderId}")
    public Result<Order> getOrderDetail(@PathVariable Long orderId) {
        return orderService.getOrderDetail(orderId);
    }
}
