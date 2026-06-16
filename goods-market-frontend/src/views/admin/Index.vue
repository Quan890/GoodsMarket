<template>
  <div class="dashboard-page">
    <h2 class="page-title">数据看板</h2>

    <div v-loading="loading">
      <!-- ==================== 统计卡片 ==================== -->
      <div class="stat-grid">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e8f4fd; color: #409eff">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">用户总数</span>
            <span class="stat-value">{{ stats.totalUsers ?? '-' }}</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: #fdf6ec; color: #e6a23c">
            <el-icon :size="28"><Shop /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">商家总数</span>
            <span class="stat-value">{{ stats.totalMerchants ?? '-' }}</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: #f0f9eb; color: #67c23a">
            <el-icon :size="28"><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">商品总数</span>
            <span class="stat-value">{{ stats.totalProducts ?? '-' }}</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: #fef0f0; color: #f56c6c">
            <el-icon :size="28"><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">订单总数</span>
            <span class="stat-value">{{ stats.totalOrders ?? '-' }}</span>
          </div>
        </div>

        <div class="stat-card wide">
          <div class="stat-icon" style="background: #f4f4f5; color: #909399">
            <el-icon :size="28"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">累计交易额</span>
            <span class="stat-value price">
              ￥{{ stats.totalAmount ?? '-' }}
            </span>
          </div>
        </div>

        <div class="stat-card wide">
          <div class="stat-icon" style="background: #fdf2f8; color: #e6a23c">
            <el-icon :size="28"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">今日交易额</span>
            <span class="stat-value price">
              ￥{{ stats.todayAmount ?? '-' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Shop, Goods, ShoppingCart, Wallet, TrendCharts } from '@element-plus/icons-vue'
import { getStatistics } from '@/api/admin'

const stats = ref({})
const loading = ref(false)

async function fetchStats() {
  loading.value = true
  try {
    const res = await getStatistics()
    stats.value = res.data || {}
  } catch {
    stats.value = {}
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  padding: 0;
}

.page-title {
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 20px;
  background: #fff;
  border-radius: 8px;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  }

  &.wide {
    grid-column: span 1;
  }
}

.stat-icon {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;

  &.price {
    color: #f56c6c;
  }
}
</style>
