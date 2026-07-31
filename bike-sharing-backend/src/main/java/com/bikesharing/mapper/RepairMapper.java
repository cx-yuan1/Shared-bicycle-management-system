package com.bikesharing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bikesharing.entity.Repair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 报修Mapper接口
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Mapper
public interface RepairMapper extends BaseMapper<Repair> {
    
    /**
     * 统计报修总数
     * 
     * @return 报修总数
     */
    Long countRepairs();
    
    /**
     * 统计待处理报修数
     * 
     * @return 待处理报修数
     */
    Long countPendingRepairs();
    
    /**
     * 统计报修状态分布
     * 
     * @return 状态分布列表
     */
    List<Map<String, Object>> countRepairsByStatus();
    
    /**
     * 查询报修详情（包含关联信息）
     * 
     * @param repairId 报修ID
     * @return 报修详情
     */
    Repair selectRepairDetail(@Param("repairId") Long repairId);
    
    /**
     * 分页查询报修列表（包含关联信息）
     * 
     * @param keyword 关键词
     * @param status 状态
     * @param userId 用户ID
     * @return 报修列表
     */
    List<Repair> selectRepairListWithDetails(@Param("keyword") String keyword,
                                             @Param("status") String status,
                                             @Param("userId") Long userId);
}
