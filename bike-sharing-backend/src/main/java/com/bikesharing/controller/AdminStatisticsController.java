package com.bikesharing.controller;

import com.bikesharing.common.Result;
import com.bikesharing.mapper.BikeMapper;
import com.bikesharing.mapper.OrderMapper;
import com.bikesharing.mapper.RepairMapper;
import com.bikesharing.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-统计数据控制器
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BikeMapper bikeMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private RepairMapper repairMapper;
    
    /**
     * 获取统计概览数据
     * 
     * @return 统计数据
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getStatisticsOverview() {
        Map<String, Object> data = new HashMap<>();
        
        // 用户统计
        Long totalUsers = userMapper.countUsers();
        Long todayNewUsers = userMapper.countTodayNewUsers();
        
        // 单车统计
        Long totalBikes = bikeMapper.countBikes();
        Long availableBikes = bikeMapper.countAvailableBikes();
        
        // 订单统计
        Long totalOrders = orderMapper.countOrders();
        Long todayOrders = orderMapper.countTodayOrders();
        
        // 报修统计
        Long totalRepairs = repairMapper.countRepairs();
        Long pendingRepairs = repairMapper.countPendingRepairs();
        
        // 单车状态分布
        List<Map<String, Object>> bikeStatusDistribution = bikeMapper.countBikesByStatus();
        
        // 订单状态分布
        List<Map<String, Object>> orderStatusDistribution = orderMapper.countOrdersByStatus();
        
        // 近7天订单趋势
        List<Map<String, Object>> orderTrend = orderMapper.countOrdersLast7Days();
        
        // 近7天收入趋势
        List<Map<String, Object>> incomeTrend = orderMapper.sumFeeLast7Days();
        
        // 组装数据
        data.put("totalUsers", totalUsers);
        data.put("todayNewUsers", todayNewUsers);
        data.put("totalBikes", totalBikes);
        data.put("availableBikes", availableBikes);
        data.put("totalOrders", totalOrders);
        data.put("todayOrders", todayOrders);
        data.put("totalRepairs", totalRepairs);
        data.put("pendingRepairs", pendingRepairs);
        data.put("bikeStatusDistribution", bikeStatusDistribution);
        data.put("orderStatusDistribution", orderStatusDistribution);
        data.put("orderTrend", orderTrend);
        data.put("incomeTrend", incomeTrend);
        
        return Result.success(data);
    }
}
