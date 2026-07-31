<template>
  <div class="scan-ride-container">
    <el-card class="scan-card">
      <template #header>
        <div class="card-header">
          <span>扫码用车</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <div class="scan-content">
        <!-- 切换模式 -->
        <el-radio-group v-model="mode" class="mode-selector" @change="handleModeChange">
          <el-radio-button label="list">选择单车</el-radio-button>
          <el-radio-button label="scan">扫码用车</el-radio-button>
        </el-radio-group>

        <!-- 模式1：单车列表选择 -->
        <div v-if="mode === 'list'" class="bike-list-section">
          <el-row :gutter="20">
            <el-col
              v-for="bike in availableBikes"
              :key="bike.bikeId"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
            >
              <el-card class="bike-card" shadow="hover">
                <el-image
                  v-if="bike.bikeImage"
                  :src="getImageUrl(bike.bikeImage)"
                  style="width: 100%; height: 150px"
                  fit="cover"
                />
                <div v-else class="no-image">
                  <el-icon :size="60"><Bicycle /></el-icon>
                </div>
                <div class="bike-card-content">
                  <h4>{{ bike.bikeNo }}</h4>
                  <p>{{ bike.bikeType }}</p>
                  <p class="qr-code">二维码：{{ bike.qrCode }}</p>
                  <p class="price">¥{{ bike.pricePerHour }}/小时</p>
                  <el-button 
                    type="primary" 
                    size="small" 
                    style="width: 100%"
                    @click="selectBike(bike)"
                  >
                    选择此车
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <el-empty v-if="availableBikes.length === 0" description="暂无可用单车" />
        </div>

        <!-- 模式2：扫码输入 -->
        <div v-else class="scan-section">
          <el-form :model="rideForm" :rules="rules" ref="formRef" label-width="100px" class="ride-form">
            <el-form-item label="单车二维码" prop="qrCode">
              <el-input
                v-model="rideForm.qrCode"
                placeholder="请输入单车二维码（如：QR_BK001）"
                clearable
              >
                <template #append>
                  <el-button type="primary" @click="getBikeInfo">查询单车</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>

        <!-- 单车信息展示 -->
        <el-card v-if="bikeInfo.bikeId" class="bike-info-card" shadow="hover">
          <h3>单车信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="单车编号">{{ bikeInfo.bikeNo }}</el-descriptions-item>
            <el-descriptions-item label="单车类型">{{ bikeInfo.bikeType }}</el-descriptions-item>
            <el-descriptions-item label="单车状态">
              <el-tag :type="getStatusType(bikeInfo.status)">
                {{ getStatusText(bikeInfo.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="价格">{{ bikeInfo.pricePerHour }} 元/小时</el-descriptions-item>
            <el-descriptions-item label="所属站点">{{ bikeInfo.stationId || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="单车图片">
              <el-image
                v-if="bikeInfo.bikeImage"
                :src="getImageUrl(bikeInfo.bikeImage)"
                :preview-src-list="[getImageUrl(bikeInfo.bikeImage)]"
                style="width: 100px; height: 80px"
                fit="cover"
              />
              <span v-else>暂无图片</span>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 用户余额提示 -->
          <el-alert
            v-if="userInfo.balance !== undefined"
            :title="`当前余额：¥${userInfo.balance.toFixed(2)}`"
            type="info"
            :closable="false"
            style="margin-top: 20px"
          />

          <!-- 开始用车按钮 -->
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :disabled="!canStartRide"
              @click="showBookingDialog"
              style="width: 200px"
            >
              开始用车
            </el-button>
          </div>

          <!-- 提示信息 -->
          <el-alert
            v-if="!canStartRide"
            :title="getDisabledReason()"
            type="warning"
            :closable="false"
            style="margin-top: 20px"
          />
        </el-card>

        <!-- 使用说明 -->
        <el-card class="tips-card" shadow="never">
          <h3>使用说明</h3>
          <ul>
            <li v-if="mode === 'list'">从列表中选择可用的单车，点击"选择此车"</li>
            <li v-else>请输入单车上的二维码标识（如：QR_BK001）</li>
            <li>确认单车状态为"可用"后，点击"开始用车"</li>
            <li>系统会自动创建订单并开始计费</li>
            <li>计费标准：按小时计费，不足1小时按1小时计算</li>
            <li>请确保账户余额充足</li>
          </ul>
        </el-card>
      </div>
    </el-card>

    <!-- 预约用车对话框 -->
    <el-dialog
      v-model="bookingDialogVisible"
      title="预约用车"
      width="500px"
      :before-close="cancelBooking"
      @open="calculateEstimatedFee"
    >
      <div v-if="selectedBike" class="booking-content">
        <!-- 单车信息 -->
        <el-card class="bike-summary" shadow="never">
          <div class="bike-summary-content">
            <el-image
              v-if="selectedBike.bikeImage"
              :src="getImageUrl(selectedBike.bikeImage)"
              style="width: 100px; height: 80px; border-radius: 8px"
              fit="cover"
            />
            <div class="bike-summary-info">
              <h4>{{ selectedBike.bikeNo }}</h4>
              <p>{{ selectedBike.bikeType }}</p>
              <p class="price-info">¥{{ selectedBike.pricePerHour }}/小时</p>
            </div>
          </div>
        </el-card>

        <!-- 预约表单 -->
        <el-form :model="bookingForm" label-width="100px" style="margin-top: 20px">
          <el-form-item label="预计时长">
            <el-select
              v-model="bookingForm.duration"
              placeholder="请选择预计用车时长"
              style="width: 100%"
              @change="handleDurationChange"
            >
              <el-option
                v-for="option in durationOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="预付费用">
            <div class="fee-display">
              <span class="fee-amount">¥{{ bookingForm.estimatedFee.toFixed(2) }}</span>
              <span class="fee-tip">（实际费用以还车时为准，多退少补）</span>
            </div>
          </el-form-item>

          <el-form-item label="当前余额">
            <div class="balance-info">
              <span :class="{ 'insufficient': userInfo.balance < bookingForm.estimatedFee }">
                ¥{{ userInfo.balance.toFixed(2) }}
              </span>
              <span v-if="userInfo.balance < bookingForm.estimatedFee" class="warning-text">
                余额不足
              </span>
            </div>
          </el-form-item>
        </el-form>

        <!-- 温馨提示 -->
        <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          style="margin-top: 10px"
        >
          <ul class="tips-list">
            <li>系统将根据您选择的时长预先扣除费用</li>
            <li>实际还车时，按实际使用时长结算</li>
            <li>如实际时长少于预约时长，将退还差额</li>
            <li>如实际时长超过预约时长，将补扣差额</li>
          </ul>
        </el-alert>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelBooking">取消</el-button>
          <el-button
            type="primary"
            :disabled="userInfo.balance < bookingForm.estimatedFee"
            @click="confirmBooking"
          >
            确认用车
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bicycle } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref(null)

// 模式选择
const mode = ref('list')

// 可用单车列表
const availableBikes = ref([])

// 用户信息
const userInfo = ref({
  userId: null,
  balance: 0
})

// 骑行表单
const rideForm = reactive({
  qrCode: ''
})

// 单车信息
const bikeInfo = ref({})

// 预约对话框
const bookingDialogVisible = ref(false)
const selectedBike = ref(null)
const bookingForm = reactive({
  duration: 1, // 预计用车时长（小时）
  estimatedFee: 0 // 预估费用
})

// 时长选项
const durationOptions = [
  { label: '1小时', value: 1 },
  { label: '2小时', value: 2 },
  { label: '3小时', value: 3 },
  { label: '4小时', value: 4 },
  { label: '5小时', value: 5 },
  { label: '6小时', value: 6 }
]

// 表单验证规则
const rules = {
  qrCode: [
    { required: true, message: '请输入单车二维码', trigger: 'blur' }
  ]
}

// 是否可以开始用车
const canStartRide = computed(() => {
  return bikeInfo.value.bikeId &&
         bikeInfo.value.status === 'AVAILABLE' &&
         userInfo.value.balance > 0
})

// 获取单车信息
const getBikeInfo = async () => {
  if (!rideForm.qrCode) {
    ElMessage.warning('请输入单车二维码')
    return
  }

  try {
    const res = await request.get(`/bike/info/${rideForm.qrCode}`)
    if (res.code === 200) {
      bikeInfo.value = res.data
      ElMessage.success('单车信息获取成功')
    } else {
      ElMessage.error(res.message || '单车不存在')
      bikeInfo.value = {}
    }
  } catch (error) {
    ElMessage.error('获取单车信息失败')
    bikeInfo.value = {}
  }
}

// 开始用车
const startRide = () => {
  ElMessageBox.confirm(
    `确定要开始使用单车"${bikeInfo.value.bikeNo}"吗？`,
    '确认用车',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.post('/order/start', {
        userId: userInfo.value.userId,
        bikeId: bikeInfo.value.bikeId
      })
      
      if (res.code === 200) {
        ElMessage.success('用车成功，祝您骑行愉快！')
        // 跳转到还车页面
        setTimeout(() => {
          router.push({
            path: '/user/return-bike',
            query: { orderId: res.data.orderId }
          })
        }, 1500)
      } else {
        ElMessage.error(res.message || '用车失败')
      }
    } catch (error) {
      ElMessage.error('用车失败')
    }
  }).catch(() => {})
}

// 获取图片URL
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  // 如果路径已经包含/upload/，直接拼接
  if (path.startsWith('/upload/')) {
    return `http://localhost:8080${path}`
  }
  // 否则添加/upload/前缀
  return `http://localhost:8080/upload/${path}`
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'AVAILABLE': 'success',
    'IN_USE': 'warning',
    'MAINTENANCE': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'AVAILABLE': '可用',
    'IN_USE': '使用中',
    'MAINTENANCE': '维修中'
  }
  return textMap[status] || '未知'
}

