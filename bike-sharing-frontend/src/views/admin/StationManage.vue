<template>
  <div class="station-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>站点管理</span>
          <el-button type="primary" @click="handleAdd">添加站点</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchKeyword"
            placeholder="请输入站点名称或地址"
            clearable
            @clear="loadStationList"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStationList">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 站点列表 -->
      <el-table
        :data="stationList"
        border
        style="width: 100%"
        :header-cell-style="{ textAlign: 'center', background: '#FFF8DC' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
        <el-table-column prop="stationName" label="站点名称" width="150" />
        <el-table-column prop="address" label="站点地址" min-width="200" />
        <el-table-column label="站点图片" width="120">
          <template #default="scope">
            <el-image
              v-if="scope.row.stationImage"
              :src="getImageUrl(scope.row.stationImage)"
              :preview-src-list="[getImageUrl(scope.row.stationImage)]"
              style="width: 80px; height: 60px"
              fit="cover"
            />
            <span v-else>暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="currentCount" label="当前车辆数" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="large">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
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
        @size-change="loadStationList"
        @current-change="loadStationList"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="stationForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="站点名称" prop="stationName">
          <el-input v-model="stationForm.stationName" placeholder="请输入站点名称" />
        </el-form-item>
        <el-form-item label="站点地址" prop="address">
          <el-input v-model="stationForm.address" placeholder="请输入站点地址" />
        </el-form-item>
        <el-form-item label="站点图片">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="stationForm.stationImage" :src="getImageUrl(stationForm.stationImage)" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number v-model="stationForm.longitude" :precision="6" :step="0.000001" placeholder="请输入经度" style="width: 100%" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number v-model="stationForm.latitude" :precision="6" :step="0.000001" placeholder="请输入纬度" style="width: 100%" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="stationForm.capacity" :min="1" placeholder="请输入站点容量" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="stationForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 数据
const stationList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('添加站点')
const formRef = ref(null)
const uploadUrl = ref('http://localhost:8080/api/admin/station/upload/image')

// 表单数据
const stationForm = reactive({
  stationId: null,
  stationName: '',
  address: '',
  stationImage: '',
  longitude: null,
  latitude: null,
  capacity: 10,
  status: 1
})

/**
 * 序号计算方法
 */
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 表单验证规则
const rules = {
  stationName: [
    { required: true, message: '请输入站点名称', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入站点地址', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入站点容量', trigger: 'blur' }
  ]
}

// 加载站点列表
const loadStationList = async () => {
  try {
    const res = await request.get('/admin/station/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value
      }
    })
    if (res.code === 200) {
      stationList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载站点列表失败')
  }
}

// 获取图片URL
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  // 如果路径已经包含 /upload/，直接拼接
  if (path.startsWith('/upload/')) {
    return `http://localhost:8080${path}`
  }
  // 否则添加 /upload/ 前缀
  return `http://localhost:8080/upload/${path}`
}

// 添加站点
const handleAdd = () => {
  dialogTitle.value = '添加站点'
  dialogVisible.value = true
}

// 编辑站点
const handleEdit = (row) => {
  dialogTitle.value = '编辑站点'
  Object.assign(stationForm, row)
  dialogVisible.value = true
}

// 删除站点
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除站点"${row.stationName}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.delete(`/admin/station/delete/${row.stationId}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadStationList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const url = stationForm.stationId ? '/admin/station/update' : '/admin/station/add'
        const method = stationForm.stationId ? 'put' : 'post'
        const res = await request[method](url, stationForm)
        
        if (res.code === 200) {
          ElMessage.success(stationForm.stationId ? '修改成功' : '添加成功')
          dialogVisible.value = false
          loadStationList()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(stationForm, {
    stationId: null,
    stationName: '',
    address: '',
    stationImage: '',
    longitude: null,
    latitude: null,
    capacity: 10,
    status: 1
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
    stationForm.stationImage = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadStationList()
})
</script>

<style scoped>
.station-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
</style>
