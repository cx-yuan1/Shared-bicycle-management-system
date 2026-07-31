<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
          <el-button type="primary" @click="logout">退出登录</el-button>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="userInfo.avatar" :src="getAvatarUrl(userInfo.avatar)" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="username">{{ userInfo.username }}</div>
        </div>

        <!-- 用户信息 -->
        <el-form :model="userInfo" label-width="100px" class="user-form">
          <el-form-item label="用户名">
            <el-input v-model="userInfo.username" disabled />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="userInfo.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="userInfo.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="账户余额">
            <el-input v-model="userInfo.balance" disabled>
              <template #append>元</template>
            </el-input>
          </el-form-item>
          <el-form-item label="充值">
            <el-button type="success" icon="Wallet" @click="showRechargeDialog">账户充值</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updateProfile">保存修改</el-button>
            <el-button @click="loadUserInfo">取消</el-button>
          </el-form-item>
        </el-form>

        <!-- 快捷功能 -->
        <el-divider>快捷功能</el-divider>
        <div class="quick-actions">
          <el-button type="success" icon="Bicycle" @click="goToPage('/user/scan-ride')">扫码用车</el-button>
          <el-button type="warning" icon="List" @click="goToPage('/user/orders')">我的订单</el-button>
          <el-button type="danger" icon="Tools" @click="goToPage('/user/repairs')">我的报修</el-button>
          <el-button type="primary" icon="Wallet" @click="showRechargeHistory">充值记录</el-button>
        </div>
      </div>
    </el-card>

    <!-- 充值对话框 -->
    <el-dialog v-model="rechargeVisible" title="账户充值" width="500px" @close="resetRechargeForm">
      <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-width="100px">
        <el-form-item label="当前余额">
          <el-text size="large" type="primary">¥{{ userInfo.balance.toFixed(2) }}</el-text>
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="1"
            :max="10000"
            :step="10"
            :precision="2"
            placeholder="请输入充值金额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="快捷金额">
          <el-button-group>
            <el-button @click="rechargeForm.amount = 10">¥10</el-button>
            <el-button @click="rechargeForm.amount = 50">¥50</el-button>
            <el-button @click="rechargeForm.amount = 100">¥100</el-button>
            <el-button @click="rechargeForm.amount = 200">¥200</el-button>
            <el-button @click="rechargeForm.amount = 500">¥500</el-button>
          </el-button-group>
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-radio-group v-model="rechargeForm.paymentMethod">
            <el-radio label="alipay">支付宝</el-radio>
            <el-radio label="wechat">微信支付</el-radio>
            <el-radio label="bank">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert
          title="提示：本系统为演示系统，充值操作会直接增加账户余额，无需实际支付"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        />
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRecharge">确认充值</el-button>
      </template>
    </el-dialog>

    <!-- 充值记录对话框 -->
    <el-dialog v-model="rechargeHistoryVisible" title="充值记录" width="800px">
      <el-table
        :data="rechargeHistory"
        border
        style="width: 100%"
        :header-cell-style="{ textAlign: 'center', background: '#FFF8DC' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column type="index" label="序号" width="80" :index="indexMethod" />
        <el-table-column prop="amount" label="充值金额" min-width="120">
          <template #default="scope">
            <el-text type="success" size="large">+¥{{ scope.row.amount.toFixed(2) }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" min-width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.paymentMethod === 'alipay'" type="primary">支付宝</el-tag>
            <el-tag v-else-if="scope.row.paymentMethod === 'wechat'" type="success">微信支付</el-tag>
            <el-tag v-else type="info">银行卡</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balanceBefore" label="充值前余额" min-width="120">
          <template #default="scope">
            ¥{{ scope.row.balanceBefore.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="充值后余额" min-width="120">
          <template #default="scope">
            ¥{{ scope.row.balanceAfter.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="充值时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="rechargePage"
        v-model:page-size="rechargePageSize"
        :total="rechargeTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRechargeHistory"
        @current-change="loadRechargeHistory"
        style="margin-top: 20px; justify-content: center"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Wallet } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatDateTime } from '@/utils/dateFormat'

const router = useRouter()

// 用户信息
const userInfo = ref({
  userId: null,
  username: '',
  realName: '',
  phone: '',
  avatar: '',
  balance: 0
})

const uploadUrl = ref('http://localhost:8080/api/user/upload/avatar')
const uploadHeaders = ref({})

// 初始化上传请求头
const initUploadHeaders = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    const user = JSON.parse(userInfo)
    uploadHeaders.value = {
      'User-Id': user.userId
    }
  }
}

// 充值相关
const rechargeVisible = ref(false)
const rechargeHistoryVisible = ref(false)
const rechargeFormRef = ref(null)
const rechargeForm = reactive({
  amount: 50,
  paymentMethod: 'alipay'
})
const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '充值金额范围为1-10000元', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

// 充值记录
const rechargeHistory = ref([])
const rechargePage = ref(1)
const rechargePageSize = ref(10)
const rechargeTotal = ref(0)

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      const user = JSON.parse(storedUserInfo)
      const res = await request.get('/user/info')
      if (res.code === 200) {
        userInfo.value = res.data
        // 更新本地存储
        localStorage.setItem('userInfo', JSON.stringify(res.data))
      }
    }
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

