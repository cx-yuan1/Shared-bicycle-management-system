package com.bikesharing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bikesharing.entity.Bike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 单车Mapper接口
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Mapper
public interface BikeMapper extends BaseMapper<Bike> {
    
    /**
     * 根据站点ID统计单车数量
     * 
     * @param stationId 站点ID
     * @return 单车数量
     */
    @Select("SELECT COUNT(*) FROM tb_bike WHERE station_id = #{stationId}")
    Long countByStationId(Long stationId);
    
    /**
     * 根据二维码查询单车
     * 
     * @param qrCode 二维码
     * @return 单车信息
     */
    @Select("SELECT * FROM tb_bike WHERE qr_code = #{qrCode}")
    Bike selectByQrCode(String qrCode);
    
    /**
     * 统计单车总数
     * 
     * @return 单车总数
     */
    Long countBikes();
    
    /**
     * 统计可用单车数
     * 
     * @return 可用单车数
     */
    Long countAvailableBikes();
    
    /**
     * 统计单车状态分布
     * 
     * @return 状态分布列表
     */
    List<Map<String, Object>> countBikesByStatus();
    
    /**
     * 统计单车类型分布
     * 
     * @return 类型分布列表
     */
    List<Map<String, Object>> countBikesByType();
    
    /**
     * 查询附近可用单车
     * 
     * @return 单车列表
     */
    List<Bike> findNearbyBikes();
}
