<template>
  <div class="user-manage-container">
    <el-card class="content-card">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 18px; font-weight: bold;">用户管理</span>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名、手机号或姓名"
          style="width: 300px;"
          clearable
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
        <el-button @click="resetSearch" icon="Refresh">重置</el-button>
      </div>
      
      <!-- 用户列表表格 -->
      <el-table
        :data="userList"
        style="width: 100%; margin-top: 20px;"
        stripe
        border
        :header-cell-style="{ textAlign: 'center', background: '#FFF8DC' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.avatar"
              :src="getImageUrl(row.avatar)"
              style="width: 50px; height: 50px; border-radius: 50%;"
              fit="cover"
              :preview-src-list="[getImageUrl(row.avatar)]"
            />
            <el-avatar v-else :size="50" icon="User" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" align="center" />
        <el-table-column prop="realName" label="真实姓名" width="120" align="center" />
        <el-table-column prop="phone" label="手机号" width="130" align="center" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="账户余额" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #67C23A; font-weight: bold;">¥{{ row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="large">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="280" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button 
              :type="row.status === 1 ? 'warning' : 'success'" 
              size="small" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="success" size="small" @click="handleRecharge(row)">充值</el-button>
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
    
    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="600px">
      <div v-if="currentUser" style="display: flex; flex-direction: column; gap: 20px;">
        <!-- 头像区域 -->
        <div style="display: flex; justify-content: center; padding: 20px 0;">
          <el-image
            v-if="currentUser.avatar"
            :src="getImageUrl(currentUser.avatar)"
            style="width: 120px; height: 120px; border-radius: 50%; border: 3px solid #D4A574;"
            fit="cover"
            :preview-src-list="[getImageUrl(currentUser.avatar)]"
          />
          <el-avatar v-else :size="120" icon="User" style="border: 3px solid #D4A574;" />
        </div>
        
        <!-- 用户信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentUser.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ currentUser.realName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="currentUser.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ currentUser.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="账户余额">
            <span style="color: #67C23A; font-weight: bold;">¥{{ currentUser.balance }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
              {{ currentUser.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDateTime(currentUser.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    
    <!-- 充值对话框 -->
    <el-dialog v-model="rechargeDialogVisible" title="用户充值" width="400px">
      <el-form :model="rechargeForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="currentUser.username" disabled />
        </el-form-item>
        <el-form-item label="当前余额">
          <el-input v-model="currentUser.balance" disabled>
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="充值金额">
          <el-input v-model="rechargeForm.amount" type="number" placeholder="请输入充值金额">
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, User } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatDateTime } from '@/utils/dateFormat'

const searchKeyword = ref('')
const userList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailDialogVisible = ref(false)
const rechargeDialogVisible = ref(false)
const currentUser = ref({})
const rechargeForm = ref({
  amount: ''
})

/**
 * 序号计算方法
 */
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

/**
 * 获取用户列表
 */
const getUserList = async () => {
  try {
    const res = await request({
      url: '/admin/user/list',
      method: 'get',
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value
      }
    })
    
    userList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取用户列表失败：', error)
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  getUserList()
}

/**
 * 重置搜索
 */
const resetSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  getUserList()
}

/**
 * 分页大小改变
 */
const handleSizeChange = () => {
  getUserList()
}

/**
 * 页码改变
 */
const handlePageChange = () => {
  getUserList()
}

/**
 * 查看详情
 */
const handleViewDetail = (row) => {
  currentUser.value = { ...row }
  detailDialogVisible.value = true
}

/**
 * 切换用户状态
 */
const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户"${row.username}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request({
      url: '/admin/user/status',
      method: 'put',
      data: {
        userId: row.userId,
        status: row.status === 1 ? 0 : 1
      }
    })
    
    ElMessage.success(`${action}成功`)
    getUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改状态失败：', error)
    }
  }
}

/**
 * 充值
 */
const handleRecharge = (row) => {
  currentUser.value = { ...row }
  rechargeForm.value.amount = ''
  rechargeDialogVisible.value = true
}

/**
 * 确认充值
 */
const confirmRecharge = async () => {
  if (!rechargeForm.value.amount || rechargeForm.value.amount <= 0) {
    ElMessage.warning('请输入正确的充值金额')
    return
  }
  
  try {
    await request({
      url: '/admin/user/recharge',
      method: 'post',
      data: {
        userId: currentUser.value.userId,
        amount: rechargeForm.value.amount
      }
    })
    
    ElMessage.success('充值成功')
    rechargeDialogVisible.value = false
    getUserList()
  } catch (error) {
    console.error('充值失败：', error)
  }
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

onMounted(() => {
  getUserList()
})
</script>

<style scoped>
.user-manage-container {
  padding: 20px;
  background-color: var(--bg-light);
  min-height: 100vh;
}
</style>
