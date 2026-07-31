package com.bikesharing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bikesharing.entity.RechargeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 充值记录Mapper接口
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Mapper
public interface RechargeRecordMapper extends BaseMapper<RechargeRecord> {
}
