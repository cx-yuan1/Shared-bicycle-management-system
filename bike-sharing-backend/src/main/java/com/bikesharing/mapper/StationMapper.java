package com.bikesharing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bikesharing.entity.Station;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 站点Mapper接口
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Mapper
public interface StationMapper extends BaseMapper<Station> {
    
    /**
     * 增加站点当前车辆数
     * 
     * @param stationId 站点ID
     * @return 影响行数
     */
    @Update("UPDATE tb_station SET current_count = current_count + 1 WHERE station_id = #{stationId}")
    int increaseCurrentCount(Long stationId);
    
    /**
     * 减少站点当前车辆数
     * 
     * @param stationId 站点ID
     * @return 影响行数
     */
    @Update("UPDATE tb_station SET current_count = current_count - 1 WHERE station_id = #{stationId} AND current_count > 0")
    int decreaseCurrentCount(Long stationId);
    
    /**
     * 统计站点总数
     * 
     * @return 站点总数
     */
    Long countStations();
    
    /**
     * 统计活跃站点数
     * 
     * @return 活跃站点数
     */
    Long countActiveStations();
    
    /**
     * 查询所有站点（用于下拉选择）
     * 
     * @return 站点列表
     */
    List<Station> selectAllStations();
}
