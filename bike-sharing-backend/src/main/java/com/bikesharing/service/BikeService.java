package com.bikesharing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bikesharing.common.Constants;
import com.bikesharing.common.Result;
import com.bikesharing.entity.Bike;
import com.bikesharing.mapper.BikeMapper;
import com.bikesharing.util.QRCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 单车服务类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Service
public class BikeService {
    
    @Autowired
    private BikeMapper bikeMapper;
    
    /**
     * 添加单车
     * 
     * @param bike 单车信息
     * @return 添加结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Bike> addBike(Bike bike) {
        // 生成二维码标识（使用单车编号）- 在校验之前先生成
        if (bike.getQrCode() == null || bike.getQrCode().isEmpty()) {
            bike.setQrCode("QR_" + bike.getBikeNo());
        }
        
        // 校验单车编号是否已存在
        LambdaQueryWrapper<Bike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bike::getBikeNo, bike.getBikeNo());
        Bike existBike = bikeMapper.selectOne(wrapper);
        if (existBike != null) {
            return Result.error("单车编号已存在");
        }
        
        // 设置默认状态
        if (bike.getStatus() == null || bike.getStatus().isEmpty()) {
            bike.setStatus(Constants.BIKE_STATUS_AVAILABLE);
        }
        
        bikeMapper.insert(bike);
        return Result.success("添加成功", bike);
    }
    
    /**
     * 分页查询单车列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词（单车编号）
     * @param status 状态筛选
     * @return 单车列表
     */
    public Result<Page<Bike>> getBikeList(Integer page, Integer size, String keyword, String status) {
        Page<Bike> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Bike> wrapper = new LambdaQueryWrapper<>();
        
        // 搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Bike::getBikeNo, keyword)
                    .or().like(Bike::getBikeType, keyword);
        }
        
        // 状态筛选
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Bike::getStatus, status);
        }
        
        wrapper.orderByDesc(Bike::getCreateTime);
        Page<Bike> result = bikeMapper.selectPage(pageParam, wrapper);
        
        return Result.success(result);
    }
    
    /**
     * 获取单车详情
     * 
     * @param bikeId 单车ID
     * @return 单车详情
     */
    public Result<Bike> getBikeDetail(Long bikeId) {
        Bike bike = bikeMapper.selectById(bikeId);
        if (bike == null) {
            return Result.error("单车不存在");
        }
        
        return Result.success(bike);
    }
    
    /**
     * 根据二维码查询单车
     * 
     * @param qrCode 二维码
     * @return 单车信息
     */
    public Result<Bike> getBikeByQrCode(String qrCode) {
        Bike bike = bikeMapper.selectByQrCode(qrCode);
        if (bike == null) {
            return Result.error("单车不存在");
        }
        
        return Result.success(bike);
    }
    
    /**
     * 更新单车信息
     * 
     * @param bike 单车信息
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Bike> updateBike(Bike bike) {
        Bike existBike = bikeMapper.selectById(bike.getBikeId());
        if (existBike == null) {
            return Result.error("单车不存在");
        }
        
        // 如果修改了单车编号，需要校验是否已被使用
        if (bike.getBikeNo() != null && !bike.getBikeNo().equals(existBike.getBikeNo())) {
            LambdaQueryWrapper<Bike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Bike::getBikeNo, bike.getBikeNo());
            Bike noBike = bikeMapper.selectOne(wrapper);
            if (noBike != null) {
                return Result.error("单车编号已被使用");
            }
        }
        
        bikeMapper.updateById(bike);
        
        // 返回更新后的单车信息
        Bike updatedBike = bikeMapper.selectById(bike.getBikeId());
        return Result.success("更新成功", updatedBike);
    }
    
    /**
     * 删除单车
     * 
     * @param bikeId 单车ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteBike(Long bikeId) {
        Bike bike = bikeMapper.selectById(bikeId);
        if (bike == null) {
            return Result.error("单车不存在");
        }
        
        // 校验单车状态，使用中的单车不能删除
        if (Constants.BIKE_STATUS_IN_USE.equals(bike.getStatus())) {
            return Result.error("该单车正在使用中，无法删除。请等待用户还车后再试。");
        }
        
        bikeMapper.deleteById(bikeId);
        return Result.success("删除成功", null);
    }
    
    /**
     * 生成单车二维码
     * 
     * @param bikeId 单车ID
     * @return 二维码图片字节数组
     */
    public Result<byte[]> generateQRCode(Long bikeId) {
        Bike bike = bikeMapper.selectById(bikeId);
        if (bike == null) {
            return Result.error("单车不存在");
        }
        
        try {
            // 生成二维码内容（包含单车二维码标识）
            String content = bike.getQrCode();
            byte[] qrCodeBytes = QRCodeUtil.generateQRCode(content);
            
            return Result.success(qrCodeBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成二维码失败：" + e.getMessage());
        }
    }
}
