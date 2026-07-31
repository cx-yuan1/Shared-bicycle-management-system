package com.bikesharing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修实体类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Data
@TableName("tb_repair")
public class Repair {
    
    /**
     * 报修ID
     */
    @TableId(type = IdType.AUTO)
    private Long repairId;
    
    /**
     * 报修用户ID
     */
    private Long userId;
    
    /**
     * 单车ID
     */
    private Long bikeId;
    
    /**
     * 故障类型
     */
    private String faultType;
    
    /**
     * 故障描述
     */
    private String faultDesc;
    
    /**
     * 故障图片
     */
    private String faultImage;
    
    /**
     * 状态（PENDING/PROCESSING/COMPLETED）
     */
    private String status;
    
    /**
     * 处理人ID
     */
    private Long handlerId;
    
    /**
     * 处理结果
     */
    private String handleResult;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    
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
     * 处理人姓名（关联查询）
     */
    @TableField(exist = false)
    private String handlerName;
}
