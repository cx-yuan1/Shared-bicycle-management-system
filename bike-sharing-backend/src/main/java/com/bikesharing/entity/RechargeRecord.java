package com.bikesharing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值记录实体类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Data
@TableName("recharge_record")
public class RechargeRecord {
    
    /**
     * 充值记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long rechargeId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 充值金额
     */
    private BigDecimal amount;
    
    /**
     * 支付方式（alipay-支付宝，wechat-微信，bank-银行卡）
     */
    private String paymentMethod;
    
    /**
     * 充值前余额
     */
    private BigDecimal balanceBefore;
    
    /**
     * 充值后余额
     */
    private BigDecimal balanceAfter;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
