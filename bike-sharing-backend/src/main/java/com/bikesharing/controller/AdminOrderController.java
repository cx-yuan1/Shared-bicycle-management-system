package com.bikesharing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Result;
import com.bikesharing.entity.Order;
import com.bikesharing.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-订单管理控制器
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 分页查询订单列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param status 状态筛选
     * @param userId 用户ID筛选
     * @return 订单列表
     */
    @GetMapping("/list")
    public Result<Page<Order>> getOrderList(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) Long userId) {
        return orderService.getOrderList(page, size, keyword, status, userId);
    }
    
    /**
     * 获取订单详情
     * 
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/detail/{orderId}")
    public Result<Order> getOrderDetail(@PathVariable Long orderId) {
        return orderService.getOrderDetail(orderId);
    }
}
