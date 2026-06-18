<template>
  <div class="orders-container">
    <div class="header">
      <el-button @click="$router.push('/home')">返回首页</el-button>
      <h2>我的订单</h2>
      <div></div>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="我买到的" name="buyer">
        <div class="orders-list">
          <template v-if="buyerOrders.length > 0">
            <div v-for="orderData in buyerOrders" :key="orderData.order?.id" class="order-card">
              <div class="order-header">
                <span>订单号: {{ orderData.order?.id }}</span>
                <span class="status">{{ getOrderStatus(orderData.order?.status) }}</span>
              </div>
              <div class="order-body">
                <p><strong>{{ orderData.itemTitle || '商品' }}</strong></p>
                <p>商品ID: {{ orderData.order?.itemId }} | 交易方式: {{ getTransactionType(orderData.order?.transactionType) }}</p>
                <p class="price">总价: ¥{{ orderData.order?.totalPrice }}</p>
                <p>创建时间: {{ formatTime(orderData.order?.createdAt) }}</p>
              </div>
              <div class="order-actions">
                <el-button size="small" @click="handleCancel(orderData.order)" v-if="orderData.order?.status === 0">取消订单</el-button>
                <el-button size="small" type="primary" @click="handleComplete(orderData.order)" v-if="orderData.order?.status === 1">确认收货</el-button>
                <el-button size="small" type="warning" @click="handleReview(orderData.order)" v-if="orderData.order?.status === 2 && !orderData.hasReviewed">评价</el-button>
                <el-tag v-if="orderData.order?.status === 2 && orderData.hasReviewed" type="success">已评价</el-tag>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无订单" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="我卖出的" name="seller">
        <div class="orders-list">
          <template v-if="sellerOrders.length > 0">
            <div v-for="orderData in sellerOrders" :key="orderData.order?.id" class="order-card">
              <div class="order-header">
                <span>订单号: {{ orderData.order?.id }}</span>
                <span class="status">{{ getOrderStatus(orderData.order?.status) }}</span>
              </div>
              <div class="order-body">
                <p><strong>{{ orderData.itemTitle || '商品' }}</strong></p>
                <p>商品ID: {{ orderData.order?.itemId }} | 交易方式: {{ getTransactionType(orderData.order?.transactionType) }}</p>
                <p class="price">总价: ¥{{ orderData.order?.totalPrice }}</p>
                <p>创建时间: {{ formatTime(orderData.order?.createdAt) }}</p>
              </div>
              <div class="order-actions">
                <el-button size="small" type="success" @click="handleConfirm(orderData.order)" v-if="orderData.order?.status === 0">确认订单</el-button>
                <el-button size="small" type="danger" @click="handleReject(orderData.order)" v-if="orderData.order?.status === 0">拒绝订单</el-button>
                <el-button size="small" type="warning" @click="handleReview(orderData.order)" v-if="orderData.order?.status === 2 && !orderData.hasReviewed">评价</el-button>
                <el-tag v-if="orderData.order?.status === 2 && orderData.hasReviewed" type="success">已评价</el-tag>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无订单" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const activeTab = ref('buyer')
const buyerOrders = ref([])
const sellerOrders = ref([])

const loadOrders = async () => {
  try {
    const [buyerRes, sellerRes] = await Promise.all([
      orderAPI.getBuyerOrders(),
      orderAPI.getSellerOrders()
    ])
    console.log('Buyer orders response:', buyerRes)
    console.log('Seller orders response:', sellerRes)
    if (buyerRes.code === 200) buyerOrders.value = buyerRes.data || []
    if (sellerRes.code === 200) sellerOrders.value = sellerRes.data || []
  } catch (error) {
    console.error('Load orders error:', error)
    ElMessage.error('加载订单失败')
  }
}

const getOrderStatus = (status) => {
  const texts = { 0: '待确认', 1: '待交易', 2: '已完成', 3: '已取消' }
  return texts[status] || '未知'
}

const getTransactionType = (type) => {
  const texts = { 0: '校园面交', 1: '自提', 2: '送货上门' }
  return texts[type] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const handleConfirm = async (order) => {
  try {
    await orderAPI.confirmOrder(order.id)
    ElMessage.success('确认成功')
    loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleReject = async (order) => {
  try {
    await ElMessageBox.confirm('确定要拒绝该订单吗？', '提示', { type: 'warning' })
    await orderAPI.rejectOrder(order.id)
    ElMessage.success('已拒绝')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await orderAPI.cancelOrder(order.id)
    ElMessage.success('已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleComplete = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示', { type: 'info' })
    await orderAPI.completeOrder(order.id)
    ElMessage.success('确认成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleReview = (order) => {
  router.push(`/review?orderId=${order.id}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-container {
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

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
}

.status {
  color: #409eff;
  font-weight: bold;
}

.order-body p {
  margin: 8px 0;
  color: #666;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.order-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}
</style>