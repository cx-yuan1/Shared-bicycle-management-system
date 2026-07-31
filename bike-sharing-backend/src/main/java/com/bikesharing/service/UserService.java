package com.bikesharing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Constants;
import com.bikesharing.common.Result;
import com.bikesharing.entity.RechargeRecord;
import com.bikesharing.entity.User;
import com.bikesharing.mapper.RechargeRecordMapper;
import com.bikesharing.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;
    
    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @return 注册结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<User> register(User user) {
        // 校验用户名是否已存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, user.getUsername());
        User existUser = userMapper.selectOne(usernameWrapper);
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        
        // 校验手机号是否已存在
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, user.getPhone());
        User existPhone = userMapper.selectOne(phoneWrapper);
        if (existPhone != null) {
            return Result.error("手机号已被注册");
        }
        
        // 设置默认值
        user.setRole(Constants.ROLE_USER);
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1);
        
        // 插入用户
        userMapper.insert(user);
        
        // 清除密码后返回
        user.setPassword(null);
        return Result.success("注册成功", user);
    }
    
    /**
     * 用户登录
     * 
     * @param username 用户名或手机号
     * @param password 密码
     * @return 登录结果
     */
    public Result<User> login(String username, String password) {
        // 根据用户名或手机号查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getUsername, username).or().eq(User::getPhone, username));
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 校验密码（明文比对）
        if (!password.equals(user.getPassword())) {
            return Result.error("密码错误");
        }
        
        // 校验用户状态
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }
        
        // 清除密码后返回
        user.setPassword(null);
        return Result.success("登录成功", user);
    }
    
    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    public Result<User> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 清除密码
        user.setPassword(null);
        return Result.success(user);
    }
    
    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<User> updateUserInfo(User user) {
        User existUser = userMapper.selectById(user.getUserId());
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        
        // 如果修改了手机号，需要校验是否已被使用
        if (user.getPhone() != null && !user.getPhone().equals(existUser.getPhone())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, user.getPhone());
            User phoneUser = userMapper.selectOne(wrapper);
            if (phoneUser != null) {
                return Result.error("手机号已被使用");
            }
        }
        
        // 不允许修改用户名、密码、角色、余额、状态
        user.setUsername(null);
        user.setPassword(null);
        user.setRole(null);
        user.setBalance(null);
        user.setStatus(null);
        
        userMapper.updateById(user);
        
        // 返回更新后的用户信息
        User updatedUser = userMapper.selectById(user.getUserId());
        updatedUser.setPassword(null);
        return Result.success("更新成功", updatedUser);
    }
    
    /**
     * 分页查询用户列表（管理员）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词（用户名或手机号）
     * @return 用户列表
     */
    public Result<Page<User>> getUserList(Integer page, Integer size, String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getRealName, keyword));
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        
        // 清除密码
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.success(result);
    }
    
    /**
     * 修改用户状态（管理员）
     * 
     * @param userId 用户ID
     * @param status 状态
     * @return 修改结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        user.setStatus(status);
        userMapper.updateById(user);
        
        return Result.success("状态修改成功", null);
    }
    
    /**
     * 用户充值（管理员）
     * 
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 充值结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> recharge(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        int rows = userMapper.increaseBalance(userId, amount);
        if (rows > 0) {
            return Result.success("充值成功", null);
        } else {
            return Result.error("充值失败");
        }
    }
    
    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 校验原密码
        if (!oldPassword.equals(user.getPassword())) {
            return Result.error("原密码错误");
        }
        
        // 更新密码
        user.setPassword(newPassword);
        userMapper.updateById(user);
        
        return Result.success("密码修改成功", null);
    }
    
    /**
     * 用户充值
     * 
     * @param userId 用户ID
     * @param amount 充值金额
     * @param paymentMethod 支付方式
     * @return 充值结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<User> recharge(Long userId, BigDecimal amount, String paymentMethod) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 记录充值前余额
        BigDecimal balanceBefore = user.getBalance();
        
        // 增加余额
        int rows = userMapper.increaseBalance(userId, amount);
        if (rows <= 0) {
            return Result.error("充值失败");
        }
        
        // 获取充值后余额
        user = userMapper.selectById(userId);
        BigDecimal balanceAfter = user.getBalance();
        
        // 记录充值记录
        RechargeRecord record = new RechargeRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setPaymentMethod(paymentMethod);
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        record.setCreateTime(LocalDateTime.now());
        rechargeRecordMapper.insert(record);
        
        // 清除密码后返回
        user.setPassword(null);
        return Result.success("充值成功", user);
    }
    
    /**
     * 获取充值记录
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 充值记录列表
     */
    public Result<Page<Map<String, Object>>> getRechargeHistory(Long userId, Integer page, Integer size) {
        Page<RechargeRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RechargeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeRecord::getUserId, userId);
        wrapper.orderByDesc(RechargeRecord::getCreateTime);
        
        Page<RechargeRecord> result = rechargeRecordMapper.selectPage(pageParam, wrapper);
        
        // 转换为Map格式，方便前端显示
        Page<Map<String, Object>> mapPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<Map<String, Object>> records = result.getRecords().stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("rechargeId", record.getRechargeId());
            map.put("amount", record.getAmount());
            map.put("paymentMethod", record.getPaymentMethod());
            map.put("balanceBefore", record.getBalanceBefore());
            map.put("balanceAfter", record.getBalanceAfter());
            map.put("createTime", record.getCreateTime());
            return map;
        }).collect(Collectors.toList());
        mapPage.setRecords(records);
        
        return Result.success(mapPage);
    }
}
