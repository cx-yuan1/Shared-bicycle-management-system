<template>
  <div class="repair-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报修管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="请选择状态" clearable @clear="loadRepairList" size="large" style="width: 180px;">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input
            v-model="searchKeyword"
            placeholder="请输入故障类型或描述"
            clearable
            @clear="loadRepairList"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRepairList" size="large">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 报修列表 -->
      <el-table
        :data="repairList"
        border
        style="width: 100%"
        :header-cell-style="{ textAlign: 'center', background: '#FFF8DC' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="bikeNo" label="单车编号" width="120" />
        <el-table-column prop="faultType" label="故障类型" width="120" />
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
        <el-table-column label="状态" width="150">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="large">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报修时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="250" align="center">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PENDING'"
              type="primary"
              size="small"
              @click="handleAccept(scope.row)"
            >
              接单
            </el-button>
            <el-button
              v-if="scope.row.status === 'PROCESSING'"
              type="success"
              size="small"
              @click="handleComplete(scope.row)"
            >
              完成
            </el-button>
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRepairList"
        @current-change="loadRepairList"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="报修详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="报修ID">{{ repairDetail.repairId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ repairDetail.username }}</el-descriptions-item>
        <el-descriptions-item label="单车编号">{{ repairDetail.bikeNo }}</el-descriptions-item>
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
        <el-descriptions-item label="处理人ID">{{ repairDetail.handlerId || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="处理结果">{{ repairDetail.handleResult || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ repairDetail.handleTime ? formatDateTime(repairDetail.handleTime) : '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ formatDateTime(repairDetail.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 完成报修对话框 -->
    <el-dialog v-model="completeVisible" title="完成报修" width="500px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-input
            v-model="completeForm.handleResult"
            type="textarea"
            :rows="4"
            placeholder="请输入处理结果"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { formatDateTime } from '@/utils/dateFormat'

// 数据
const repairList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchStatus = ref('')
const searchKeyword = ref('')
const detailVisible = ref(false)
const completeVisible = ref(false)
const repairDetail = ref({})

// 完成表单
const completeForm = reactive({
  repairId: null,
  handleResult: ''
})

/**
 * 序号计算方法
 */
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 加载报修列表
const loadRepairList = async () => {
  try {
    const res = await request.get('/admin/repair/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        status: searchStatus.value,
        keyword: searchKeyword.value
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

// 接单处理
const handleAccept = (row) => {
  ElMessageBox.confirm(
    `确定要接单处理报修"${row.repairId}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 这里假设管理员ID为1，实际应该从登录信息中获取
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const handlerId = userInfo.userId || 1
      
      const res = await request.post('/admin/repair/accept', null, {
        params: {
          repairId: row.repairId,
          handlerId: handlerId
        }
      })
      if (res.code === 200) {
        ElMessage.success('接单成功')
        loadRepairList()
      } else {
        ElMessage.error(res.message || '接单失败')
      }
    } catch (error) {
      ElMessage.error('接单失败')
    }
  }).catch(() => {})
}

// 完成报修
const handleComplete = (row) => {
  completeForm.repairId = row.repairId
  completeForm.handleResult = ''
  completeVisible.value = true
}

// 提交完成
const submitComplete = async () => {
  if (!completeForm.handleResult) {
    ElMessage.warning('请输入处理结果')
    return
  }
  
  try {
    const res = await request.post('/admin/repair/complete', null, {
      params: {
        repairId: completeForm.repairId,
        handleResult: completeForm.handleResult
      }
    })
    if (res.code === 200) {
      ElMessage.success('报修处理完成')
      completeVisible.value = false
      loadRepairList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 查看详情
const handleDetail = async (row) => {
  try {
    const res = await request.get(`/admin/repair/detail/${row.repairId}`)
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
      const res = await request.delete(`/admin/repair/delete/${row.repairId}`)
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

// 页面加载时获取数据
onMounted(() => {
  loadRepairList()
})
</script>

<style scoped>
.repair-manage {
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
</style>
