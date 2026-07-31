<template>
  <div class="order-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的订单</span>
          <el-button type="primary" @click="goBack">返回</el-button>
        </div>
      </template>

      <!-- 订单列表 -->
      <el-table
        :data="orderList"
        border
        style="width: 100%"
        :header-cell-style="{ textAlign: 'center', background: '#FFF8DC' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="bikeId" label="单车ID" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="scope">
            <span>{{ scope.row.endTime || '进行中' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="120">
          <template #default="scope">
            <span>{{ scope.row.duration || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalFee" label="费用(元)" width="100">
          <template #default="scope">
            <span v-if="scope.row.totalFee">{{ scope.row.totalFee.toFixed(2) }}</span>
            <span v-else>待结算</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="250">
          <template #default="scope">
            <el-button type="info" size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.status === 'IN_PROGRESS'" 
              type="success" 
              size="small" 
              @click="handleReturnBike(scope.row)"
            >
              还车
            </el-button>
            <el-button 
              v-if="scope.row.status === 'IN_PROGRESS'" 
              type="danger" 
              size="small" 
              @click="handleRepair(scope.row)"
            >
              报修
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadOrderList"
        @current-change="loadOrderList"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单编号">{{ orderDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="单车ID">{{ orderDetail.bikeId }}</el-descriptions-item>
        <el-descriptions-item label="起始站点ID">{{ orderDetail.startStationId || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="结束站点ID">{{ orderDetail.endStationId || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ orderDetail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ orderDetail.endTime || '进行中' }}</el-descriptions-item>
        <el-descriptions-item label="骑行时长">
          {{ orderDetail.duration ? orderDetail.duration + ' 分钟' : '进行中' }}
        </el-descriptions-item>
        <el-descriptions-item label="总费用">
          {{ orderDetail.totalFee ? orderDetail.totalFee.toFixed(2) + ' 元' : '待结算' }}
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(orderDetail.status)">
            {{ getStatusText(orderDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ orderDetail.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 报修对话框 -->
    <el-dialog v-model="repairVisible" title="提交报修" width="600px">
      <el-form :model="repairForm" :rules="repairRules" ref="repairFormRef" label-width="100px">
        <el-form-item label="单车ID">
          <el-input v-model="repairForm.bikeId" disabled />
        </el-form-item>
        
        <el-form-item label="故障类型" prop="faultType">
          <el-select v-model="repairForm.faultType" placeholder="请选择故障类型" style="width: 100%">
            <el-option label="刹车故障" value="刹车故障" />
            <el-option label="轮胎问题" value="轮胎问题" />
            <el-option label="链条故障" value="链条故障" />
            <el-option label="车座损坏" value="车座损坏" />
            <el-option label="车把故障" value="车把故障" />
            <el-option label="其他问题" value="其他问题" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input
            v-model="repairForm.faultDesc"
            type="textarea"
            :rows="4"
            placeholder="请详细描述故障情况"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="故障图片">
          <el-upload
            class="upload-demo"
            action="/api/repair/upload/image"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleRemove"
            :file-list="fileList"
            list-type="picture-card"
            :limit="3"
            name="file"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传3张图片，支持jpg、png格式</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="repairVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRepair">提交报修</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 数据
const orderList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const orderDetail = ref({})

// 报修相关
const repairVisible = ref(false)
const repairFormRef = ref(null)
const repairForm = reactive({
  bikeId: null,
  orderId: null,
  faultType: '',
  faultDesc: '',
  faultImage: ''
})

const repairRules = {
  faultType: [
    { required: true, message: '请选择故障类型', trigger: 'change' }
  ],
  faultDesc: [
    { required: true, message: '请输入故障描述', trigger: 'blur' },
    { min: 10, message: '故障描述至少10个字符', trigger: 'blur' }
  ]
}

// 文件上传
const fileList = ref([])
const uploadHeaders = {
  'User-Id': JSON.parse(localStorage.getItem('userInfo') || '{}').userId
}

// 加载订单列表
const loadOrderList = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await request.get('/order/list', {
      params: {
        userId: userInfo.userId,
        page: currentPage.value,
        size: pageSize.value
      }
    })
    if (res.code === 200) {
      orderList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  }
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

// 查看详情
const handleDetail = async (row) => {
  try {
    const res = await request.get(`/order/detail/${row.orderId}`)
    if (res.code === 200) {
      orderDetail.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 还车
const handleReturnBike = (row) => {
  // 跳转到还车页面，并传递订单ID
  router.push({
    path: '/user/return-bike',
    query: { orderId: row.orderId }
  })
}

// 打开报修对话框
const handleRepair = (row) => {
  repairForm.bikeId = row.bikeId
  repairForm.orderId = row.orderId
  repairForm.faultType = ''
  repairForm.faultDesc = ''
  repairForm.faultImage = ''
  fileList.value = []
  repairVisible.value = true
}

// 上传成功
const handleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    ElMessage.success('图片上传成功')
    // 收集所有已上传的图片路径
    const imagePaths = fileList
      .filter(f => f.response && f.response.code === 200)
      .map(f => f.response.data)
    repairForm.faultImage = imagePaths.join(',')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

// 移除图片
const handleRemove = (file, fileList) => {
  // 重新收集剩余图片路径
  const imagePaths = fileList
    .filter(f => f.response && f.response.code === 200)
    .map(f => f.response.data)
  repairForm.faultImage = imagePaths.join(',')
}

// 提交报修
const submitRepair = async () => {
  if (!repairFormRef.value) return
  
  await repairFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        const res = await request.post('/repair/submit', {
          userId: userInfo.userId,
          bikeId: repairForm.bikeId,
          faultType: repairForm.faultType,
          faultDesc: repairForm.faultDesc,
          faultImage: repairForm.faultImage
        })
        
        if (res.code === 200) {
          ElMessage.success('报修提交成功')
          repairVisible.value = false
          
          // 询问是否跳转到报修管理页面
          ElMessageBox.confirm(
            '报修已提交，是否前往报修管理页面查看？',
            '提示',
            {
              confirmButtonText: '前往查看',
              cancelButtonText: '留在此页',
              type: 'success'
            }
          ).then(() => {
            router.push('/user/repairs')
          }).catch(() => {
            // 用户选择留在当前页面
          })
        } else {
          ElMessage.error(res.message || '报修提交失败')
        }
      } catch (error) {
        console.error('提交报修失败:', error)
        ElMessage.error('报修提交失败')
      }
    }
  })
}

// 返回
const goBack = () => {
  router.back()
}

// 页面加载时获取数据
onMounted(() => {
  loadOrderList()
})
</script>

<style scoped>
.order-list-container {
  min-height: 100vh;
  background-color: var(--bg-light);
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-demo {
  display: inline-block;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>
