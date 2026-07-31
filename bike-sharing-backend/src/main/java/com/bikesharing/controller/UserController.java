package com.bikesharing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Constants;
import com.bikesharing.common.Result;
import com.bikesharing.entity.User;
import com.bikesharing.service.UserService;
import com.bikesharing.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileUtil fileUtil;

    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        // 校验必填字段
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            return Result.error("手机号不能为空");
        }

        return userService.register(user);
    }

    /**
     * 用户登录
     * 
     * @param loginData 登录数据
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || username.isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.error("密码不能为空");
        }

        return userService.login(username, password);
    }

    /**
     * 获取个人信息
     * 
     * @param userId 用户ID（从请求头获取）
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader(value = "User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }

        return userService.getUserInfo(userId);
    }

    /**
     * 修改个人信息
     * 
     * @param user   用户信息
     * @param userId 用户ID（从请求头获取）
     * @return 修改结果
     */
    @PutMapping("/update")
    public Result<User> updateUserInfo(@RequestBody User user,
            @RequestHeader(value = "User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }

        user.setUserId(userId);
        return userService.updateUserInfo(user);
    }

    /**
     * 上传头像
     * 
     * @param file   头像文件
     * @param userId 用户ID（从请求头获取）
     * @return 上传结果
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
            @RequestHeader(value = "User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }

        try {
            // 校验文件类型
            if (!fileUtil.isImage(file)) {
                return Result.error("只能上传图片文件（jpg/png/gif）");
            }

            // 上传文件
            String filePath = fileUtil.uploadFile(file, Constants.UPLOAD_PATH_AVATAR);

            // 更新用户头像
            User user = new User();
            user.setUserId(userId);
            user.setAvatar(filePath);
            userService.updateUserInfo(user);

            return Result.success("上传成功", filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     * 
     * @param passwordData 密码数据
     * @param userId       用户ID（从请求头获取）
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> passwordData,
            @RequestHeader(value = "User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }

        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("请输入原密码");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("请输入新密码");
        }
        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        return userService.updatePassword(userId, oldPassword, newPassword);
    }

    /**
     * 账户充值
     * 
     * @param rechargeData 充值数据
     * @param userId       用户ID（从请求头获取）
     * @return 充值结果
     */
    @PostMapping("/recharge")
    public Result<User> recharge(@RequestBody Map<String, Object> rechargeData,
            @RequestHeader(value = "User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }

        Object amountObj = rechargeData.get("amount");
        String paymentMethod = (String) rechargeData.get("paymentMethod");

        if (amountObj == null) {
            return Result.error("请输入充值金额");
        }

        BigDecimal amount;
        try {
            if (amountObj instanceof Integer) {
                amount = new BigDecimal((Integer) amountObj);
            } else if (amountObj instanceof Double) {
                amount = BigDecimal.valueOf((Double) amountObj);
            } else if (amountObj instanceof String) {
                amount = new BigDecimal((String) amountObj);
            } else {
                amount = new BigDecimal(amountObj.toString());
            }
        } catch (Exception e) {
            return Result.error("充值金额格式错误");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            return Result.error("单次充值金额不能超过10000元");
        }

        if (paymentMethod == null || paymentMethod.isEmpty()) {
            return Result.error("请选择支付方式");
        }

        return userService.recharge(userId, amount, paymentMethod);
    }

    /**
     * 获取充值记录
     * 
     * @param page   页码
     * @param size   每页数量
     * @param userId 用户ID（从请求头获取）
     * @return 充值记录列表
     */
    @GetMapping("/recharge/history")
    public Result<Page<Map<String, Object>>> getRechargeHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader(value = "User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }

        return userService.getRechargeHistory(userId, page, size);
    }
}
