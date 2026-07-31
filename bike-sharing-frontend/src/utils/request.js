import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * Axios请求封装
 * 统一处理请求和响应
 */

// 创建axios实例
const service = axios.create({
  baseURL: '/api',  // 通过vite代理转发到后端
  timeout: 5000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token（如果有）
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token
    }
    
    // 从localStorage获取用户信息
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      config.headers['User-Id'] = user.userId
    }
    
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 根据code判断请求是否成功
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // 如果是401未授权，跳转到登录页
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        window.location.href = '/login'
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误：', error)
    
    let message = '网络错误'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = error.response.data.message || '请求失败'
      }
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
