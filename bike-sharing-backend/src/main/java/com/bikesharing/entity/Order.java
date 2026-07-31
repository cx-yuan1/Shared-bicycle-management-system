package com.bikesharing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Data
@TableName("tb_order")
public class Order {
    
    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long orderId;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 单车ID
     */
    private Long bikeId;
    
    /**
     * 起始站点ID
     */
    private Long startStationId;
    
    /**
     * 结束站点ID
     */
    private Long endStationId;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 骑行时长（分钟）
     */
    private Integer duration;
    
    /**
     * 总费用
     */
    private BigDecimal totalFee;
    
    /**
     * 状态（IN_PROGRESS/COMPLETED/CANCELLED）
     */
    private String status;
    
    /**
     * 预计用车时长（小时）
     */
    private Integer plannedDuration;
    
    /**
     * 预付费用
     */
    private BigDecimal prepaidFee;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // ========== 关联查询字段（不映射到数据库） ==========
    
    /**
     * 用户名（关联查询）
     */
    @TableField(exist = false)
    private String username;
    
    /**
     * 单车编号（关联查询）
     */
    @TableField(exist = false)
    private String bikeNo;
    
    /**
     * 起始站点名称（关联查询）
     */
    @TableField(exist = false)
    private String startStationName;
    
    /**
     * 结束站点名称（关联查询）
     */
    @TableField(exist = false)
    private String endStationName;
}
