package com.bikesharing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bikesharing.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 减少用户余额
     * 
     * @param userId 用户ID
     * @param amount 金额
     * @return 影响行数
     */
    @Update("UPDATE tb_user SET balance = balance - #{amount} WHERE user_id = #{userId} AND balance >= #{amount}")
    int decreaseBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    /**
     * 增加用户余额
     * 
     * @param userId 用户ID
     * @param amount 金额
     * @return 影响行数
     */
    @Update("UPDATE tb_user SET balance = balance + #{amount} WHERE user_id = #{userId}")
    int increaseBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    Long countUsers();
    
    /**
     * 统计今日新增用户
     * 
     * @return 今日新增用户数
     */
    Long countTodayNewUsers();
    
    /**
     * 统计用户角色分布
     * 
     * @return 角色分布列表
     */
    List<Map<String, Object>> countUsersByRole();
}
