<template>
  <div class="user-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="logo">
        <el-icon v-if="!isCollapse" :size="32"><Bicycle /></el-icon>
        <span v-if="!isCollapse" class="logo-text">共享单车</span>
        <el-icon v-else :size="24"><Bicycle /></el-icon>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        background-color="#FFF8DC"
        text-color="#666"
        active-text-color="#8B7355"
      >
        <el-menu-item index="/user/home">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <el-menu-item index="/user/profile">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        
        <el-menu-item index="/user/orders">
          <el-icon><List /></el-icon>
          <template #title>用车订单</template>
        </el-menu-item>
        
        <el-menu-item index="/user/repairs">
          <el-icon><Tools /></el-icon>
          <template #title>报修管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/user/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <div class="balance-info">
            <el-icon><Wallet /></el-icon>
            <span>余额：¥{{ userInfo.balance || 0 }}</span>
          </div>
          <span class="welcome-text">{{ userInfo.realName || userInfo.username }}</span>
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" :src="getAvatarUrl(userInfo.avatar)">
                <el-icon><User /></el-icon>
              </el-avatar>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import {
  User,
  List,
  Tools,
  HomeFilled,
  Bicycle,
  Fold,
  Expand,
  ArrowDown,
  Wallet
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 用户信息
const userInfo = ref({
  username: '',
  realName: '',
  avatar: '',
  balance: 0
})

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 当前页面标题
const currentTitle = computed(() => route.meta.title || '')

/**
 * 切换侧边栏折叠状态
 */
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

/**
 * 处理下拉菜单命令
 */
const handleCommand = (command) => {
  if (command === 'logout') {
    logout()
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}

/**
 * 退出登录
 */
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('已退出登录')
  router.push('/login')
}

/**
 * 获取头像URL
 */
const getAvatarUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  // 如果路径已经包含/upload/，直接拼接
  if (path.startsWith('/upload/')) {
    return `http://localhost:8080${path}`
  }
  // 否则添加/upload/前缀
  return `http://localhost:8080/upload/${path}`
}

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  const info = localStorage.getItem('userInfo')
  if (info) {
    userInfo.value = JSON.parse(info)
  }
  
  // 从服务器获取最新用户信息
  try {
    const res = await request.get('/user/info')
    if (res.code === 200) {
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

// 监听路由变化，刷新用户信息
watch(() => route.path, () => {
  loadUserInfo()
})

onMounted(() => {
  loadUserInfo()
  
  // 每30秒自动刷新一次用户信息
  setInterval(() => {
    loadUserInfo()
  }, 30000)
})
</script>

<style scoped>
.user-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background-color: var(--primary-color);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 20px;
  border-bottom: 1px solid #F5DEB3;
  color: #8B7355;
  font-weight: bold;
}

.logo-text {
  font-size: 18px;
  white-space: nowrap;
}

.el-menu {
  border-right: none;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s;
}

.collapse-icon:hover {
  color: #8B7355;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.balance-info {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 15px;
  background: linear-gradient(135deg, #FFF8DC 0%, #F5DEB3 100%);
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #8B7355;
}

.welcome-text {
  font-size: 14px;
  color: #606266;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.content {
  background-color: #f5f5f5;
  overflow-y: auto;
  padding: 20px;
}

/* 面包屑样式 */
:deep(.el-breadcrumb__inner) {
  color: #606266;
  font-weight: normal;
}

:deep(.el-breadcrumb__inner:hover) {
  color: #8B7355;
}

/* 菜单项激活样式 */
:deep(.el-menu-item.is-active) {
  background-color: #F5DEB3 !important;
}

:deep(.el-menu-item:hover) {
  background-color: #FAEBD7 !important;
}
</style>
