package com.bikesharing.common;

import lombok.Data;

/**
 * 统一响应结果封装类
 * 
 * @author BikeSharing Team
 * @date 2026-02-28
 */
@Data
public class Result<T> {
    
    /**
     * 状态码：200成功，其他失败
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 成功响应（带数据）
     * 
     * @param data 响应数据
     * @return Result对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    
    /**
     * 成功响应（无数据）
     * 
     * @return Result对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 成功响应（自定义消息）
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @return Result对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    /**
     * 失败响应
     * 
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败响应（自定义状态码）
     * 
     * @param code 状态码
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
