import request from '@/utils/request'

/**
 * 管理员相关API
 */

/**
 * 获取用户列表（分页）
 * @param {Object} params 查询参数
 */
export function getUserList(params) {
  return request({
    url: 'admin/user/list',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 * @param {Number} userId 用户ID
 */
export function getUserDetail(userId) {
  return request({
    url: `admin/user/detail/${userId}`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {Object} data 用户信息
 */
export function updateUser(data) {
  return request({
    url: 'admin/user/update',
    method: 'put',
    data
  })
}

/**
 * 启用/禁用用户
 * @param {Object} data 状态数据
 */
export function updateUserStatus(data) {
  return request({
    url: 'admin/user/status',
    method: 'put',
    data
  })
}

/**
 * 用户充值
 * @param {Object} data 充值数据
 */
export function rechargeUser(data) {
  return request({
    url: 'admin/user/recharge',
    method: 'post',
    data
  })
}

/**
 * 获取单车列表（分页）
 * @param {Object} params 查询参数
 */
export function getBikeList(params) {
  return request({
    url: 'admin/bike/list',
    method: 'get',
    params
  })
}

/**
 * 添加单车
 * @param {Object} data 单车信息
 */
export function addBike(data) {
  return request({
    url: 'admin/bike/add',
    method: 'post',
    data
  })
}

/**
 * 更新单车信息
 * @param {Object} data 单车信息
 */
export function updateBike(data) {
  return request({
    url: 'admin/bike/update',
    method: 'put',
    data
  })
}

/**
 * 删除单车
 * @param {Number} bikeId 单车ID
 */
export function deleteBike(bikeId) {
  return request({
    url: `admin/bike/delete/${bikeId}`,
    method: 'delete'
  })
}

/**
 * 生成单车二维码
 * @param {Number} bikeId 单车ID
 */
export function generateQRCode(bikeId) {
  return request({
    url: `admin/bike/qrcode/${bikeId}`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取站点列表（分页）
 * @param {Object} params 查询参数
 */
export function getStationList(params) {
  return request({
    url: 'admin/station/list',
    method: 'get',
    params
  })
}

/**
 * 添加站点
 * @param {Object} data 站点信息
 */
export function addStation(data) {
  return request({
    url: 'admin/station/add',
    method: 'post',
    data
  })
}

/**
 * 更新站点信息
 * @param {Object} data 站点信息
 */
export function updateStation(data) {
  return request({
    url: 'admin/station/update',
    method: 'put',
    data
  })
}

/**
 * 删除站点
 * @param {Number} stationId 站点ID
 */
export function deleteStation(stationId) {
  return request({
    url: `admin/station/delete/${stationId}`,
    method: 'delete'
  })
}

/**
 * 获取订单列表（分页）
 * @param {Object} params 查询参数
 */
export function getOrderList(params) {
  return request({
    url: 'admin/order/list',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 * @param {Number} orderId 订单ID
 */
export function getOrderDetail(orderId) {
  return request({
    url: `admin/order/detail/${orderId}`,
    method: 'get'
  })
}

/**
 * 获取报修列表（分页）
 * @param {Object} params 查询参数
 */
export function getRepairList(params) {
  return request({
    url: 'admin/repair/list',
    method: 'get',
    params
  })
}

/**
 * 处理报修
 * @param {Object} data 处理数据
 */
export function handleRepair(data) {
  return request({
    url: 'admin/repair/handle',
    method: 'put',
    data
  })
}

/**
 * 获取统计数据
 */
export function getStatistics() {
  return request({
    url: 'admin/statistics/overview',
    method: 'get'
  })
}

/**
 * 上传单车图片
 * @param {FormData} formData 表单数据
 */
export function uploadBikeImage(formData) {
  return request({
    url: 'admin/bike/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传站点图片
 * @param {FormData} formData 表单数据
 */
export function uploadStationImage(formData) {
  return request({
    url: 'admin/station/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
