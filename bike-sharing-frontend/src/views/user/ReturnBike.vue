<template>
  <div class="return-bike-container">
    <el-card class="return-card">
      <template #header>
        <div class="card-header">
          <span>还车</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <div class="return-content">
        <!-- 当前订单信息 -->
        <el-card v-if="orderInfo.orderId" class="order-info-card" shadow="hover">
          <h3>当前订单</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">{{ orderInfo.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="单车ID">{{ orderInfo.bikeId }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ orderInfo.startTime }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(orderInfo.status)">
                {{ getStatusText(orderInfo.status) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 骑行时长显示 -->
          <el-alert
            :title="`已骑行：${ridingDuration}`"
            type="info"
            :closable="false"
            style="margin-top: 20px"
          />

          <!-- 选择还车站点 -->
          <el-form :model="returnForm" :rules="rules" ref="formRef" label-width="120px" style="margin-top: 30px">
            <el-form-item label="还车站点" prop="endStationId">
              <el-select
                v-model="returnForm.endStationId"
                placeholder="请选择还车站点"
                style="width: 100%"
                @change="handleStationChange"
              >
                <el-option
                  v-for="station in stationList"
                  :key="station.stationId"
                  :label="`${station.stationName} (${station.address})`"
                  :value="station.stationId"
                />
              </el-select>
            </el-form-item>

            <!-- 站点信息 -->
            <el-form-item v-if="selectedStation.stationId" label="站点信息">
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="站点名称">{{ selectedStation.stationName }}</el-descriptions-item>
                <el-descriptions-item label="站点地址">{{ selectedStation.address }}</el-descriptions-item>
                <el-descriptions-item label="当前车辆数">
                  {{ selectedStation.currentCount }} / {{ selectedStation.capacity }}
                </el-descriptions-item>
              </el-descriptions>
            </el-form-item>
          </el-form>

          <!-- 费用预估 -->
          <el-card class="fee-card" shadow="never">
            <h4>费用预估</h4>
            <div class="fee-info">
              <div class="fee-item">
                <span>预计骑行时长：</span>
                <span class="fee-value">{{ estimatedDuration }} 分钟</span>
              </div>
              <div class="fee-item">
                <span>计费时长：</span>
                <span class="fee-value">{{ billingHours }} 小时</span>
              </div>
              <div class="fee-item">
                <span>预计费用：</span>
                <span class="fee-value highlight">¥{{ estimatedFee.toFixed(2) }}</span>
              </div>
            </div>
            <el-alert
              title="提示：费用按小时计算，不足1小时按1小时计算"
              type="warning"
              :closable="false"
              style="margin-top: 15px"
            />
          </el-card>

          <!-- 还车按钮 -->
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :disabled="!canReturn"
              @click="returnBike"
              style="width: 200px"
            >
              确认还车
            </el-button>
          </div>
        </el-card>

        <!-- 无订单提示 -->
        <el-empty v-else description="暂无进行中的订单">
          <el-button type="primary" @click="goToScanRide">去扫码用车</el-button>
        </el-empty>

        <!-- 使用说明 -->
        <el-card class="tips-card" shadow="never">
          <h3>还车说明</h3>
          <ul>
            <li>请选择还车站点</li>
            <li>确认费用无误后点击"确认还车"</li>
            <li>系统会自动计算骑行时长和费用</li>
            <li>费用将从账户余额中扣除</li>
            <li>还车后单车状态将更新为"可用"</li>
          </ul>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)

// 订单信息
const orderInfo = ref({})

// 站点列表
const stationList = ref([])

// 选中的站点
const selectedStation = ref({})

// 还车表单
const returnForm = reactive({
  endStationId: null
})

// 开始时间
const startTime = ref(null)

// 当前时间
const currentTime = ref(new Date())

// 表单验证规则
const rules = {
  endStationId: [
    { required: true, message: '请选择还车站点', trigger: 'change' }
  ]
}

// 骑行时长（分钟）
const ridingDuration = computed(() => {
  if (!startTime.value) return '0 分钟'
  const diff = Math.floor((currentTime.value - startTime.value) / 1000 / 60)
  const hours = Math.floor(diff / 60)
  const minutes = diff % 60
  if (hours > 0) {
    return `${hours} 小时 ${minutes} 分钟`
  }
  return `${minutes} 分钟`
})

// 预计骑行时长（分钟）
const estimatedDuration = computed(() => {
  if (!startTime.value) return 0
  return Math.floor((currentTime.value - startTime.value) / 1000 / 60)
})

// 计费时长（小时，向上取整）
const billingHours = computed(() => {
  return Math.ceil(estimatedDuration.value / 60)
})

// 预计费用
const estimatedFee = computed(() => {
  // 假设价格为2元/小时
  const pricePerHour = 2.0
  return billingHours.value * pricePerHour
})

// 是否可以还车
const canReturn = computed(() => {
  return orderInfo.value.orderId &&
         orderInfo.value.status === 'IN_PROGRESS' &&
         returnForm.endStationId
})

// 加载订单信息
const loadOrderInfo = async () => {
  try {
    const orderId = route.query.orderId
    if (orderId) {
      const res = await request.get(`/order/detail/${orderId}`)
      if (res.code === 200) {
        orderInfo.value = res.data
        startTime.value = new Date(res.data.startTime)
      }
    } else {
      // 查询用户当前进行中的订单
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const res = await request.get('/order/list', {
        params: {
          userId: userInfo.userId,
          page: 1,
          size: 10
        }
      })
      if (res.code === 200 && res.data.records.length > 0) {
        // 查找进行中的订单
        const inProgressOrder = res.data.records.find(order => order.status === 'IN_PROGRESS')
        if (inProgressOrder) {
          orderInfo.value = inProgressOrder
          startTime.value = new Date(inProgressOrder.startTime)
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载订单信息失败')
  }
}

// 加载站点列表
const loadStationList = async () => {
  try {
    const res = await request.get('/station/list')
    if (res.code === 200) {
      stationList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载站点列表失败')
  }
}

// 站点选择变化
const handleStationChange = (stationId) => {
  const station = stationList.value.find(s => s.stationId === stationId)
  if (station) {
    selectedStation.value = station
  }
}

// 还车
const returnBike = () => {
  if (!formRef.value) return
  
  formRef.value.validate(async (valid) => {
    if (valid) {
      ElMessageBox.confirm(
        `确定要在"${selectedStation.value.stationName}"还车吗？预计费用：¥${estimatedFee.value.toFixed(2)}`,
        '确认还车',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          const res = await request.post('/order/end', {
            endStationId: returnForm.endStationId
          })
          
          if (res.code === 200) {
            ElMessage.success(res.message || '还车成功！')
            // 显示费用详情
            ElMessageBox.alert(
              `骑行时长：${res.data.duration} 分钟\n实际费用：¥${res.data.totalFee.toFixed(2)}`,
              '还车成功',
              {
                confirmButtonText: '查看订单',
                callback: () => {
                  router.push('/user/orders')
                }
              }
            )
          } else {
            ElMessage.error(res.message || '还车失败')
          }
        } catch (error) {
          ElMessage.error('还车失败')
        }
      }).catch(() => {})
    }
  })
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return textMap[status] || '未知'
}

// 返回
const goBack = () => {
  router.back()
}

// 去扫码用车
const goToScanRide = () => {
  router.push('/user/scan-ride')
}

// 更新当前时间
const updateCurrentTime = () => {
  setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
}

// 页面加载时获取数据
onMounted(() => {
  loadOrderInfo()
  loadStationList()
  updateCurrentTime()
})
</script>

<style scoped>
.return-bike-container {
  min-height: 100vh;
  background-color: var(--bg-light);
  padding: 20px;
}

.return-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.return-content {
  padding: 20px;
}

.order-info-card {
  margin-bottom: 20px;
  background-color: #FFF8DC;
}

.order-info-card h3 {
  margin-bottom: 20px;
  color: var(--text-color);
}

.fee-card {
  background-color: #f5f7fa;
  margin-top: 20px;
}

.fee-card h4 {
  margin-bottom: 15px;
  color: var(--text-color);
}

.fee-info {
  padding: 10px 0;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #dcdfe6;
}

.fee-item:last-child {
  border-bottom: none;
  padding-top: 15px;
  font-size: 18px;
  font-weight: bold;
}

.fee-value {
  color: var(--text-color);
}

.fee-value.highlight {
  color: #f56c6c;
  font-size: 24px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.tips-card {
  background-color: #f5f7fa;
  margin-top: 20px;
}

.tips-card h3 {
  margin-bottom: 15px;
  color: var(--text-color);
}

.tips-card ul {
  padding-left: 20px;
  line-height: 2;
  color: var(--text-secondary);
}

.tips-card li {
  margin-bottom: 8px;
}
</style>
