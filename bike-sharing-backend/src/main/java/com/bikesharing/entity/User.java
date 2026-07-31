package com.bikesharing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Data
@TableName("tb_user")
public class User {
    
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码（明文存储）
     */
    private String password;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 头像路径
     */
    private String avatar;
    
    /**
     * 角色（USER/ADMIN）
     */
    private String role;
    
    /**
     * 账户余额
     */
    private BigDecimal balance;
    
    /**
     * 状态（1正常/0禁用）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
