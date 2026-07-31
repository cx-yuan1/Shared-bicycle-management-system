package com.bikesharing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Constants;
import com.bikesharing.common.Result;
import com.bikesharing.entity.Bike;
import com.bikesharing.entity.Order;
import com.bikesharing.entity.User;
import com.bikesharing.mapper.BikeMapper;
import com.bikesharing.mapper.OrderMapper;
import com.bikesharing.mapper.StationMapper;
import com.bikesharing.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单服务类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Service
public class OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private BikeMapper bikeMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StationMapper stationMapper;
    
    /**
     * 扫码用车（创建订单）
     * 关键点：事务保证订单创建与车辆状态更新的一致性
     * 
     * @param userId 用户ID
     * @param qrCode 二维码
     * @return 订单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Order> startRide(Long userId, String qrCode) {
        // 1. 检查用户是否有进行中的订单
        Order existOrder = orderMapper.selectInProgressByUserId(userId);
        if (existOrder != null) {
            return Result.error("您有正在进行中的订单，请先还车");
        }
        
        // 2. 根据二维码查询单车
        Bike bike = bikeMapper.selectByQrCode(qrCode);
        if (bike == null) {
            return Result.error("单车不存在");
        }
        
        // 3. 校验单车状态
        if (!Constants.BIKE_STATUS_AVAILABLE.equals(bike.getStatus())) {
            String statusText = Constants.BIKE_STATUS_IN_USE.equals(bike.getStatus()) 
                ? "使用中" : "维修中";
            return Result.error("单车不可用，当前状态：" + statusText);
        }
        
        // 4. 检查用户余额
        User user = userMapper.selectById(userId);
        if (user.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("账户余额不足，请先充值");
        }
        
        // 5. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setBikeId(bike.getBikeId());
        order.setStartStationId(bike.getStationId());
        order.setStartTime(LocalDateTime.now());
        order.setStatus(Constants.ORDER_STATUS_IN_PROGRESS);
        orderMapper.insert(order);
        
        // 6. 更新单车状态为使用中
        bike.setStatus(Constants.BIKE_STATUS_IN_USE);
        bikeMapper.updateById(bike);
        
        // 7. 更新站点当前车辆数（如果单车有所属站点）
        if (bike.getStationId() != null) {
            stationMapper.decreaseCurrentCount(bike.getStationId());
        }
        
        return Result.success("用车成功", order);
    }
    
    /**
     * 通过单车ID开始用车（创建订单）
     * 
     * @param userId 用户ID
     * @param bikeId 单车ID
     * @return 订单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Order> startRideByBikeId(Long userId, Long bikeId) {
        return startRideByBikeId(userId, bikeId, null, null);
    }
    
    /**
     * 通过单车ID开始用车（支持预付费）
     * 
     * @param userId 用户ID
     * @param bikeId 单车ID
     * @param plannedDuration 预计用车时长（小时）
     * @param prepaidFee 预付费用
     * @return 订单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Order> startRideByBikeId(Long userId, Long bikeId, Integer plannedDuration, BigDecimal prepaidFee) {
        // 1. 检查用户是否有进行中的订单
        Order existOrder = orderMapper.selectInProgressByUserId(userId);
        if (existOrder != null) {
            return Result.error("您有正在进行中的订单，请先还车");
        }
        
        // 2. 查询单车
        Bike bike = bikeMapper.selectById(bikeId);
        if (bike == null) {
            return Result.error("单车不存在");
        }
        
        // 3. 校验单车状态
        if (!Constants.BIKE_STATUS_AVAILABLE.equals(bike.getStatus())) {
            String statusText = Constants.BIKE_STATUS_IN_USE.equals(bike.getStatus()) 
                ? "使用中" : "维修中";
            return Result.error("单车不可用，当前状态：" + statusText);
        }
        
        // 4. 检查用户余额
        User user = userMapper.selectById(userId);
        
        // 如果有预付费，检查余额是否足够
        if (prepaidFee != null && prepaidFee.compareTo(BigDecimal.ZERO) > 0) {
            if (user.getBalance().compareTo(prepaidFee) < 0) {
                return Result.error("账户余额不足，请先充值");
            }
            
            // 扣除预付费用
            user.setBalance(user.getBalance().subtract(prepaidFee));
            userMapper.updateById(user);
        } else {
            // 没有预付费，只检查余额是否大于0
            if (user.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("账户余额不足，请先充值");
            }
        }
        
        // 5. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setBikeId(bike.getBikeId());
        order.setStartStationId(bike.getStationId());
        order.setStartTime(LocalDateTime.now());
        order.setStatus(Constants.ORDER_STATUS_IN_PROGRESS);
        order.setPlannedDuration(plannedDuration);
        order.setPrepaidFee(prepaidFee);
        orderMapper.insert(order);
        
        // 6. 更新单车状态为使用中
        bike.setStatus(Constants.BIKE_STATUS_IN_USE);
        bikeMapper.updateById(bike);
        
        // 7. 更新站点当前车辆数（如果单车有所属站点）
        if (bike.getStationId() != null) {
            stationMapper.decreaseCurrentCount(bike.getStationId());
        }
        
        return Result.success("用车成功", order);
    }
    
    /**
     * 还车（结束订单）
     * 关键点：计算费用、更新订单、更新车辆状态、更新站点
     * 
     * @param userId 用户ID
     * @param endStationId 结束站点ID
     * @return 订单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Order> endRide(Long userId, Long endStationId) {
        // 1. 查询用户的进行中订单
        Order order = orderMapper.selectInProgressByUserId(userId);
        if (order == null) {
            return Result.error("没有进行中的订单");
        }
        
        // 2. 查询单车信息
        Bike bike = bikeMapper.selectById(order.getBikeId());
        if (bike == null) {
            return Result.error("单车信息异常");
        }
        
        // 3. 计算骑行时长和费用
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(order.getStartTime(), endTime);
        long minutes = duration.toMinutes();
        
        // 向上取整小时数
        BigDecimal hours = new BigDecimal(Math.ceil(minutes / 60.0));
        if (hours.compareTo(BigDecimal.ZERO) == 0) {
            hours = BigDecimal.ONE; // 最少按1小时计费
        }
        
        BigDecimal actualFee = bike.getPricePerHour().multiply(hours).setScale(2, RoundingMode.HALF_UP);
        
        // 4. 处理预付费逻辑（多退少补）
        User user = userMapper.selectById(userId);
        BigDecimal prepaidFee = order.getPrepaidFee() != null ? order.getPrepaidFee() : BigDecimal.ZERO;
        BigDecimal feeDifference = actualFee.subtract(prepaidFee);
        
        String message;
        if (feeDifference.compareTo(BigDecimal.ZERO) > 0) {
            // 实际费用大于预付费用，需要补扣
            if (user.getBalance().compareTo(feeDifference) < 0) {
                return Result.error("账户余额不足，需要补扣：¥" + feeDifference + "，当前余额：¥" + user.getBalance());
            }
            // 扣除差额
            int rows = userMapper.decreaseBalance(userId, feeDifference);
            if (rows == 0) {
                throw new RuntimeException("扣费失败，余额不足");
            }
            message = String.format("还车成功！预付：¥%.2f，实际：¥%.2f，补扣：¥%.2f", 
                prepaidFee, actualFee, feeDifference);
        } else if (feeDifference.compareTo(BigDecimal.ZERO) < 0) {
            // 实际费用小于预付费用，需要退款
            BigDecimal refund = feeDifference.abs();
            user.setBalance(user.getBalance().add(refund));
            userMapper.updateById(user);
            message = String.format("还车成功！预付：¥%.2f，实际：¥%.2f，退还：¥%.2f", 
                prepaidFee, actualFee, refund);
        } else {
            // 实际费用等于预付费用
            if (prepaidFee.compareTo(BigDecimal.ZERO) > 0) {
                message = String.format("还车成功！预付：¥%.2f，实际：¥%.2f，无需补扣", 
                    prepaidFee, actualFee);
            } else {
                // 没有预付费，直接扣款
                if (user.getBalance().compareTo(actualFee) < 0) {
                    return Result.error("账户余额不足，需要支付：¥" + actualFee + "，当前余额：¥" + user.getBalance());
                }
                int rows = userMapper.decreaseBalance(userId, actualFee);
                if (rows == 0) {
                    throw new RuntimeException("扣费失败，余额不足");
                }
                message = "还车成功，本次消费：¥" + actualFee;
            }
        }
        
        // 5. 更新订单
        order.setEndStationId(endStationId);
        order.setEndTime(endTime);
        order.setDuration((int) minutes);
        order.setTotalFee(actualFee);
        order.setStatus(Constants.ORDER_STATUS_COMPLETED);
        orderMapper.updateById(order);
        
        // 6. 更新单车状态和所属站点
        bike.setStatus(Constants.BIKE_STATUS_AVAILABLE);
        bike.setStationId(endStationId);
        bikeMapper.updateById(bike);
        
        // 7. 更新站点当前车辆数
        stationMapper.increaseCurrentCount(endStationId);
        
        return Result.success(message, order);
    }
    
    /**
     * 查询用户订单列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param status 状态筛选
     * @return 订单列表
     */
    public Result<Page<Order>> getUserOrderList(Long userId, Integer page, Integer size, String status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Order::getUserId, userId);
        
        // 状态筛选
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }
        
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        
        return Result.success(result);
    }
    
    /**
     * 查询订单详情（包含关联信息）
     * 
     * @param orderId 订单ID
     * @return 订单详情
     */
    public Result<Order> getOrderDetail(Long orderId) {
        Order order = orderMapper.selectOrderDetail(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        return Result.success(order);
    }
    
    /**
     * 管理员查询订单列表（包含关联信息）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词（订单号）
     * @param status 状态筛选
     * @param userId 用户ID筛选
     * @return 订单列表
     */
    public Result<Page<Order>> getOrderList(Integer page, Integer size, String keyword, String status, Long userId) {
        Page<Order> pageParam = new Page<>(page, size);
        
        // 使用自定义的关联查询方法
        List<Order> records = orderMapper.selectOrderListWithDetails(keyword, status, userId);
        
        // 设置总数
        int total = records.size();
        pageParam.setTotal(total);
        
        // 手动分页
        if (total > 0) {
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            
            // 防止越界
            if (start < total) {
                List<Order> pageRecords = records.subList(start, end);
                pageParam.setRecords(pageRecords);
            } else {
                pageParam.setRecords(new java.util.ArrayList<>());
            }
        } else {
            pageParam.setRecords(new java.util.ArrayList<>());
        }
        
        return Result.success(pageParam);
    }
    
    /**
     * 生成订单编号
     * 格式：ORD + 时间戳 + 4位随机数
     * 
     * @return 订单编号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 9000) + 1000;
        return "ORD" + timestamp + random;
    }
}
