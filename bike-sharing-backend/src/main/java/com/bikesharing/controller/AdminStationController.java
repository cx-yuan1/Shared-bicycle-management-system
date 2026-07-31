package com.bikesharing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Constants;
import com.bikesharing.common.Result;
import com.bikesharing.entity.Station;
import com.bikesharing.service.StationService;
import com.bikesharing.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理员-站点管理控制器
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/admin/station")
public class AdminStationController {
    
    @Autowired
    private StationService stationService;
    
    @Autowired
    private FileUtil fileUtil;
    
    /**
     * 添加站点
     * 
     * @param station 站点信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<Station> addStation(@RequestBody Station station) {
        // 校验必填字段
        if (station.getStationName() == null || station.getStationName().isEmpty()) {
            return Result.error("站点名称不能为空");
        }
        if (station.getAddress() == null || station.getAddress().isEmpty()) {
            return Result.error("站点地址不能为空");
        }
        if (station.getCapacity() == null || station.getCapacity() <= 0) {
            return Result.error("站点容量必须大于0");
        }
        
        return stationService.addStation(station);
    }
    
    /**
     * 分页查询站点列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @return 站点列表
     */
    @GetMapping("/list")
    public Result<Page<Station>> getStationList(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String keyword) {
        return stationService.getStationList(page, size, keyword);
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
    
    /**
     * 修改站点信息
     * 
     * @param station 站点信息
     * @return 修改结果
     */
    @PutMapping("/update")
    public Result<Station> updateStation(@RequestBody Station station) {
        if (station.getStationId() == null) {
            return Result.error("站点ID不能为空");
        }
        
        return stationService.updateStation(station);
    }
    
    /**
     * 删除站点
     * 
     * @param stationId 站点ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{stationId}")
    public Result<Void> deleteStation(@PathVariable Long stationId) {
        return stationService.deleteStation(stationId);
    }
    
    /**
     * 上传站点图片
     * 
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/upload/image")
    public Result<String> uploadStationImage(@RequestParam("file") MultipartFile file) {
        try {
            // 校验文件类型
            if (!fileUtil.isImage(file)) {
                return Result.error("只能上传图片文件（jpg/png/gif）");
            }
            
            // 校验文件大小（5MB）
            if (!fileUtil.checkFileSize(file, 5 * 1024 * 1024)) {
                return Result.error("图片大小不能超过5MB");
            }
            
            // 上传文件
            String filePath = fileUtil.uploadFile(file, Constants.UPLOAD_PATH_STATION);
            
            return Result.success("上传成功", filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}
