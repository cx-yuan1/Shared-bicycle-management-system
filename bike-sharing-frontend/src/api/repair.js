import request from '@/utils/request'

/**
 * 报修相关API（用户端）
 */

/**
 * 提交报修
 * @param {Object} data 报修数据
 */
export function submitRepair(data) {
  return request({
    url: 'repair/submit',
    method: 'post',
    data
  })
}

/**
 * 获取我的报修列表
 * @param {Object} params 查询参数
 */
export function getMyRepairs(params) {
  return request({
    url: 'repair/my-list',
    method: 'get',
    params
  })
}

/**
 * 获取报修详情
 * @param {Number} repairId 报修ID
 */
export function getRepairDetail(repairId) {
  return request({
    url: `repair/detail/${repairId}`,
    method: 'get'
  })
}

/**
 * 上传报修图片
 * @param {FormData} formData 表单数据
 */
export function uploadRepairImage(formData) {
  return request({
    url: 'repair/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
