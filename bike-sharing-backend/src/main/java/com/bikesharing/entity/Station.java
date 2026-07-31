package com.bikesharing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 站点实体类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Data
@TableName("tb_station")
public class Station {
    
    /**
     * 站点ID
     */
    @TableId(type = IdType.AUTO)
    private Long stationId;
    
    /**
     * 站点名称
     */
    private String stationName;
    
    /**
     * 站点地址
     */
    private String address;
    
    /**
     * 站点图片
     */
    private String stationImage;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 容量
     */
    private Integer capacity;
    
    /**
     * 当前车辆数
     */
    private Integer currentCount;
    
    /**
     * 状态（1-启用，0-禁用）
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
