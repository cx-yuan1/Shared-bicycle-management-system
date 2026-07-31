import request from '@/utils/request'

/**
 * 订单相关API（用户端）
 */

/**
 * 创建订单（开始用车）
 * @param {Object} data 订单数据
 */
export function createOrder(data) {
  return request({
    url: 'order/create',
    method: 'post',
    data
  })
}

/**
 * 结束订单（还车）
 * @param {Object} data 还车数据
 */
export function finishOrder(data) {
  return request({
    url: 'order/finish',
    method: 'put',
    data
  })
}

/**
 * 获取我的订单列表
 * @param {Object} params 查询参数
 */
export function getMyOrders(params) {
  return request({
    url: 'order/my-list',
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
    url: `order/detail/${orderId}`,
    method: 'get'
  })
}

/**
 * 获取当前进行中的订单
 */
export function getCurrentOrder() {
  return request({
    url: 'order/current',
    method: 'get'
  })
}
