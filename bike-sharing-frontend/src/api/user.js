import request from '@/utils/request'

/**
 * 用户相关API
 */

/**
 * 用户注册
 * @param {Object} data 注册信息
 */
export function register(data) {
  return request({
    url: 'user/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 * @param {Object} data 登录信息
 */
export function login(data) {
  return request({
    url: 'user/login',
    method: 'post',
    data
  })
}

/**
 * 获取个人信息
 */
export function getUserInfo() {
  return request({
    url: 'user/info',
    method: 'get'
  })
}

/**
 * 修改个人信息
 * @param {Object} data 用户信息
 */
export function updateUserInfo(data) {
  return request({
    url: 'user/update',
    method: 'put',
    data
  })
}

/**
 * 上传头像
 * @param {FormData} formData 表单数据
 */
export function uploadAvatar(formData) {
  return request({
    url: 'user/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
