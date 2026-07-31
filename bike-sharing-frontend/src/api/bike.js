import request from '@/utils/request'

/**
 * 单车相关API（用户端）
 */

/**
 * 根据二维码获取单车信息
 * @param {String} qrCode 二维码
 */
export function getBikeByQrCode(qrCode) {
  return request({
    url: `bike/info/${qrCode}`,
    method: 'get'
  })
}

/**
 * 获取附近可用单车列表
 * @param {Object} params 查询参数（经纬度、范围等）
 */
export function getNearbyBikes(params) {
  return request({
    url: 'bike/nearby',
    method: 'get',
    params
  })
}
