<template>
  <div class="bike-manage-container">
    <el-card class="content-card">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 18px; font-weight: bold;">单车管理</span>
          <el-button type="primary" @click="handleAdd" icon="Plus">添加单车</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索单车编号或类型"
          style="width: 250px;"
          clearable
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 150px;" clearable>
          <el-option label="可用" value="AVAILABLE" />
          <el-option label="使用中" value="IN_USE" />
          <el-option label="维修中" value="MAINTENANCE" />
        </el-select>
        <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
        <el-button @click="resetSearch" icon="Refresh">重置</el-button>
      </div>
      
      <!-- 单车列表表格 -->
      <el-table
        :data="bikeList"
        style="width: 100%; margin-top: 20px;"
        stripe
        border
        :header-cell-style="{ textAlign: 'center', background: '#FFF8DC' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
        <el-table-column prop="bikeNo" label="单车编号" width="120" align="center" />
        <el-table-column prop="bikeType" label="单车类型" width="120" align="center" />
        <el-table-column label="单车图片" width="100" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.bikeImage"
              :src="getImageUrl(row.bikeImage)"
              style="width: 50px; height: 50px;"
              fit="cover"
            />
            <span v-else style="color: #999;">无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="large">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pricePerHour" label="价格(元/小时)" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #67C23A; font-weight: bold;">¥{{ row.pricePerHour }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="qrCode" label="二维码标识" width="150" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="300" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button type="success" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" size="small" @click="handleGenerateQR(row)">二维码</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: center;"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="bikeForm" :rules="rules" ref="bikeFormRef" label-width="120px">
        <el-form-item label="单车编号" prop="bikeNo">
          <el-input v-model="bikeForm.bikeNo" placeholder="请输入单车编号" />
        </el-form-item>
        <el-form-item label="单车类型" prop="bikeType">
          <el-select v-model="bikeForm.bikeType" placeholder="请选择单车类型" style="width: 100%;">
            <el-option label="普通单车" value="普通单车" />
            <el-option label="电动单车" value="电动单车" />
          </el-select>
        </el-form-item>
        <el-form-item label="单车图片">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <img v-if="bikeForm.bikeImage" :src="getImageUrl(bikeForm.bikeImage)" class="avatar">
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="每小时价格" prop="pricePerHour">
          <el-input-number v-model="bikeForm.pricePerHour" :min="0" :precision="2" :step="0.5" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="bikeForm.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="维修中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSave">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="单车详情" width="600px">
      <el-descriptions :column="2" border v-if="currentBike">
        <el-descriptions-item label="单车ID">{{ currentBike.bikeId }}</el-descriptions-item>
        <el-descriptions-item label="单车编号">{{ currentBike.bikeNo }}</el-descriptions-item>
        <el-descriptions-item label="单车类型">{{ currentBike.bikeType }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentBike.status)">
            {{ getStatusText(currentBike.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ currentBike.pricePerHour }}/小时</el-descriptions-item>
        <el-descriptions-item label="二维码标识">{{ currentBike.qrCode }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(currentBike.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="单车图片" :span="2">
          <el-image
            v-if="currentBike.bikeImage"
            :src="getImageUrl(currentBike.bikeImage)"
            style="width: 200px; height: 200px;"
            fit="cover"
          />
          <span v-else>无图片</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatDateTime } from '@/utils/dateFormat'

const searchKeyword = ref('')
const statusFilter = ref('')
const bikeList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('添加单车')
const currentBike = ref({})
const bikeFormRef = ref(null)
const uploadUrl = '/api/admin/bike/upload/image'

const bikeForm = reactive({
  bikeId: null,
  bikeNo: '',
  bikeType: '',
  bikeImage: '',
  pricePerHour: 2.00,
  status: 'AVAILABLE'
})

const rules = {
  bikeNo: [
    { required: true, message: '请输入单车编号', trigger: 'blur' }
  ],
  bikeType: [
    { required: true, message: '请选择单车类型', trigger: 'change' }
  ],
  pricePerHour: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

/**
 * 序号计算方法
 */
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

/**
 * 获取单车列表
 */
const getBikeList = async () => {
  try {
    const res = await request({
      url: '/admin/bike/list',
      method: 'get',
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value,
        status: statusFilter.value
      }
    })
    
    bikeList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取单车列表失败：', error)
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  getBikeList()
}

/**
 * 重置搜索
 */
const resetSearch = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  currentPage.value = 1
  getBikeList()
}

/**
 * 分页大小改变
 */
const handleSizeChange = () => {
  getBikeList()
}

/**
 * 页码改变
 */
const handlePageChange = () => {
  getBikeList()
}

/**
 * 添加单车
 */
const handleAdd = () => {
  dialogTitle.value = '添加单车'
  Object.assign(bikeForm, {
    bikeId: null,
    bikeNo: '',
    bikeType: '',
    bikeImage: '',
    pricePerHour: 2.00,
    status: 'AVAILABLE'
  })
  dialogVisible.value = true
}

/**
 * 编辑单车
 */
const handleEdit = (row) => {
  dialogTitle.value = '编辑单车'
  Object.assign(bikeForm, { ...row })
  dialogVisible.value = true
}

/**
 * 查看详情
 */
const handleViewDetail = (row) => {
  currentBike.value = { ...row }
  detailDialogVisible.value = true
}

/**
 * 确认保存
 */
const confirmSave = async () => {
  if (!bikeFormRef.value) return
  
  await bikeFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      const url = bikeForm.bikeId ? '/admin/bike/update' : '/admin/bike/add'
      const method = bikeForm.bikeId ? 'put' : 'post'
      
      await request({
        url,
        method,
        data: bikeForm
      })
      
      ElMessage.success(bikeForm.bikeId ? '修改成功' : '添加成功')
      dialogVisible.value = false
      getBikeList()
    } catch (error) {
      console.error('保存失败：', error)
    }
  })
}

