<template>
  <div class="home-container">
    <div class="page-header">
      <h2>欢迎使用共享单车系统</h2>
      <span class="update-time">{{ currentTime }}</span>
    </div>
    
    <div class="home-content">
      <!-- 用户信息卡片 -->
      <el-card class="user-info-card">
        <div class="user-info">
          <el-avatar :size="80" :src="getAvatarUrl(userInfo.avatar)">
            <el-icon :size="40"><User /></el-icon>
          </el-avatar>
          <div class="user-details">
            <h3>{{ userInfo.realName || userInfo.username }}</h3>
            <p>手机号：{{ userInfo.phone }}</p>
            <div class="balance-display">
              <el-icon><Wallet /></el-icon>
              <span>账户余额：¥{{ userInfo.balance || 0 }}</span>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 快捷功能 -->
      <div class="quick-actions">
        <el-card class="action-card" @click="goToPage('/user/scan-ride')">
          <div class="action-icon" style="background: linear-gradient(135deg, #D4A574 0%, #C19A6B 100%);">
            <el-icon :size="40"><Bicycle /></el-icon>
          </div>
          <div class="action-text">
            <h4>扫码用车</h4>
            <p>扫描二维码开始骑行</p>
          </div>
        </el-card>
        
        <el-card class="action-card" @click="goToPage('/user/return-bike')">
          <div class="action-icon" style="background: linear-gradient(135deg, #B8A88A 0%, #A89968 100%);">
            <el-icon :size="40"><Position /></el-icon>
          </div>
          <div class="action-text">
            <h4>还车</h4>
            <p>选择站点归还单车</p>
          </div>
        </el-card>
        
        <el-card class="action-card" @click="goToPage('/user/orders')">
          <div class="action-icon" style="background: linear-gradient(135deg, #E6C9A8 0%, #D4B896 100%);">
            <el-icon :size="40"><List /></el-icon>
          </div>
          <div class="action-text">
            <h4>用车订单</h4>
            <p>查看历史订单记录</p>
          </div>
        </el-card>
        
        <el-card class="action-card" @click="goToPage('/user/repairs')">
          <div class="action-icon" style="background: linear-gradient(135deg, #DEB887 0%, #D2A679 100%);">
            <el-icon :size="40"><Tools /></el-icon>
          </div>
          <div class="action-text">
            <h4>报修管理</h4>
            <p>提交或查看报修记录</p>
          </div>
        </el-card>
      </div>
      
      <!-- 使用说明 -->
      <el-card class="usage-guide">
        <template #header>
          <span>使用说明</span>
        </template>
        <el-steps :active="4" align-center>
          <el-step title="扫码用车" description="扫描单车二维码" />
          <el-step title="开始骑行" description="确认用车信息" />
          <el-step title="到达目的地" description="选择还车站点" />
          <el-step title="完成还车" description="系统自动计费" />
        </el-steps>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Bicycle, List, Tools, Position, Wallet } from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref({})
const currentTime = ref('')

/**
 * 获取头像URL
 */
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  // 如果路径已经包含/upload/，直接拼接
  if (avatar.startsWith('/upload/')) {
    return `http://localhost:8080${avatar}`
  }
  // 否则添加/upload/前缀
  return `http://localhost:8080/upload/${avatar}`
}

/**
 * 更新当前时间
 */
const updateTime = () => {
  const now = new Date()
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}`
}

onMounted(() => {
  // 获取用户信息
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    userInfo.value = JSON.parse(userInfoStr)
  }
  
  // 更新时间
  updateTime()
  setInterval(updateTime, 60000) // 每分钟更新一次
})

/**
 * 跳转页面
 */
const goToPage = (path) => {
  router.push(path)
}
</script>

<style scoped>
.home-container {
  min-height: calc(100vh - 100px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  color: #303133;
  margin: 0;
}

.update-time {
  font-size: 24px;
  font-weight: bold;
  color: #8B7355;
}

.home-content {
  width: 100%;
}

.user-info-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #FFF8DC 0%, #F5DEB3 100%);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-details {
  flex: 1;
}

.user-details h3 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 10px 0;
}

.user-details p {
  font-size: 14px;
  color: #606266;
  margin: 5px 0;
}

.balance-display {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  background-color: white;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 500;
  color: #8B7355;
  margin-top: 10px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.action-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.action-icon {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.action-text h4 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 8px 0;
}

.action-text p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.usage-guide {
  margin-top: 20px;
}

.usage-guide :deep(.el-step__title) {
  font-size: 14px;
}

.usage-guide :deep(.el-step__description) {
  font-size: 12px;
}
</style>
