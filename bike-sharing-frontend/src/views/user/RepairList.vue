<template>
  <div class="repair-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的报修</span>
          <div>
            <el-button type="primary" @click="showSubmitDialog">提交报修</el-button>
            <el-button @click="goBack">返回</el-button>
          </div>
        </div>
      </template>

      <!-- 报修列表 -->
      <el-table
        :data="repairList"
        border
        style="width: 100%"
        :header-cell-style="{ textAlign: 'center', background: '#FFF8DC' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column type="index" label="序号" width="80" :index="indexMethod" />
        <el-table-column prop="bikeNo" label="单车编号" width="150" />
        <el-table-column prop="faultType" label="故障类型" width="150" />
        <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="故障图片" width="120">
          <template #default="scope">
            <div v-if="scope.row.faultImage" class="image-preview">
              <el-image
                :src="getFirstImage(scope.row.faultImage)"
                :preview-src-list="getImageList(scope.row.faultImage)"
                style="width: 80px; height: 60px"
                fit="cover"
              />
              <span v-if="getImageCount(scope.row.faultImage) > 1" class="image-count">
                +{{ getImageCount(scope.row.faultImage) - 1 }}
              </span>
            </div>
            <span v-else>暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报修时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="info" size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 'PENDING'"
              type="danger"
              size="small"
              @click="handleDelete(scope.row)"
            >
              删除
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
        @size-change="loadRepairList"
        @current-change="loadRepairList"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 提交报修对话框 -->
    <el-dialog v-model="submitVisible" title="提交报修" width="600px" @close="resetForm">
      <el-form :model="repairForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="单车ID" prop="bikeId">
          <el-input-number v-model="repairForm.bikeId" :min="1" placeholder="请输入单车ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="故障类型" prop="faultType">
          <el-select v-model="repairForm.faultType" placeholder="请选择故障类型" style="width: 100%">
            <el-option label="刹车故障" value="刹车故障" />
            <el-option label="轮胎问题" value="轮胎问题" />
            <el-option label="链条脱落" value="链条脱落" />
            <el-option label="车座损坏" value="车座损坏" />
            <el-option label="车铃故障" value="车铃故障" />
            <el-option label="其他问题" value="其他问题" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input
            v-model="repairForm.faultDesc"
            type="textarea"
            :rows="4"
            placeholder="请详细描述故障情况"
          />
        </el-form-item>
        <el-form-item label="故障图片">
          <el-upload
            class="image-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="repairForm.faultImage" :src="getImageUrl(repairForm.faultImage)" class="uploaded-image" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRepair">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="报修详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="报修编号">{{ repairDetail.repairId }}</el-descriptions-item>
        <el-descriptions-item label="单车编号">{{ repairDetail.bikeNo || repairDetail.bikeId }}</el-descriptions-item>
        <el-descriptions-item label="故障类型">{{ repairDetail.faultType }}</el-descriptions-item>
        <el-descriptions-item label="故障描述">{{ repairDetail.faultDesc }}</el-descriptions-item>
        <el-descriptions-item label="故障图片">
          <div v-if="repairDetail.faultImage" class="detail-images">
            <el-image
              v-for="(img, index) in getImageList(repairDetail.faultImage)"
              :key="index"
              :src="img"
              :preview-src-list="getImageList(repairDetail.faultImage)"
              :initial-index="index"
              style="width: 100px; height: 100px; margin-right: 10px"
              fit="cover"
            />
          </div>
          <span v-else>暂无图片</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(repairDetail.status)">
            {{ getStatusText(repairDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理结果">{{ repairDetail.handleResult || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ formatTime(repairDetail.handleTime) || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ formatTime(repairDetail.createTime) }}</el-descriptions-item>
      </el-descriptions>
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
const repairList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const submitVisible = ref(false)
const detailVisible = ref(false)
const repairDetail = ref({})
const formRef = ref(null)
const uploadUrl = ref('http://localhost:8080/api/repair/upload/image')

// 报修表单
const repairForm = reactive({
  userId: null,
  bikeId: null,
  faultType: '',
  faultDesc: '',
  faultImage: ''
})

// 表单验证规则
const rules = {
  bikeId: [
    { required: true, message: '请输入单车ID', trigger: 'blur' }
  ],
  faultType: [
    { required: true, message: '请选择故障类型', trigger: 'change' }
  ],
  faultDesc: [
    { required: true, message: '请输入故障描述', trigger: 'blur' }
  ]
}

// 加载报修列表
const loadRepairList = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await request.get('/repair/list', {
      params: {
        userId: userInfo.userId,
        page: currentPage.value,
        size: pageSize.value
      }
    })
    if (res.code === 200) {
      repairList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载报修列表失败')
  }
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

// 获取第一张图片
const getFirstImage = (images) => {
  if (!images) return ''
  const imageArray = images.split(',')
  return getImageUrl(imageArray[0])
}

// 获取图片列表
const getImageList = (images) => {
  if (!images) return []
  const imageArray = images.split(',')
  return imageArray.map(img => getImageUrl(img))
}

// 获取图片数量
const getImageCount = (images) => {
  if (!images) return 0
  return images.split(',').length
}

// 计算序号（考虑分页）
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 格式化时间（去掉T）
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ')
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'PROCESSING': 'primary',
    'COMPLETED': 'success'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成'
  }
  return textMap[status] || '未知'
}

// 显示提交对话框
const showSubmitDialog = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  repairForm.userId = userInfo.userId
  submitVisible.value = true
}

// 提交报修
const submitRepair = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await request.post('/repair/submit', repairForm)
        if (res.code === 200) {
          ElMessage.success('报修提交成功')
          submitVisible.value = false
          loadRepairList()
        } else {
          ElMessage.error(res.message || '提交失败')
        }
      } catch (error) {
        ElMessage.error('提交失败')
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(repairForm, {
    userId: null,
    bikeId: null,
    faultType: '',
    faultDesc: '',
    faultImage: ''
  })
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    repairForm.faultImage = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 查看详情
const handleDetail = async (row) => {
  try {
    const res = await request.get(`/repair/detail/${row.repairId}`)
    if (res.code === 200) {
      repairDetail.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 删除报修
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除报修记录"${row.repairId}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.delete(`/repair/delete/${row.repairId}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadRepairList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 返回
const goBack = () => {
  router.back()
}

// 页面加载时获取数据
onMounted(() => {
  loadRepairList()
})
</script>

<style scoped>
.repair-list-container {
  min-height: 100vh;
  background-color: var(--bg-light);
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-preview {
  position: relative;
  display: inline-block;
}

.image-count {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.image-uploader:hover {
  border-color: #409eff;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.uploaded-image {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
</style>
