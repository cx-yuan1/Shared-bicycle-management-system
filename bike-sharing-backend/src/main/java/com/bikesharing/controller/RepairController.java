package com.bikesharing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Constants;
import com.bikesharing.common.Result;
import com.bikesharing.entity.Repair;
import com.bikesharing.service.RepairService;
import com.bikesharing.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 报修控制器（用户端）
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/repair")
public class RepairController {
    
    @Autowired
    private RepairService repairService;
    
    @Autowired
    private FileUtil fileUtil;
    
    /**
     * 提交报修
     * 
     * @param repair 报修信息
     * @return 提交结果
     */
    @PostMapping("/submit")
    public Result<Repair> submitRepair(@RequestBody Repair repair) {
        // 校验必填字段
        if (repair.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        if (repair.getBikeId() == null) {
            return Result.error("单车ID不能为空");
        }
        if (repair.getFaultType() == null || repair.getFaultType().isEmpty()) {
            return Result.error("故障类型不能为空");
        }
        if (repair.getFaultDesc() == null || repair.getFaultDesc().isEmpty()) {
            return Result.error("故障描述不能为空");
        }
        
        return repairService.submitRepair(repair);
    }
    
    /**
     * 查询用户报修列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 报修列表
     */
    @GetMapping("/list")
    public Result<Page<Repair>> getUserRepairList(@RequestParam Long userId,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        return repairService.getUserRepairList(userId, page, size);
    }
    
    /**
     * 获取报修详情
     * 
     * @param repairId 报修ID
     * @return 报修详情
     */
    @GetMapping("/detail/{repairId}")
    public Result<Repair> getRepairDetail(@PathVariable Long repairId) {
        return repairService.getRepairDetail(repairId);
    }
    
    /**
     * 上传故障图片
     * 
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/upload/image")
    public Result<String> uploadFaultImage(@RequestParam("file") MultipartFile file) {
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
            String filePath = fileUtil.uploadFile(file, Constants.UPLOAD_PATH_REPAIR);
            
            return Result.success("上传成功", filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除报修记录（用户只能删除待处理状态的报修）
     * 
     * @param repairId 报修ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{repairId}")
    public Result<Void> deleteRepair(@PathVariable Long repairId) {
        return repairService.deleteRepair(repairId);
    }
}
