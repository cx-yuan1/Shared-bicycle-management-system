<template>
  <div class="admin-login-container">
    <div class="admin-login-box">
      <h2 class="admin-login-title">管理员登录</h2>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="admin-login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入管理员账号"
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
        
        <div class="admin-login-footer">
          <el-link type="primary" @click="goUserLogin">返回用户登录</el-link>
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
    { required: true, message: '请输入管理员账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
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
      if (res.data.role !== 'ADMIN') {
        ElMessage.error('您不是管理员，无权访问')
        return
      }
      
      // 保存token和用户信息
      localStorage.setItem('token', res.data.token || 'mock-token')
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      
      ElMessage.success('登录成功')
      
      // 跳转到管理后台
      router.push('/admin/dashboard')
    } catch (error) {
      console.error('登录失败：', error)
    } finally {
      loading.value = false
    }
  })
}

/**
 * 返回用户登录
 */
const goUserLogin = () => {
  router.push('/user/login')
}
</script>

<style scoped>
.admin-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.admin-login-box {
  width: 400px;
  padding: 40px;
  background-color: var(--bg-color);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.admin-login-title {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  color: var(--text-color);
  margin-bottom: 30px;
}

.admin-login-form {
  margin-top: 20px;
}

.admin-login-footer {
  text-align: center;
  margin-top: 20px;
}
</style>
