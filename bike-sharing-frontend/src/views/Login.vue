<template>
  <div class="unified-login-container">
    <div class="login-box">
      <h2 class="login-title">共享单车管理系统</h2>
      
      <!-- 身份选择标签 -->
      <div class="role-tabs">
        <div 
          class="role-tab" 
          :class="{ active: loginRole === 'user' }"
          @click="switchRole('user')"
        >
          <el-icon><User /></el-icon>
          <span>用户</span>
        </div>
        <div 
          class="role-tab" 
          :class="{ active: loginRole === 'admin' }"
          @click="switchRole('admin')"
        >
          <el-icon><UserFilled /></el-icon>
          <span>管理员</span>
        </div>
      </div>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            :placeholder="loginRole === 'admin' ? '请输入管理员账号' : '请输入用户名或手机号'"
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
        
        <div class="login-footer" v-if="loginRole === 'user'">
          <span>还没有账号？</span>
          <el-link type="primary" @click="goRegister">立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, UserFilled } from '@element-plus/icons-vue'
import { login } from '@/api/user'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

// 当前登录角色：user 或 admin
const loginRole = ref('user')

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

/**
 * 切换登录角色
 */
const switchRole = (role) => {
  loginRole.value = role
  // 清空表单
  loginForm.username = ''
  loginForm.password = ''
  // 清除验证
  loginFormRef.value?.clearValidate()
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
      
      // 根据选择的角色进行验证
      if (loginRole.value === 'admin') {
        // 管理员登录
        if (res.data.role !== 'ADMIN') {
          ElMessage.error('您不是管理员，无权访问')
          loading.value = false
          return
        }
        
        // 保存token和用户信息
        localStorage.setItem('token', res.data.token || 'mock-token')
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        
        ElMessage.success('登录成功')
        router.push('/admin/dashboard')
      } else {
        // 用户登录
        if (res.data.role === 'ADMIN') {
          ElMessage.warning('检测到您是管理员，请切换到管理员登录')
          loading.value = false
          return
        }
        
        // 保存token和用户信息
        localStorage.setItem('token', res.data.token || 'mock-token')
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        
        ElMessage.success('登录成功')
        router.push('/user/home')
      }
    } catch (error) {
      console.error('登录失败：', error)
      ElMessage.error('登录失败，请检查账号密码')
    } finally {
      loading.value = false
    }
  })
}

/**
 * 跳转到注册页
 */
const goRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.unified-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: url('https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=1920&q=80') center/cover no-repeat;
  position: relative;
}

.unified-login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.85);
}

.login-box {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.login-title {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  color: #8B7355;
  margin-bottom: 30px;
}

.role-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  background-color: #F5F5F5;
  padding: 5px;
  border-radius: 12px;
}

.role-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 16px;
  font-weight: 500;
  color: #666;
  background-color: transparent;
}

.role-tab:hover {
  background-color: #E8E8E8;
}

.role-tab.active {
  background: linear-gradient(135deg, #D4A574 0%, #C19A6B 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(212, 165, 116, 0.3);
}

.role-tab .el-icon {
  font-size: 20px;
}

.login-form {
  margin-top: 20px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.login-footer span {
  margin-right: 5px;
}

/* 按钮样式优化 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #D4A574 0%, #C19A6B 100%);
  border: none;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 2px;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #C19A6B 0%, #B8A88A 100%);
}

/* 输入框样式优化 */
:deep(.el-input__wrapper) {
  background-color: #FAFAFA;
  box-shadow: 0 0 0 1px #E8E8E8 inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #D4A574 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #D4A574 inset !important;
}
</style>
