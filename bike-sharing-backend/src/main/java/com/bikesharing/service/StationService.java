package com.bikesharing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Result;
import com.bikesharing.entity.Station;
import com.bikesharing.mapper.BikeMapper;
import com.bikesharing.mapper.StationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 站点服务类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Service
public class StationService {
    
    @Autowired
    private StationMapper stationMapper;
    
    @Autowired
    private BikeMapper bikeMapper;
    
    /**
     * 添加站点
     * 
     * @param station 站点信息
     * @return 添加结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Station> addStation(Station station) {
        // 设置默认值
        if (station.getCurrentCount() == null) {
            station.setCurrentCount(0);
        }
        if (station.getStatus() == null) {
            station.setStatus(1);
        }
        
        stationMapper.insert(station);
        return Result.success("添加成功", station);
    }
    
    /**
     * 分页查询站点列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词（站点名称或地址）
     * @return 站点列表
     */
    public Result<Page<Station>> getStationList(Integer page, Integer size, String keyword) {
        Page<Station> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        
        // 搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Station::getStationName, keyword)
                    .or().like(Station::getAddress, keyword));
        }
        
        wrapper.orderByDesc(Station::getCreateTime);
        Page<Station> result = stationMapper.selectPage(pageParam, wrapper);
        
        return Result.success(result);
    }
    
    /**
     * 获取所有站点（不分页，用于下拉选择）
     * 
     * @return 站点列表
     */
    public Result<java.util.List<Station>> getAllStations() {
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Station::getStatus, 1);
        wrapper.orderBy(true, true, Station::getStationName);
        
        java.util.List<Station> list = stationMapper.selectList(wrapper);
        return Result.success(list);
    }
    
    /**
     * 获取站点详情
     * 
     * @param stationId 站点ID
     * @return 站点详情
     */
    public Result<Station> getStationDetail(Long stationId) {
        Station station = stationMapper.selectById(stationId);
        if (station == null) {
            return Result.error("站点不存在");
        }
        
        return Result.success(station);
    }
    
    /**
     * 更新站点信息
     * 
     * @param station 站点信息
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Station> updateStation(Station station) {
        Station existStation = stationMapper.selectById(station.getStationId());
        if (existStation == null) {
            return Result.error("站点不存在");
        }
        
        stationMapper.updateById(station);
        
        // 返回更新后的站点信息
        Station updatedStation = stationMapper.selectById(station.getStationId());
        return Result.success("更新成功", updatedStation);
    }
    
    /**
     * 删除站点
     * 
     * @param stationId 站点ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteStation(Long stationId) {
        Station station = stationMapper.selectById(stationId);
        if (station == null) {
            return Result.error("站点不存在");
        }
        
        // 校验是否有关联单车
        Long count = bikeMapper.countByStationId(stationId);
        if (count > 0) {
            return Result.error("该站点下还有 " + count + " 辆单车，无法删除。请先将单车转移到其他站点。");
        }
        
        stationMapper.deleteById(stationId);
        return Result.success("删除成功", null);
    }
}
