/**
 * 时间格式化工具函数
 */

/**
 * 格式化日期时间，去掉ISO 8601格式中的"T"字符
 * @param {string} dateTimeStr - ISO 8601格式的日期时间字符串，如 "2026-02-28T11:09:58"
 * @returns {string} 格式化后的日期时间字符串，如 "2026-02-28 11:09:58"
 */
export function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return ''
  
  // 如果包含"T"，替换为空格
  if (dateTimeStr.includes('T')) {
    // 处理格式：2026-02-28T11:09:58 或 2026-02-28T11:09:58.123
    return dateTimeStr.replace('T', ' ').split('.')[0]
  }
  
  return dateTimeStr
}

/**
 * 格式化日期，只保留日期部分
 * @param {string} dateTimeStr - 日期时间字符串
 * @returns {string} 格式化后的日期字符串，如 "2026-02-28"
 */
export function formatDate(dateTimeStr) {
  if (!dateTimeStr) return ''
  
  // 提取日期部分
  return dateTimeStr.split('T')[0].split(' ')[0]
}

/**
 * 格式化时间，只保留时间部分
 * @param {string} dateTimeStr - 日期时间字符串
 * @returns {string} 格式化后的时间字符串，如 "11:09:58"
 */
export function formatTime(dateTimeStr) {
  if (!dateTimeStr) return ''
  
  // 提取时间部分
  if (dateTimeStr.includes('T')) {
    return dateTimeStr.split('T')[1].split('.')[0]
  } else if (dateTimeStr.includes(' ')) {
    return dateTimeStr.split(' ')[1].split('.')[0]
  }
  
  return dateTimeStr
}
