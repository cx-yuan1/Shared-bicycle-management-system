package com.bikesharing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单车实体类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Data
@TableName("tb_bike")
public class Bike {
    
    /**
     * 单车ID
     */
    @TableId(type = IdType.AUTO)
    private Long bikeId;
    
    /**
     * 单车编号
     */
    private String bikeNo;
    
    /**
     * 单车类型
     */
    private String bikeType;
    
    /**
     * 单车图片
     */
    private String bikeImage;
    
    /**
     * 二维码标识
     */
    private String qrCode;
    
    /**
     * 状态（AVAILABLE/IN_USE/MAINTENANCE）
     */
    private String status;
    
    /**
     * 所属站点ID
     */
    private Long stationId;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 每小时价格
     */
    private BigDecimal pricePerHour;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
