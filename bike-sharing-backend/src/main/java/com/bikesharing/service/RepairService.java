package com.bikesharing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Constants;
import com.bikesharing.common.Result;
import com.bikesharing.entity.Bike;
import com.bikesharing.entity.Repair;
import com.bikesharing.mapper.BikeMapper;
import com.bikesharing.mapper.RepairMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报修服务类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Service
public class RepairService {
    
    @Autowired
    private RepairMapper repairMapper;
    
    @Autowired
    private BikeMapper bikeMapper;
    
    /**
     * 提交报修
     * 
     * @param repair 报修信息
     * @return 提交结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Repair> submitRepair(Repair repair) {
        // 校验单车是否存在
        Bike bike = bikeMapper.selectById(repair.getBikeId());
        if (bike == null) {
            return Result.error("单车不存在");
        }
        
        // 设置默认状态
        repair.setStatus(Constants.REPAIR_STATUS_PENDING);
        
        // 将单车状态更新为维修中
        bike.setStatus(Constants.BIKE_STATUS_MAINTENANCE);
        bikeMapper.updateById(bike);
        
        // 保存报修记录
        repairMapper.insert(repair);
        
        return Result.success("报修提交成功，我们会尽快处理", repair);
    }
    
    /**
     * 分页查询报修列表（用户端）
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 报修列表
     */
    public Result<Page<Repair>> getUserRepairList(Long userId, Integer page, Integer size) {
        Page<Repair> pageParam = new Page<>(page, size);
        
        // 使用自定义的关联查询方法
        List<Repair> records = repairMapper.selectRepairListWithDetails(null, null, userId);
        
        // 设置总数
        int total = records.size();
        pageParam.setTotal(total);
        
        // 手动分页
        if (total > 0) {
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            
            // 防止越界
            if (start < total) {
                List<Repair> pageRecords = records.subList(start, end);
                pageParam.setRecords(pageRecords);
            } else {
                pageParam.setRecords(new java.util.ArrayList<>());
            }
        } else {
            pageParam.setRecords(new java.util.ArrayList<>());
        }
        
        return Result.success(pageParam);
    }
    
    /**
     * 分页查询报修列表（管理员端，包含关联信息）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param status 状态筛选
     * @param keyword 搜索关键词
     * @return 报修列表
     */
    public Result<Page<Repair>> getAdminRepairList(Integer page, Integer size, String status, String keyword) {
        Page<Repair> pageParam = new Page<>(page, size);
        
        // 使用自定义的关联查询方法
        List<Repair> records = repairMapper.selectRepairListWithDetails(keyword, status, null);
        
        // 设置总数
        int total = records.size();
        pageParam.setTotal(total);
        
        // 手动分页
        if (total > 0) {
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            
            // 防止越界
            if (start < total) {
                List<Repair> pageRecords = records.subList(start, end);
                pageParam.setRecords(pageRecords);
            } else {
                pageParam.setRecords(new java.util.ArrayList<>());
            }
        } else {
            pageParam.setRecords(new java.util.ArrayList<>());
        }
        
        return Result.success(pageParam);
    }
    
    /**
     * 获取报修详情（包含关联信息）
     * 
     * @param repairId 报修ID
     * @return 报修详情
     */
    public Result<Repair> getRepairDetail(Long repairId) {
        Repair repair = repairMapper.selectRepairDetail(repairId);
        if (repair == null) {
            return Result.error("报修记录不存在");
        }
        
        return Result.success(repair);
    }
    
    /**
     * 接单处理（管理员）
     * 
     * @param repairId 报修ID
     * @param handlerId 处理人ID
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Repair> acceptRepair(Long repairId, Long handlerId) {
        Repair repair = repairMapper.selectById(repairId);
        if (repair == null) {
            return Result.error("报修记录不存在");
        }
        
        if (!Constants.REPAIR_STATUS_PENDING.equals(repair.getStatus())) {
            return Result.error("该报修已被处理，无法重复接单");
        }
        
        // 更新报修状态为处理中
        repair.setStatus(Constants.REPAIR_STATUS_PROCESSING);
        repair.setHandlerId(handlerId);
        repairMapper.updateById(repair);
        
        return Result.success("接单成功", repair);
    }
    
    /**
     * 完成报修（管理员）
     * 
     * @param repairId 报修ID
     * @param handleResult 处理结果
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Repair> completeRepair(Long repairId, String handleResult) {
        Repair repair = repairMapper.selectById(repairId);
        if (repair == null) {
            return Result.error("报修记录不存在");
        }
        
        if (Constants.REPAIR_STATUS_COMPLETED.equals(repair.getStatus())) {
            return Result.error("该报修已完成，无需重复操作");
        }
        
        // 更新报修状态为已完成
        repair.setStatus(Constants.REPAIR_STATUS_COMPLETED);
        repair.setHandleResult(handleResult);
        repair.setHandleTime(LocalDateTime.now());
        repairMapper.updateById(repair);
        
        // 将单车状态更新为可用
        Bike bike = bikeMapper.selectById(repair.getBikeId());
        if (bike != null) {
            bike.setStatus(Constants.BIKE_STATUS_AVAILABLE);
            bikeMapper.updateById(bike);
        }
        
        return Result.success("报修处理完成", repair);
    }
    
    /**
     * 删除报修记录
     * 
     * @param repairId 报修ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteRepair(Long repairId) {
        Repair repair = repairMapper.selectById(repairId);
        if (repair == null) {
            return Result.error("报修记录不存在");
        }
        
        // 只有待处理状态的报修可以删除
        if (!Constants.REPAIR_STATUS_PENDING.equals(repair.getStatus())) {
            return Result.error("只有待处理状态的报修记录可以删除");
        }
        
        // 如果单车状态是维修中，需要恢复为可用
        Bike bike = bikeMapper.selectById(repair.getBikeId());
        if (bike != null && Constants.BIKE_STATUS_MAINTENANCE.equals(bike.getStatus())) {
            bike.setStatus(Constants.BIKE_STATUS_AVAILABLE);
            bikeMapper.updateById(bike);
        }
        
        repairMapper.deleteById(repairId);
        return Result.success("删除成功", null);
    }
}
