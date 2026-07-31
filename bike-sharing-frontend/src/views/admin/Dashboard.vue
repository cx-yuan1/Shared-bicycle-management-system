<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2>数据统计</h2>
      <span class="update-time">更新时间：{{ updateTime }}</span>
    </div>
    
    <div class="dashboard-content">
      <!-- 统计卡片 -->
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #D4A574 0%, #C19A6B 100%);">
              <el-icon :size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">总用户数</div>
              <div class="stat-value">{{ statistics.totalUsers }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #B8A88A 0%, #A89968 100%);">
              <el-icon :size="30"><Bicycle /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">总单车数</div>
              <div class="stat-value">{{ statistics.totalBikes }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #E6C9A8 0%, #D4B896 100%);">
              <el-icon :size="30"><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">今日订单</div>
              <div class="stat-value">{{ statistics.todayOrders }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #DEB887 0%, #D2A679 100%);">
              <el-icon :size="30"><Tools /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">待处理报修</div>
              <div class="stat-value">{{ statistics.pendingRepairs }}</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>单车状态分布</span>
            </template>
            <div ref="bikeStatusChart" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>订单状态分布</span>
            </template>
            <div ref="orderStatusChart" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <el-card>
            <template #header>
              <span>近7天订单趋势</span>
            </template>
            <div ref="orderTrendChart" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Bicycle, List, Tools } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getStatistics } from '@/api/admin'

const router = useRouter()

// 更新时间
const updateTime = ref('')

// 统计数据
const statistics = ref({
  totalUsers: 0,
  totalBikes: 0,
  todayOrders: 0,
  pendingRepairs: 0
})

// 图表数据
const chartData = ref({
  bikeStatusDistribution: [],
  orderStatusDistribution: [],
  orderTrend: []
})

/**
 * 格式化当前时间
 */
const formatTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 图表引用
const bikeStatusChart = ref(null)
const orderStatusChart = ref(null)
const orderTrendChart = ref(null)

// 图表实例
let bikeStatusChartInstance = null
let orderStatusChartInstance = null
let orderTrendChartInstance = null

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    
    // 更新统计卡片数据
    statistics.value = {
      totalUsers: res.data.totalUsers || 0,
      totalBikes: res.data.totalBikes || 0,
      todayOrders: res.data.todayOrders || 0,
      pendingRepairs: res.data.pendingRepairs || 0
    }
    
    // 保存图表数据
    chartData.value = {
      bikeStatusDistribution: res.data.bikeStatusDistribution || [],
      orderStatusDistribution: res.data.orderStatusDistribution || [],
      orderTrend: res.data.orderTrend || []
    }
    
    // 更新图表
    updateBikeStatusChart()
    updateOrderStatusChart()
    updateOrderTrendChart()
    
  } catch (error) {
    console.error('加载统计数据失败：', error)
    ElMessage.error('加载统计数据失败')
  }
}

/**
 * 初始化单车状态分布图表
 */
const initBikeStatusChart = () => {
  if (!bikeStatusChart.value) return
  bikeStatusChartInstance = echarts.init(bikeStatusChart.value)
}

/**
 * 状态转换映射 - 单车状态
 */
const bikeStatusMap = {
  'AVAILABLE': '可用',
  'IN_USE': '使用中',
  'MAINTENANCE': '维修中',
  'SCRAPPED': '已报废'
}

/**
 * 更新单车状态分布图表
 */
const updateBikeStatusChart = () => {
  if (!bikeStatusChartInstance) return
  
  // 颜色映射
  const colorMap = {
    '可用': '#B8A88A',
    '使用中': '#E6C9A8',
    '维修中': '#DEB887',
    '已报废': '#C8B8A0'
  }
  
  // 转换数据格式，将英文状态转为中文
  const data = chartData.value.bikeStatusDistribution.map(item => {
    const chineseName = bikeStatusMap[item.name] || item.name
    return {
      value: item.value,
      name: chineseName,
      itemStyle: { color: colorMap[chineseName] || '#D4A574' }
    }
  })
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '单车状态',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  bikeStatusChartInstance.setOption(option)
}

/**
 * 初始化订单状态分布图表
 */
const initOrderStatusChart = () => {
  if (!orderStatusChart.value) return
  orderStatusChartInstance = echarts.init(orderStatusChart.value)
}

/**
 * 状态转换映射 - 订单状态
 */
const orderStatusMap = {
  'IN_PROGRESS': '进行中',
  'COMPLETED': '已完成',
  'CANCELLED': '已取消'
}

/**
 * 更新订单状态分布图表
 */
const updateOrderStatusChart = () => {
  if (!orderStatusChartInstance) return
  
  // 颜色映射
  const colorMap = {
    '进行中': '#E6C9A8',
    '已完成': '#B8A88A',
    '已取消': '#C8B8A0'
  }
  
  // 转换数据格式，将英文状态转为中文
  const data = chartData.value.orderStatusDistribution.map(item => {
    const chineseName = orderStatusMap[item.name] || item.name
    return {
      value: item.value,
      name: chineseName,
      itemStyle: { color: colorMap[chineseName] || '#D4A574' }
    }
  })
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  
  orderStatusChartInstance.setOption(option)
}

/**
 * 初始化订单趋势图表
 */
const initOrderTrendChart = () => {
  if (!orderTrendChart.value) return
  orderTrendChartInstance = echarts.init(orderTrendChart.value)
}

/**
 * 更新订单趋势图表
 */
const updateOrderTrendChart = () => {
  if (!orderTrendChartInstance) return
  
  // 提取日期和订单数
  const dates = chartData.value.orderTrend.map(item => {
    const date = new Date(item.date)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })
  const counts = chartData.value.orderTrend.map(item => item.count)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['订单数']
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '订单数',
        type: 'line',
        data: counts,
        smooth: true,
        itemStyle: { color: '#D4A574' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(212, 165, 116, 0.3)' },
            { offset: 1, color: 'rgba(212, 165, 116, 0.1)' }
          ])
        }
      }
    ]
  }
  
  orderTrendChartInstance.setOption(option)
}

/**
 * 窗口大小改变时重新调整图表
 */
const handleResize = () => {
  bikeStatusChartInstance?.resize()
  orderStatusChartInstance?.resize()
  orderTrendChartInstance?.resize()
}

onMounted(() => {
  // 设置更新时间
  updateTime.value = formatTime()
  
  // 延迟初始化图表，确保DOM已渲染
  setTimeout(() => {
    initBikeStatusChart()
    initOrderStatusChart()
    initOrderTrendChart()
    
    // 加载统计数据
    loadStatistics()
  }, 100)
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  bikeStatusChartInstance?.dispose()
  orderStatusChartInstance?.dispose()
  orderTrendChartInstance?.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-container {
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
  font-size: 14px;
  color: #909399;
}

.dashboard-content {
  width: 100%;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
</style>
