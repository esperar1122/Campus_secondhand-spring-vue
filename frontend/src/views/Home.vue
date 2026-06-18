<template>
  <div class="home-container">
    <div class="header">
      <h1>校园二手交易平台</h1>
      <div class="header-right">
        <el-input v-model="keyword" placeholder="搜索商品" class="search-input" @keyup.enter="handleSearch">
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
        <el-button type="primary" @click="$router.push('/publish')">发布商品</el-button>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
          <el-button :icon="Bell" circle @click="handleNotificationClick2" />
        </el-badge>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <span>{{ userStore.username || '用户' }}</span>
            <span class="credit-score">信用分: {{ userStore.creditScore || 80 }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="my-items">我的商品</el-dropdown-item>
              <el-dropdown-item command="orders">我的订单</el-dropdown-item>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="admin" v-if="isAdmin">管理后台</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="content">
      <div class="items-grid">
        <div v-for="item in items" :key="item.id" class="item-card">
          <div class="item-image" @click="$router.push(`/item/${item.id}`)">
            <img :src="item.images?.[0]?.imageUrl || '/placeholder.png'" alt="" />
          </div>
          <div class="item-info" @click="$router.push(`/item/${item.id}`)">
            <h3>{{ item.title }}</h3>
            <p class="price">¥{{ item.price }}</p>
            <p class="time">{{ formatTime(item.createdAt) }}</p>
          </div>
          <div class="item-actions" v-if="item.sellerId !== userStore.userId">
            <el-button type="primary" size="small" @click.stop="handleQuickBuy(item)">购买</el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-if="items.length === 0 && !loading" description="暂无商品" />
      
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

    <el-dialog v-model="orderDialogVisible" title="确认订单" width="500px">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="商品">
          <span>{{ selectedItem?.title }} — ¥{{ selectedItem?.price }}</span>
        </el-form-item>
        <el-form-item label="交易方式">
          <el-radio-group v-model="orderForm.transactionType">
            <el-radio :value="0">校园面交</el-radio>
            <el-radio :value="1">自提</el-radio>
            <el-radio :value="2">送货上门</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="送货地址" v-if="orderForm.transactionType === 2">
          <el-input v-model="orderForm.deliveryAddress" placeholder="请输入送货地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">确认下单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showNotifications" title="我的通知" width="500px">
      <div class="notifications-list">
        <div 
          v-for="notification in notifications" 
          :key="notification.id" 
          class="notification-item"
          :class="{ unread: !notification.isRead }"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-content-wrapper">
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-content">{{ notification.content }}</div>
            <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
          </div>
          <el-button 
            type="danger" 
            size="small" 
            circle 
            :icon="Close" 
            @click.stop="handleDeleteNotification(notification.id)"
          />
        </div>
        <el-empty v-if="notifications.length === 0" description="暂无通知" />
      </div>
      <template #footer>
        <el-button @click="handleClearAllNotifications">清空全部</el-button>
        <el-button @click="handleMarkAllAsRead">全部标记已读</el-button>
        <el-button type="primary" @click="handleCloseNotifications">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { itemAPI, notificationAPI, orderAPI } from '@/api'
import { Search, Bell, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const items = ref([])
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(false)
const keyword = ref('')
const unreadCount = ref(0)
const notifications = ref([])
const showNotifications = ref(false)

const isAdmin = computed(() => userStore.isAdmin)

// 下单相关
const orderDialogVisible = ref(false)
const selectedItem = ref(null)
const orderForm = ref({
  transactionType: 0,
  deliveryAddress: ''
})

const handleQuickBuy = (item) => {
  selectedItem.value = item
  orderForm.value = { transactionType: 0, deliveryAddress: '' }
  orderDialogVisible.value = true
}

const submitOrder = async () => {
  try {
    const res = await orderAPI.createOrder({
      itemId: selectedItem.value.id,
      transactionType: orderForm.value.transactionType,
      deliveryAddress: orderForm.value.deliveryAddress
    })
    if (res.code === 200) {
      ElMessage.success('下单成功')
      orderDialogVisible.value = false
      loadItems()
    }
  } catch (error) {
    ElMessage.error(error.message || '下单失败')
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await notificationAPI.getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data.count
    }
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

const loadNotifications = async () => {
  try {
    const res = await notificationAPI.getNotifications()
    if (res.code === 200) {
      notifications.value = res.data.notifications
      unreadCount.value = res.data.unreadCount
    }
  } catch (error) {
    console.error('获取通知失败', error)
  }
}

const handleNotificationClick = async (notification) => {
  if (!notification.isRead) {
    await notificationAPI.markAsRead(notification.id)
    loadUnreadCount()
  }
  if (notification.relatedId) {
    showNotifications.value = false
    if (notification.type === 'order') {
      router.push('/orders?tab=seller')
    } else if (notification.type === 'review') {
      router.push(`/review?orderId=${notification.relatedId}`)
    } else {
      router.push(`/chat/${notification.relatedId}`)
    }
  }
}

const handleNotificationClick2 = async () => {
  showNotifications.value = true
  loadNotifications()
  startPolling()
}

let pollingTimer = null

const startPolling = () => {
  if (pollingTimer) clearInterval(pollingTimer)
  pollingTimer = setInterval(() => {
    loadNotifications()
    loadUnreadCount()
  }, 3000)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

const handleCloseNotifications = () => {
  showNotifications.value = false
  stopPolling()
}

const handleMarkAllAsRead = async () => {
  try {
    await notificationAPI.markAllAsRead()
    loadNotifications()
    ElMessage.success('已标记全部已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDeleteNotification = async (id) => {
  console.log('Deleting notification with id:', id, typeof id)
  try {
    const res = await notificationAPI.deleteNotification(id)
    console.log('Delete response:', res)
    if (res.code === 200) {
      loadNotifications()
      loadUnreadCount()
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('Delete error:', error)
    ElMessage.error('删除失败')
  }
}

const handleClearAllNotifications = async () => {
  try {
    for (const notification of notifications.value) {
      await notificationAPI.deleteNotification(notification.id)
    }
    loadNotifications()
    loadUnreadCount()
    ElMessage.success('已清空全部通知')
  } catch (error) {
    ElMessage.error('清空失败')
  }
}

const loadItems = async () => {
  loading.value = true
  try {
    const res = await itemAPI.getItems({ page: page.value, size: pageSize.value, keyword: keyword.value })
    if (res.code === 200) {
      items.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadItems()
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'admin') {
    router.push('/admin')
  } else {
    router.push(`/${command}`)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}-${date.getDate()}`
}

onMounted(() => {
  loadItems()
  if (userStore.isLoggedIn) {
    loadUnreadCount()
  }
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: #fff;
  padding: 20px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header h1 {
  font-size: 24px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-input {
  width: 300px;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.credit-score {
  font-size: 12px;
  color: #909399;
}

.content {
  padding: 20px 40px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.item-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.item-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #eee;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  padding: 15px;
}

.item-info h3 {
  font-size: 16px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}

.time {
  color: #909399;
  font-size: 12px;
}

.item-actions {
  padding: 0 15px 15px;
  display: flex;
  gap: 10px;
}

.item-actions .el-button {
  flex: 1;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.notifications-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.notification-item:hover {
  background: #f5f5f5;
}

.notification-item.unread {
  background: #e6f7ff;
}

.notification-content-wrapper {
  flex: 1;
}

.notification-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.notification-content {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.notification-time {
  color: #909399;
  font-size: 12px;
}
</style>