// 获取不可用原因
const getDisabledReason = () => {
  if (!bikeInfo.value.bikeId) {
    return '请先查询单车信息'
  }
  if (bikeInfo.value.status !== 'AVAILABLE') {
    return '该单车当前不可用'
  }
  if (userInfo.value.balance <= 0) {
    return '账户余额不足，请先充值'
  }
  return ''
}

// 返回
const goBack = () => {
  router.back()
}

// 显示预约对话框（扫码模式）
const showBookingDialog = () => {
  selectedBike.value = bikeInfo.value
  bookingDialogVisible.value = true
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      const user = JSON.parse(storedUserInfo)
      const res = await request.get('/user/info')
      if (res.code === 200) {
        userInfo.value = res.data
      }
    }
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

// 加载可用单车列表
const loadAvailableBikes = async () => {
  try {
    const res = await request.get('/bike/list', {
      params: {
        status: 'AVAILABLE',
        page: 1,
        size: 100
      }
    })
    if (res.code === 200) {
      availableBikes.value = res.data.records || res.data
    }
  } catch (error) {
    ElMessage.error('加载单车列表失败')
  }
}

// 选择单车（显示预约对话框）
const selectBike = (bike) => {
  // 先检查余额
  if (userInfo.value.balance <= 0) {
    ElMessage.warning('账户余额不足，请先充值')
    return
  }

  // 设置选中的单车并显示预约对话框
  selectedBike.value = bike
  bookingDialogVisible.value = true
}

