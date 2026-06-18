<template>
  <div class="my-items-container">
    <div class="header">
      <el-button @click="$router.push('/home')">返回首页</el-button>
      <h2>我的商品</h2>
      <el-button type="primary" @click="$router.push('/publish')">发布新商品</el-button>
    </div>

    <el-table :data="items" style="width: 100%">
      <el-table-column prop="title" label="商品标题" />
      <el-table-column prop="price" label="价格">
        <template #default="{ row }">
          ¥{{ row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="发布时间">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)" v-if="row.status === 0">编辑</el-button>
          <el-button size="small" type="danger" @click="handleOffline(row)" v-if="row.status === 0">下架</el-button>
          <el-button size="small" @click="$router.push(`/item/${row.id}`)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadItems"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { itemAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const items = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadItems = async () => {
  try {
    const res = await itemAPI.getMyItems({ page: page.value, size: pageSize.value })
    if (res.code === 200) {
      items.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const getStatusType = (status) => {
  const types = { 0: 'success', 1: 'warning', 2: 'info', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '在售', 1: '已锁定', 2: '已售出', 3: '已下架' }
  return texts[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const handleEdit = (item) => {
  router.push(`/edit-item/${item.id}`)
}

const handleOffline = async (item) => {
  try {
    await ElMessageBox.confirm('确定要下架该商品吗？', '提示', { type: 'warning' })
    await itemAPI.offlineItem(item.id)
    ElMessage.success('下架成功')
    loadItems()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '下架失败')
    }
  }
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.my-items-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>