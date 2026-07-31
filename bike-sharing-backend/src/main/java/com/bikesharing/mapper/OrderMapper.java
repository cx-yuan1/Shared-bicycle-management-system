package com.bikesharing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bikesharing.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单Mapper接口
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 统计单车的进行中订单数量
     * 
     * @param bikeId 单车ID
     * @return 订单数量
     */
    @Select("SELECT COUNT(*) FROM tb_order WHERE bike_id = #{bikeId} AND status = 'IN_PROGRESS'")
    Long countInProgressByBikeId(Long bikeId);
    
    /**
     * 查询用户的进行中订单
     * 
     * @param userId 用户ID
     * @return 订单信息
     */
    @Select("SELECT * FROM tb_order WHERE user_id = #{userId} AND status = 'IN_PROGRESS' LIMIT 1")
    Order selectInProgressByUserId(Long userId);
    
    /**
     * 统计订单总数
     * 
     * @return 订单总数
     */
    Long countOrders();
    
    /**
     * 统计今日订单数
     * 
     * @return 今日订单数
     */
    Long countTodayOrders();
    
    /**
     * 统计订单状态分布
     * 
     * @return 状态分布列表
     */
    List<Map<String, Object>> countOrdersByStatus();
    
    /**
     * 统计近7天订单趋势
     * 
     * @return 订单趋势列表
     */
    List<Map<String, Object>> countOrdersLast7Days();
    
    /**
     * 统计近7天收入趋势
     * 
     * @return 收入趋势列表
     */
    List<Map<String, Object>> sumFeeLast7Days();
    
    /**
     * 查询订单详情（包含关联信息）
     * 
     * @param orderId 订单ID
     * @return 订单详情
     */
    Order selectOrderDetail(@Param("orderId") Long orderId);
    
    /**
     * 分页查询订单列表（包含关联信息）
     * 
     * @param keyword 关键词
     * @param status 状态
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> selectOrderListWithDetails(@Param("keyword") String keyword, 
                                           @Param("status") String status,
                                           @Param("userId") Long userId);
    
    /**
     * 查询用户当前进行中的订单
     * 
     * @param userId 用户ID
     * @return 订单信息
     */
    Order selectCurrentOrder(@Param("userId") Long userId);
}