// 模式切换处理
const handleModeChange = () => {
  // 切换模式时清空单车信息
  bikeInfo.value = {}
  rideForm.qrCode = ''
}

// 计算预估费用
const calculateEstimatedFee = () => {
  if (selectedBike.value) {
    bookingForm.estimatedFee = selectedBike.value.pricePerHour * bookingForm.duration
  }
}

// 监听时长变化，自动计算费用
const handleDurationChange = () => {
  calculateEstimatedFee()
}

// 确认预约用车
const confirmBooking = async () => {
  // 检查余额是否足够
  if (userInfo.value.balance < bookingForm.estimatedFee) {
    ElMessage.warning('账户余额不足，请先充值')
    return
  }

  try {
    const res = await request.post('/order/start', {
      userId: userInfo.value.userId,
      bikeId: selectedBike.value.bikeId,
      plannedDuration: bookingForm.duration,
      prepaidFee: bookingForm.estimatedFee
    })
    
    if (res.code === 200) {
      ElMessage.success('用车成功，祝您骑行愉快！')
      bookingDialogVisible.value = false
      
      // 跳转到还车页面
      setTimeout(() => {
        router.push({
          path: '/user/return-bike',
          query: { orderId: res.data.orderId }
        })
      }, 1500)
    } else {
      ElMessage.error(res.message || '用车失败')
    }
  } catch (error) {
    console.error('用车失败:', error)
    ElMessage.error('用车失败，请稍后重试')
  }
}

// 取消预约
const cancelBooking = () => {
  bookingDialogVisible.value = false
  selectedBike.value = null
  bookingForm.duration = 1
  bookingForm.estimatedFee = 0
}

// 页面加载时获取用户信息和单车列表
onMounted(() => {
  loadUserInfo()
  loadAvailableBikes()
})
</script>

<style scoped>
.scan-ride-container {
  min-height: 100vh;
  background-color: var(--bg-light);
  padding: 20px;
}

.scan-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.scan-content {
  padding: 20px;
}

.ride-form {
  margin-bottom: 30px;
}

.bike-info-card {
  margin-bottom: 20px;
  background-color: #FFF8DC;
}

.bike-info-card h3 {
  margin-bottom: 20px;
  color: var(--text-color);
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.tips-card {
  background-color: #f5f7fa;
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

.mode-selector {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.bike-list-section {
  margin-bottom: 30px;
}

.bike-card {
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.bike-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.bike-card .no-image {
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
}

.bike-card-content {
  padding: 15px 0;
  text-align: center;
}

.bike-card-content h4 {
  margin: 10px 0;
  color: var(--text-color);
}

.bike-card-content p {
  margin: 5px 0;
  color: var(--text-secondary);
}

.bike-card-content .qr-code {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
}

.bike-card-content .price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
  margin: 10px 0;
}

.scan-section {
  margin-bottom: 30px;
}

.booking-content {
  padding: 10px 0;
}

.bike-summary {
  background-color: #FFF8DC;
  border: none;
}

.bike-summary-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.bike-summary-info h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.bike-summary-info p {
  margin: 5px 0;
  color: #606266;
}

.bike-summary-info .price-info {
  font-size: 16px;
  font-weight: bold;
  color: #D4A574;
}

.fee-display {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.fee-amount {
  font-size: 24px;
  font-weight: bold;
  color: #D4A574;
}

.fee-tip {
  font-size: 12px;
  color: #909399;
}

.balance-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
  color: #67C23A;
}

.balance-info .insufficient {
  color: #F56C6C;
}

.balance-info .warning-text {
  font-size: 14px;
  color: #F56C6C;
}

.tips-list {
  margin: 10px 0 0 0;
  padding-left: 20px;
  line-height: 1.8;
  font-size: 13px;
}

.tips-list li {
  margin-bottom: 5px;
}
</style>
