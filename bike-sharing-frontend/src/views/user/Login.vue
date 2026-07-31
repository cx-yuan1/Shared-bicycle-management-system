<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">共享单车管理系统</h2>
      <p class="login-subtitle">用户登录</p>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名或手机号"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="goRegister">立即注册</el-link>
          <el-divider direction="vertical" />
          <el-link type="primary" @click="goAdminLogin">管理员登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名或手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    
    try {
      const res = await login(loginForm)
      
      // 检查是否为管理员
      if (res.data.role === 'ADMIN') {
        ElMessage.warning('检测到您是管理员，请使用管理员登录入口')
        loading.value = false
        return
      }
      
      // 保存token和用户信息
      localStorage.setItem('token', res.data.token || 'mock-token')
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      
      ElMessage.success('登录成功')
      
      // 跳转到首页
      router.push('/user/home')
    } catch (error) {
      console.error('登录失败：', error)
    } finally {
      loading.value = false
    }
  })
}

/**
 * 跳转到注册页
 */
const goRegister = () => {
  router.push('/user/register')
}

/**
 * 跳转到管理员登录页
 */
const goAdminLogin = () => {
  router.push('/admin/login')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background-color: var(--bg-color);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  color: var(--text-color);
  margin-bottom: 10px;
}

.login-subtitle {
  text-align: center;
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 30px;
}

.login-form {
  margin-top: 20px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: var(--text-secondary);
}

.login-footer span {
  margin-right: 5px;
}
</style>
