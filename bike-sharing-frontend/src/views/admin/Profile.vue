<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span style="font-size: 18px; font-weight: bold;">个人信息</span>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-image
              v-if="userInfo.avatar"
              :src="getImageUrl(userInfo.avatar)"
              class="avatar-image"
              fit="cover"
              :preview-src-list="[getImageUrl(userInfo.avatar)]"
            />
            <el-avatar v-else :size="120" icon="User" class="avatar-default" />
            <el-upload
              class="avatar-uploader-overlay"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
            >
              <div class="upload-overlay">
                <el-icon :size="24"><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </el-upload>
          </div>
          <div class="user-basic-info">
            <h2>{{ userInfo.realName || userInfo.username }}</h2>
            <p class="user-role">
              <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'primary'">
                {{ userInfo.role === 'ADMIN' ? '系统管理员' : '普通用户' }}
              </el-tag>
            </p>
          </div>
        </div>

        <!-- 信息展示与编辑 -->
        <el-divider />

        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form
              ref="basicFormRef"
              :model="basicForm"
              :rules="basicRules"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="用户名">
                <el-input v-model="userInfo.username" disabled />
              </el-form-item>
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="basicForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="basicForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="角色">
                <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'primary'">
                  {{ userInfo.role === 'ADMIN' ? '系统管理员' : '普通用户' }}
                </el-tag>
              </el-form-item>
              <el-form-item label="注册时间">
                <el-input :value="formatDateTime(userInfo.createTime)" disabled />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateBasicInfo">保存修改</el-button>
                <el-button @click="resetBasicForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 修改密码 -->
          <el-tab-pane label="修改密码" name="password">
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入原密码"
                  show-password
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updatePassword">修改密码</el-button>
                <el-button @click="resetPasswordForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Camera } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatDateTime } from '@/utils/dateFormat'

const activeTab = ref('basic')
const basicFormRef = ref(null)
const passwordFormRef = ref(null)
const uploadUrl = '/api/user/upload/avatar'

// 上传请求头
const uploadHeaders = ref({})

// 初始化上传请求头
const initUploadHeaders = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    const user = JSON.parse(userInfo)
    uploadHeaders.value = {
      'User-Id': user.userId
    }
  }
}

// 用户信息
const userInfo = ref({
  userId: '',
  username: '',
  realName: '',
  phone: '',
  avatar: '',
  role: '',
  balance: 0,
  createTime: ''
})

// 基本信息表单
const basicForm = reactive({
  realName: '',
  phone: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 基本信息验证规则
const basicRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  try {
    const localInfo = localStorage.getItem('userInfo')
    if (localInfo) {
      const info = JSON.parse(localInfo)
      
      // 从后端获取最新信息
      const res = await request.get('/user/info')
      if (res.code === 200) {
        userInfo.value = res.data
        basicForm.realName = res.data.realName
        basicForm.phone = res.data.phone
        
        // 更新本地存储
        localStorage.setItem('userInfo', JSON.stringify(res.data))
      }
    }
  } catch (error) {
    console.error('加载用户信息失败：', error)
  }
}

/**
 * 更新基本信息
 */
const updateBasicInfo = async () => {
  if (!basicFormRef.value) return
  
  await basicFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const res = await request.put('/user/update', {
        userId: userInfo.value.userId,
        realName: basicForm.realName,
        phone: basicForm.phone
      })
      
      if (res.code === 200) {
        ElMessage.success('信息更新成功')
        loadUserInfo()
      }
    } catch (error) {
      console.error('更新信息失败：', error)
    }
  })
}

/**
 * 重置基本信息表单
 */
const resetBasicForm = () => {
  basicForm.realName = userInfo.value.realName
  basicForm.phone = userInfo.value.phone
}

/**
 * 修改密码
 */
const updatePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const res = await request.put('/user/password', {
        userId: userInfo.value.userId,
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      
      if (res.code === 200) {
        ElMessage.success('密码修改成功，请重新登录')
        resetPasswordForm()
        
        // 延迟跳转到登录页
        setTimeout(() => {
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
        }, 1500)
      }
    } catch (error) {
      console.error('修改密码失败：', error)
    }
  })
}

/**
 * 重置密码表单
 */
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.resetFields()
}

/**
 * 头像上传成功
 */
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    userInfo.value.avatar = response.data
    ElMessage.success('头像上传成功')
    
    // 更新本地存储
    const localInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    localInfo.avatar = response.data
    localStorage.setItem('userInfo', JSON.stringify(localInfo))
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

/**
 * 上传前校验
 */
const beforeAvatarUpload = (file) => {
  const isImage = /^image\/(jpeg|png|gif|jpg)$/.test(file.type)
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  return true
}

/**
 * 获取图片完整URL
 */
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  return `http://localhost:8080${path}`
}

onMounted(() => {
  initUploadHeaders()
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background-color: var(--bg-light);
  min-height: calc(100vh - 100px);
}

.profile-card {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-content {
  padding: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 20px 0;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
}

.avatar-image {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 3px solid #D4A574;
}

.avatar-default {
  border: 3px solid #D4A574;
}

.avatar-uploader-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-overlay {
  width: 130px; /* 略大于头像框，确保覆盖圆边 */
  height: 130px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
  font-size: 14px;
  gap: 8px;
  border-radius: 50%;
}

.avatar-uploader-overlay:hover .upload-overlay {
  opacity: 1;
}

.user-basic-info h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.user-role {
  margin: 0;
}

.profile-tabs {
  margin-top: 20px;
}

.profile-form {
  max-width: 600px;
  margin-top: 20px;
}
</style>
