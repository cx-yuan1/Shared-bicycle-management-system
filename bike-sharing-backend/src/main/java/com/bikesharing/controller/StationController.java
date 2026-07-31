package com.bikesharing.controller;

import com.bikesharing.common.Result;
import com.bikesharing.entity.Station;
import com.bikesharing.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 站点控制器（用户端）
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/station")
public class StationController {
    
    @Autowired
    private StationService stationService;
    
    /**
     * 获取所有站点（用于地图展示和下拉选择）
     * 
     * @return 站点列表
     */
    @GetMapping("/list")
    public Result<List<Station>> getAllStations() {
        return stationService.getAllStations();
    }
    
    /**
     * 获取站点详情
     * 
     * @param stationId 站点ID
     * @return 站点详情
     */
    @GetMapping("/detail/{stationId}")
    public Result<Station> getStationDetail(@PathVariable Long stationId) {
        return stationService.getStationDetail(stationId);
    }
}
