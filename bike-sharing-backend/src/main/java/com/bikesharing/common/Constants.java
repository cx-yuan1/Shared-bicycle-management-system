package com.bikesharing.common;

/**
 * 系统常量类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
public class Constants {
    
    // ==================== 用户角色 ====================
    /**
     * 普通用户角色
     */
    public static final String ROLE_USER = "USER";
    
    /**
     * 管理员角色
     */
    public static final String ROLE_ADMIN = "ADMIN";
    
    // ==================== 单车状态 ====================
    /**
     * 单车状态：可用
     */
    public static final String BIKE_STATUS_AVAILABLE = "AVAILABLE";
    
    /**
     * 单车状态：使用中
     */
    public static final String BIKE_STATUS_IN_USE = "IN_USE";
    
    /**
     * 单车状态：维修中
     */
    public static final String BIKE_STATUS_MAINTENANCE = "MAINTENANCE";
    
    // ==================== 订单状态 ====================
    /**
     * 订单状态：进行中
     */
    public static final String ORDER_STATUS_IN_PROGRESS = "IN_PROGRESS";
    
    /**
     * 订单状态：已完成
     */
    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    
    /**
     * 订单状态：已取消
     */
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    
    // ==================== 报修状态 ====================
    /**
     * 报修状态：待处理
     */
    public static final String REPAIR_STATUS_PENDING = "PENDING";
    
    /**
     * 报修状态：处理中
     */
    public static final String REPAIR_STATUS_PROCESSING = "PROCESSING";
    
    /**
     * 报修状态：已完成
     */
    public static final String REPAIR_STATUS_COMPLETED = "COMPLETED";
    
    // ==================== 文件上传路径 ====================
    /**
     * 头像上传路径
     */
    public static final String UPLOAD_PATH_AVATAR = "avatar/";
    
    /**
     * 单车图片上传路径
     */
    public static final String UPLOAD_PATH_BIKE = "bike/";
    
    /**
     * 站点图片上传路径
     */
    public static final String UPLOAD_PATH_STATION = "station/";
    
    /**
     * 报修图片上传路径
     */
    public static final String UPLOAD_PATH_REPAIR = "repair/";
}