// 获取头像URL
const getAvatarUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  // 如果路径已经包含/upload/，直接拼接
  if (path.startsWith('/upload/')) {
    return `http://localhost:8080${path}`
  }
  // 否则添加/upload/前缀
  return `http://localhost:8080/upload/${path}`
}

// 更新个人信息
const updateProfile = async () => {
  try {
    const res = await request.put('/user/update', userInfo.value)
    if (res.code === 200) {
      ElMessage.success('修改成功')
      // 更新本地存储
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      loadUserInfo()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

// 上传前校验
const beforeAvatarUpload = (file) => {
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
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    userInfo.value.avatar = response.data
    // 更新本地存储
    const storedUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    storedUserInfo.avatar = response.data
    localStorage.setItem('userInfo', JSON.stringify(storedUserInfo))
    ElMessage.success('头像上传成功')
    // 重新加载用户信息以确保显示最新数据
    loadUserInfo()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 退出登录
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('已退出登录')
  router.push('/user/login')
}

// 跳转页面
const goToPage = (path) => {
  router.push(path)
}

// 显示充值对话框
const showRechargeDialog = () => {
  rechargeVisible.value = true
}

// 重置充值表单
const resetRechargeForm = () => {
  rechargeForm.amount = 50
  rechargeForm.paymentMethod = 'alipay'
  if (rechargeFormRef.value) {
    rechargeFormRef.value.resetFields()
  }
}

// 处理充值
const handleRecharge = async () => {
  if (!rechargeFormRef.value) return
  
  await rechargeFormRef.value.validate(async (valid) => {
    if (valid) {
      ElMessageBox.confirm(
        `确认充值 ¥${rechargeForm.amount.toFixed(2)} 吗？`,
        '确认充值',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          const res = await request.post('/user/recharge', {
            amount: rechargeForm.amount,
            paymentMethod: rechargeForm.paymentMethod
          })
          
          if (res.code === 200) {
            ElMessage.success('充值成功！')
            rechargeVisible.value = false
            loadUserInfo()
            
            // 显示充值成功详情
            ElMessageBox.alert(
              `充值金额：¥${rechargeForm.amount.toFixed(2)}\n当前余额：¥${res.data.balance.toFixed(2)}`,
              '充值成功',
              {
                confirmButtonText: '查看充值记录',
                callback: () => {
                  showRechargeHistory()
                }
              }
            )
          } else {
            ElMessage.error(res.message || '充值失败')
          }
        } catch (error) {
          ElMessage.error('充值失败')
        }
      }).catch(() => {})
    }
  })
}

// 显示充值记录
const showRechargeHistory = () => {
  rechargeHistoryVisible.value = true
  loadRechargeHistory()
}

// 加载充值记录
const loadRechargeHistory = async () => {
  try {
    const res = await request.get('/user/recharge/history', {
      params: {
        page: rechargePage.value,
        size: rechargePageSize.value
      }
    })
    if (res.code === 200) {
      rechargeHistory.value = res.data.records
      rechargeTotal.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载充值记录失败')
  }
}

// 序号计算
const indexMethod = (index) => {
  return (rechargePage.value - 1) * rechargePageSize.value + index + 1
}

// 页面加载时获取数据
onMounted(() => {
  initUploadHeaders()
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background-color: var(--bg-light);
  padding: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-content {
  padding: 20px;
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: block;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  transition: border-color 0.3s;
}

.avatar-uploader-icon:hover {
  border-color: #409eff;
}

.username {
  margin-top: 10px;
  font-size: 18px;
  font-weight: bold;
  color: var(--text-color);
}

.user-form {
  max-width: 500px;
  margin: 0 auto;
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.quick-actions .el-button {
  width: 150px;
  height: 50px;
  font-size: 16px;
}
</style>
