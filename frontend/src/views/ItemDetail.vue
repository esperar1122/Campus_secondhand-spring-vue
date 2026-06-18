<template>
  <div class="item-detail-container">
    <div class="header">
      <el-button @click="$router.push('/home')">返回首页</el-button>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <div class="content" v-if="item">
      <div class="images-section">
        <div class="main-image">
          <img :src="currentImage || '/placeholder.png'" alt="" />
        </div>
        <div class="thumbnails">
          <div 
            v-for="(img, index) in images" 
            :key="index" 
            class="thumbnail"
            :class="{ active: currentImage === img.imageUrl }"
            @click="currentImage = img.imageUrl"
          >
            <img :src="img.imageUrl" alt="" />
          </div>
          <div v-if="images.length === 0" class="no-image">暂无图片</div>
        </div>
      </div>

      <div class="info-section">
        <h1>{{ item.title }}</h1>
        <p class="price">¥{{ item.price }}</p>
        
        <div class="seller-info" v-if="sellerInfo">
          <span>卖家: {{ sellerInfo.username }}</span>
          <span class="credit">信用分: {{ sellerInfo.creditScore }} ({{ creditLevel }})</span>
        </div>

        <div class="description">
          <h3>商品描述</h3>
          <p>{{ item.description }}</p>
        </div>

        <div class="actions" v-if="isLoggedIn && !isOwner">
          <el-button type="primary" size="large" @click="handleBuy">我要购买</el-button>
        </div>
      </div>
    </div>

    <div class="message-section" v-if="item">
      <el-card>
        <template #header>
          <div class="message-header">
            <span>留言板</span>
            <el-button v-if="isLoggedIn" type="primary" size="small" @click="showMessageInput = true">
              留言
            </el-button>
          </div>
        </template>

        <div v-if="showMessageInput" class="message-input">
          <el-input
            v-model="newMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入留言内容（最多500字）"
            maxlength="500"
            show-word-limit
          />
          <div class="message-actions">
            <el-button @click="showMessageInput = false; newMessage = ''">取消</el-button>
            <el-button type="primary" @click="submitMessage" :loading="submitting">提交</el-button>
          </div>
        </div>

        <div class="messages-list">
          <div v-for="msg in messages" :key="msg.id" class="message-item">
            <div class="message-header">
              <span class="username">{{ msg.username }}</span>
              <span class="time">{{ formatTime(msg.createdAt) }}</span>
            </div>
            <div class="message-content">{{ msg.content }}</div>
          </div>
          <el-empty v-if="messages.length === 0" description="暂无留言" />
        </div>
      </el-card>
    </div>

    <el-dialog v-model="orderDialogVisible" title="确认订单" width="500px">
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
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { itemAPI, orderAPI, userAPI, messageAPI } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const item = ref(null)
const images = ref([])
const currentImage = ref('')
const sellerInfo = ref(null)
const orderDialogVisible = ref(false)
const orderForm = ref({
  transactionType: 0,
  deliveryAddress: ''
})

const messages = ref([])
const showMessageInput = ref(false)
const newMessage = ref('')
const submitting = ref(false)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isOwner = computed(() => item.value?.sellerId === userStore.userId)

const creditLevel = computed(() => {
  if (!sellerInfo.value) return ''
  const score = sellerInfo.value.creditScore || 80
  if (score >= 90) return '极好'
  if (score >= 80) return '优秀'
  if (score >= 70) return '良好'
  if (score >= 60) return '一般'
  return '糟糕'
})

const loadItem = async () => {
  try {
    const res = await itemAPI.getItem(route.params.id)
    if (res.code === 200) {
      item.value = res.data.item
      images.value = res.data.images || []
      currentImage.value = images.value[0]?.imageUrl || ''
      
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
    const res = await messageAPI.getMessagesByItem(route.params.id)
    if (res.code === 200) {
      messages.value = res.data
    }
  } catch (error) {
    console.error('加载留言失败', error)
  }
}

const submitMessage = async () => {
  if (!newMessage.value.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }
  
  submitting.value = true
  try {
    const res = await messageAPI.createMessage({
      itemId: item.value.id,
      content: newMessage.value
    })
    if (res.code === 200) {
      ElMessage.success('留言成功')
      showMessageInput.value = false
      newMessage.value = ''
      loadMessages()
    }
  } catch (error) {
    ElMessage.error(error.message || '留言失败')
  } finally {
    submitting.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const handleBuy = () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  orderDialogVisible.value = true
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
      orderDialogVisible.value = false
      router.push('/orders')
    }
  } catch (error) {
    ElMessage.error(error.message || '下单失败')
  }
}

onMounted(() => {
  loadItem()
  loadMessages()
})
</script>

<style scoped>
.item-detail-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40px;
}

.header {
  background: #fff;
  padding: 15px 40px;
}

.content {
  display: flex;
  gap: 30px;
  padding: 30px 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.images-section {
  flex: 0 0 400px;
}

.main-image {
  width: 400px;
  height: 400px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 15px;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnails {
  display: flex;
  gap: 10px;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.thumbnail.active {
  border-color: #409eff;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  color: #909399;
  font-size: 14px;
  padding: 20px;
  text-align: center;
  background: #f5f5f5;
  border-radius: 4px;
  width: 100%;
}

.info-section {
  flex: 1;
  background: #fff;
  padding: 30px;
  border-radius: 8px;
}

.info-section h1 {
  font-size: 24px;
  margin-bottom: 20px;
}

.price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 20px;
}

.seller-info {
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}

.credit {
  color: #409eff;
}

.description h3 {
  font-size: 16px;
  margin-bottom: 10px;
}

.description p {
  line-height: 1.8;
  color: #666;
}

.actions {
  margin-top: 30px;
}

.message-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-input {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
}

.message-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.messages-list {
  max-height: 400px;
  overflow-y: auto;
}

.message-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.message-item:last-child {
  border-bottom: none;
}

.message-item .message-header {
  margin-bottom: 8px;
}

.message-item .username {
  font-weight: bold;
  color: #409eff;
}

.message-item .time {
  color: #909399;
  font-size: 12px;
}

.message-item .message-content {
  color: #333;
  line-height: 1.6;
}
</style>