/**
 * 删除单车
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除单车"${row.bikeNo}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request({
      url: `/admin/bike/delete/${row.bikeId}`,
      method: 'delete'
    })
    
    ElMessage.success('删除成功')
    getBikeList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败：', error)
    }
  }
}

/**
 * 生成二维码
 */
const handleGenerateQR = async (row) => {
  try {
    const response = await fetch(`/api/admin/bike/generate-qr?bikeId=${row.bikeId}`, {
      method: 'POST'
    })
    
    if (response.ok) {
      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `bike_qr_${row.bikeNo}.png`
      a.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('二维码下载成功')
    } else {
      ElMessage.error('生成二维码失败')
    }
  } catch (error) {
    console.error('生成二维码失败：', error)
    ElMessage.error('生成二维码失败')
  }
}

/**
 * 图片上传成功
 */
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    bikeForm.bikeImage = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

/**
 * 上传前校验
 */
const beforeUpload = (file) => {
  const isImage = /^image\/(jpeg|png|gif|jpg)$/.test(file.type)
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

/**
 * 获取图片完整URL
 */
const getImageUrl = (path) => {
  if (!path) return ''
  // 如果已经是完整URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  // 否则拼接后端地址
  return `http://localhost:8080${path}`
}

/**
 * 获取状态类型
 */
const getStatusType = (status) => {
  const typeMap = {
    'AVAILABLE': 'success',
    'IN_USE': 'warning',
    'MAINTENANCE': 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    'AVAILABLE': '可用',
    'IN_USE': '使用中',
    'MAINTENANCE': '维修中'
  }
  return textMap[status] || status
}

onMounted(() => {
  getBikeList()
})
</script>

<style scoped>
.bike-manage-container {
  padding: 20px;
  background-color: var(--bg-light);
  min-height: 100vh;
}

.avatar-uploader .avatar {
  width: 150px;
  height: 150px;
  display: block;
  border-radius: 8px;
}

.avatar-uploader .avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 150px;
  height: 150px;
  text-align: center;
  line-height: 150px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
}

.avatar-uploader .avatar-uploader-icon:hover {
  border-color: var(--primary-color);
}


:deep(.el-image__preview) {
  cursor: default;
}

/* 表格图片样式 */
:deep(.el-table .el-image) {
  border-radius: 4px;
}
</style>
