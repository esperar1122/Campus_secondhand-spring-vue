<template>
  <div class="profile-container">
    <div class="header">
      <el-button @click="$router.push('/home')">返回首页</el-button>
    </div>
    <el-card>
      <h2>个人中心</h2>
      <div class="profile-info">
        <div class="info-item">
          <span class="label">用户名:</span>
          <span class="value">{{ userStore.username }}</span>
        </div>
        <div class="info-item">
          <span class="label">信用分:</span>
          <span class="value credit">{{ userStore.creditScore }} ({{ creditLevel }})</span>
        </div>
        <div class="info-item">
          <span class="label">我的评价:</span>
        </div>
        <div class="reviews-list">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-header">
              <span>评分: {{ review.rating }}星</span>
              <span>{{ formatTime(review.createdAt) }}</span>
            </div>
            <p>{{ review.content || '默认好评' }}</p>
          </div>
          <el-empty v-if="reviews.length === 0" description="暂无评价" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { reviewAPI } from '@/api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const reviews = ref([])

const creditLevel = computed(() => {
  const score = userStore.creditScore || 80
  if (score >= 90) return '极好'
  if (score >= 80) return '优秀'
  if (score >= 70) return '良好'
  if (score >= 60) return '一般'
  return '糟糕'
})

const loadReviews = async () => {
  try {
    const res = await reviewAPI.getUserReviews(userStore.userId)
    if (res.code === 200) {
      reviews.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载评价失败')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.profile-info {
  padding: 20px;
}

.info-item {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
}

.label {
  font-weight: bold;
  margin-right: 10px;
}

.value.credit {
  color: #409eff;
  font-weight: bold;
}

.reviews-list {
  margin-top: 20px;
}

.review-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #666;
}
</style>