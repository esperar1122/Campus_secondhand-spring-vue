<template>
  <div class="chat-container">
    <div class="chat-header">
      <el-button @click="$router.back()">返回</el-button>
      <div class="chat-title" v-if="item">
        <span>与卖家 {{ sellerInfo?.username }} 沟通中</span>
        <span class="item-title">商品: {{ item.title }}</span>
      </div>
    </div>

    <div class="chat-content" v-if="item">
      <div class="item-info-card">
        <div class="item-image">
          <img :src="images[0]?.imageUrl || '/placeholder.png'" alt="" />
        </div>
        <div class="item-details">
          <h3>{{ item.title }}</h3>
          <p class="price">¥{{ item.price }}</p>
          <p class="description">{{ item.description?.substring(0, 100) }}...</p>
        </div>
      </div>

      <div class="messages-area">
        <div class="messages-list" ref="messagesListRef">
          <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ 'own': msg.userId === currentUserId }">
            <div class="message-header">
              <span class="username">{{ msg.username }}</span>
              <span class="time">{{ formatTime(msg.createdAt) }}</span>
            </div>
            <div class="message-content">{{ msg.content }}</div>
          </div>
          <el-empty v-if="messages.length === 0" description="暂无消息，开始聊天吧" />
        </div>

        <div class="message-input">
          <el-input
            v-model="newMessage"
            type="textarea"
            :rows="2"
            placeholder="请输入消息内容"
            @keydown.enter.ctrl="sendMessage"
          />
          <el-button type="primary" @click="sendMessage" :loading="sending">发送</el-button>
        </div>
      </div>
    </div>

    <div class="purchase-float" v-if="item && !isOwner">
      <el-button type="success" size="large" @click="showOrderDialog = true">
        <el-icon><ShoppingCart /></el-icon>
        立即购买
      </el-button>
    </div>

    <el-dialog v-model="showOrderDialog" title="确认订单" width="500px">
      <div class="order-summary">
        <p><strong>商品:</strong> {{ item?.title }}</p>
        <p><strong>价格:</strong> ¥{{ item?.price }}</p>
      </div>
      <el-form :model="orderForm" label-width="100px">
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
        <el-button @click="showOrderDialog = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { itemAPI, orderAPI, userAPI, messageAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const item = ref(null)
const images = ref([])
const sellerInfo = ref(null)
const messages = ref([])
const newMessage = ref('')
const sending = ref(false)
const messagesListRef = ref(null)

const showOrderDialog = ref(false)
const orderForm = ref({
  transactionType: 0,
  deliveryAddress: ''
})

const currentUserId = computed(() => userStore.userId)
const isOwner = computed(() => item.value?.sellerId === userStore.userId)

let timer = null

const loadItem = async () => {
  try {
    const res = await itemAPI.getItem(route.params.itemId)
    if (res.code === 200) {
      item.value = res.data.item
      images.value = res.data.images || []
      
      if (item.value?.sellerId) {
        loadSellerInfo(item.value.sellerId)
      }
    }
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const loadSellerInfo = async (sellerId) => {
  try {
    const res = await userAPI.getUser(sellerId)
    if (res.code === 200) {
      sellerInfo.value = res.data
    }
  } catch (error) {
    console.error('加载卖家信息失败', error)
  }
}

const loadMessages = async () => {
  try {
    const res = await messageAPI.getMessagesByItem(route.params.itemId)
    if (res.code === 200) {
      messages.value = res.data
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载留言失败', error)
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  
  if (!item.value || !item.value.id) {
    ElMessage.warning('商品信息加载中，请稍后重试')
    return
  }
  
  sending.value = true
  try {
    console.log('Sending message:', { itemId: item.value.id, content: newMessage.value })
    const res = await messageAPI.createMessage({
      itemId: item.value.id,
      content: newMessage.value
    })
    console.log('Response:', res)
    if (res.code === 200) {
      newMessage.value = ''
      loadMessages()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    console.error('Send message error:', error)
    ElMessage.error(error.message || '发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesListRef.value) {
    messagesListRef.value.scrollTop = messagesListRef.value.scrollHeight
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const submitOrder = async () => {
  try {
    const res = await orderAPI.createOrder({
      itemId: item.value.id,
      transactionType: orderForm.value.transactionType,
      deliveryAddress: orderForm.value.deliveryAddress
    })
    if (res.code === 200) {
      ElMessage.success('下单成功')
      showOrderDialog.value = false
      router.push('/orders')
    }
  } catch (error) {
    ElMessage.error(error.message || '下单失败')
  }
}

onMounted(() => {
  if (route.params.itemId) {
    loadItem()
    loadMessages()
    timer = setInterval(() => {
      if (item.value?.id) {
        loadMessages()
      }
    }, 3000)
  } else {
    ElMessage.error('商品不存在')
    router.push('/home')
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.chat-container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background: #fff;
  padding: 15px 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chat-title {
  display: flex;
  flex-direction: column;
}

.chat-title .item-title {
  font-size: 12px;
  color: #909399;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  gap: 20px;
}

.item-info-card {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  gap: 15px;
}

.item-info-card .item-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}

.item-info-card .item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info-card .item-details h3 {
  margin-bottom: 10px;
}

.item-info-card .price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.item-info-card .description {
  color: #666;
  font-size: 12px;
}

.messages-area {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  max-height: 400px;
}

.message-item {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 8px;
  background: #f5f5f5;
}

.message-item.own {
  background: #e6f7ff;
  margin-left: 20%;
}

.message-item .message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.message-item .username {
  font-weight: bold;
  color: #409eff;
}

.message-item.own .username {
  color: #52c41a;
}

.message-item .time {
  color: #909399;
  font-size: 12px;
}

.message-item .message-content {
  line-height: 1.6;
}

.message-input {
  padding: 15px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
}

.message-input .el-textarea {
  flex: 1;
}

.purchase-float {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 100;
}

.purchase-float .el-button {
  padding: 20px 30px;
  font-size: 16px;
}

.order-summary {
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
  margin-bottom: 20px;
}

.order-summary p {
  margin: 8px 0;
}
</style>