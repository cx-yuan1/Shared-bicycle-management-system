package com.bikesharing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Result;
import com.bikesharing.entity.Repair;
import com.bikesharing.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-报修管理控制器
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/api/admin/repair")
public class AdminRepairController {
    
    @Autowired
    private RepairService repairService;
    
    /**
     * 分页查询报修列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param status 状态筛选
     * @param keyword 搜索关键词
     * @return 报修列表
     */
    @GetMapping("/list")
    public Result<Page<Repair>> getRepairList(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String keyword) {
        return repairService.getAdminRepairList(page, size, status, keyword);
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
     * 接单处理
     * 
     * @param repairId 报修ID
     * @param handlerId 处理人ID
     * @return 处理结果
     */
    @PostMapping("/accept")
    public Result<Repair> acceptRepair(@RequestParam Long repairId,
                                         @RequestParam Long handlerId) {
        return repairService.acceptRepair(repairId, handlerId);
    }
    
    /**
     * 完成报修
     * 
     * @param repairId 报修ID
     * @param handleResult 处理结果
     * @return 处理结果
     */
    @PostMapping("/complete")
    public Result<Repair> completeRepair(@RequestParam Long repairId,
                                           @RequestParam String handleResult) {
        if (handleResult == null || handleResult.isEmpty()) {
            return Result.error("处理结果不能为空");
        }
        
        return repairService.completeRepair(repairId, handleResult);
    }
    
    /**
     * 删除报修记录
     * 
     * @param repairId 报修ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{repairId}")
    public Result<Void> deleteRepair(@PathVariable Long repairId) {
        return repairService.deleteRepair(repairId);
    }
}
