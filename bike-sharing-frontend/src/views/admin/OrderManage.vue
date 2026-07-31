<template>
  <div class="order-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="请选择状态" clearable @clear="loadOrderList" size="large" style="width: 180px;">
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input
            v-model="searchKeyword"
            placeholder="请输入订单编号"
            clearable
            @clear="loadOrderList"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrderList" size="large">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 订单列表 -->
      <el-table
        :data="orderList"
        border
        style="width: 100%"
        :header-cell-style="{ textAlign: 'center', background: '#FFF8DC' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="bikeNo" label="单车编号" width="120" />
        <el-table-column prop="startStationName" label="起始站点" width="150" />
        <el-table-column prop="endStationName" label="结束站点" width="150" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="{ row }">
            {{ row.endTime ? formatDateTime(row.endTime) : '进行中' }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="totalFee" label="费用(元)" width="100">
          <template #default="scope">
            <span v-if="scope.row.totalFee">{{ scope.row.totalFee.toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="150">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="large">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" align="center">
          <template #default="scope">
            <el-button type="info" size="small" @click="handleDetail(scope.row)">详情</el-button>
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
        @size-change="loadOrderList"
        @current-change="loadOrderList"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单ID">{{ orderDetail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ orderDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ orderDetail.username }}</el-descriptions-item>
        <el-descriptions-item label="单车编号">{{ orderDetail.bikeNo }}</el-descriptions-item>
        <el-descriptions-item label="起始站点">{{ orderDetail.startStationName || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="结束站点">{{ orderDetail.endStationName || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(orderDetail.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ orderDetail.endTime ? formatDateTime(orderDetail.endTime) : '进行中' }}</el-descriptions-item>
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
        <el-descriptions-item label="创建时间">{{ formatDateTime(orderDetail.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { formatDateTime } from '@/utils/dateFormat'

// 数据
const orderList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchStatus = ref('')
const searchKeyword = ref('')
const detailVisible = ref(false)
const orderDetail = ref({})

/**
 * 序号计算方法
 */
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 加载订单列表
const loadOrderList = async () => {
  try {
    const res = await request.get('/admin/order/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        status: searchStatus.value,
        keyword: searchKeyword.value
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
    const res = await request.get(`/admin/order/detail/${row.orderId}`)
    if (res.code === 200) {
      orderDetail.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadOrderList()
})
</script>

<style scoped>
.order-manage {
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
</style>
