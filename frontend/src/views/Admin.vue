<template>
  <div class="admin-container">
    <div class="header">
      <h2>管理后台</h2>
    </div>

    <div class="dashboard">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.todayNewUsers }}</div>
            <div class="stat-label">今日新增用户</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.totalItems }}</div>
            <div class="stat-label">总商品数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-value">¥{{ stats.totalRevenue.toFixed(2) }}</div>
            <div class="stat-label">总成交额</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-card class="users-card">
      <h3>用户管理</h3>
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="creditScore" label="信用分">
          <template #default="{ row }">
            <el-tag :type="getCreditType(row.creditScore)">{{ row.creditScore }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="danger" 
              @click="handleBan(row)" 
              v-if="row.status === 0"
            >封禁</el-button>
            <el-button 
              size="small" 
              type="success" 
              @click="handleUnban(row)" 
              v-if="row.status === 1"
            >解封</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadUsers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = ref({
  totalUsers: 0,
  todayNewUsers: 0,
  totalItems: 0,
  todayNewItems: 0,
  totalRevenue: 0,
  todayRevenue: 0
})

const loadUsers = async () => {
  try {
    const res = await adminAPI.getUsers({ page: page.value, size: pageSize.value })
    if (res.code === 200) {
      users.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载用户失败')
  }
}

const loadStats = async () => {
  try {
    const res = await adminAPI.getDashboard()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const getCreditType = (score) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 70) return 'warning'
  return 'danger'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const handleBan = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要封禁用户 ${user.username} 吗？`, '提示', { type: 'warning' })
    await adminAPI.banUser(user.id)
    ElMessage.success('封禁成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleUnban = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要解封用户 ${user.username} 吗？`, '提示', { type: 'info' })
    await adminAPI.unbanUser(user.id)
    ElMessage.success('解封成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

onMounted(() => {
  loadUsers()
  loadStats()
})
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.dashboard {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  color: #666;
}

.users-card {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>