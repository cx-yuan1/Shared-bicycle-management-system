package com.bikesharing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Result;
import com.bikesharing.entity.User;
import com.bikesharing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 管理员-用户管理控制器
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 分页查询用户列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result<Page<User>> getUserList(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String keyword) {
        return userService.getUserList(page, size, keyword);
    }
    
    /**
     * 获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/detail/{userId}")
    public Result<User> getUserDetail(@PathVariable Long userId) {
        return userService.getUserInfo(userId);
    }
    
    /**
     * 修改用户信息
     * 
     * @param user 用户信息
     * @return 修改结果
     */
    @PutMapping("/update")
    public Result<User> updateUser(@RequestBody User user) {
        if (user.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        
        return userService.updateUserInfo(user);
    }
    
    /**
     * 启用/禁用用户
     * 
     * @param data 请求数据
     * @return 修改结果
     */
    @PutMapping("/status")
    public Result<Void> updateUserStatus(@RequestBody Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        Integer status = Integer.valueOf(data.get("status").toString());
        
        if (status != 0 && status != 1) {
            return Result.error("状态值只能为0或1");
        }
        
        return userService.updateUserStatus(userId, status);
    }
    
    /**
     * 用户充值
     * 
     * @param data 充值数据
     * @return 充值结果
     */
    @PostMapping("/recharge")
    public Result<Void> recharge(@RequestBody Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        BigDecimal amount = new BigDecimal(data.get("amount").toString());
        
        return userService.recharge(userId, amount);
    }
}
