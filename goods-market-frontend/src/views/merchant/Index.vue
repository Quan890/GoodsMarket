<template>
  <div class="merchant-home" v-loading="loading">
    <!-- 欢迎区 -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>欢迎回来，{{ userStore.nickname || '商家' }}</h2>
        <p>这里是您的商家中心，可以管理商品和查看订单</p>
      </div>
      <el-button v-if="!merchantInfo" type="primary" @click="router.push({ name: 'MerchantApply' })">
        申请入驻
      </el-button>
    </div>

    <!-- 商家信息卡片 -->
    <el-card v-if="merchantInfo" class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>店铺信息</span>
          <el-tag :type="auditTagType" size="small">{{ auditStatusText }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="店铺名称">{{ merchantInfo.shopName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ merchantInfo.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="店铺描述" :span="2">{{ merchantInfo.description || '暂无描述' }}</el-descriptions-item>
        <el-descriptions-item v-if="merchantInfo.licenseNo" label="营业执照号">{{ merchantInfo.licenseNo }}</el-descriptions-item>
        <el-descriptions-item label="入驻时间">{{ merchantInfo.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="merchantInfo.auditRemark" label="审核备注" :span="2">
          <span :style="{ color: merchantInfo.auditStatus === 2 ? '#f56c6c' : '#606266' }">
            {{ merchantInfo.auditRemark }}
          </span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 快捷操作 -->
    <div class="action-grid">
      <el-card class="action-card" shadow="hover" @click="router.push({ name: 'MerchantProduct' })">
        <el-icon :size="36" color="#409eff"><Goods /></el-icon>
        <h3>商品管理</h3>
        <p>管理您的商品，上下架、编辑商品信息</p>
      </el-card>
      <el-card class="action-card" shadow="hover" @click="router.push({ name: 'MerchantOrder' })">
        <el-icon :size="36" color="#67c23a"><List /></el-icon>
        <h3>店铺订单</h3>
        <p>查看与您商品相关的订单</p>
      </el-card>
      <el-card v-if="!merchantInfo || merchantInfo.auditStatus !== 1" class="action-card" shadow="hover" @click="router.push({ name: 'MerchantApply' })">
        <el-icon :size="36" color="#e6a23c"><Promotion /></el-icon>
        <h3>入驻申请</h3>
        <p>提交或查看商家入驻审核状态</p>
      </el-card>
    </div>

    <!-- 审核被驳回提示 -->
    <el-alert
      v-if="merchantInfo && merchantInfo.auditStatus === 2"
      title="入驻申请被驳回"
      :description="merchantInfo.auditRemark || '请联系管理员了解详情'"
      type="error"
      show-icon
      :closable="false"
      style="margin-top: 16px"
    >
      <template #default>
        <span>驳回原因：{{ merchantInfo.auditRemark || '请联系管理员了解详情' }}</span>
        <el-button type="danger" link style="margin-left: 12px" @click="router.push({ name: 'MerchantApply' })">
          重新申请
        </el-button>
      </template>
    </el-alert>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Goods, List, Promotion } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { getMerchantStatus } from '@/api/merchant'

const router = useRouter()
const userStore = useUserStore()

const merchantInfo = ref(null)
const loading = ref(false)

const AUDIT_MAP = {
  0: { text: '待审核', tagType: 'warning' },
  1: { text: '审核通过', tagType: 'success' },
  2: { text: '已驳回', tagType: 'danger' },
}

const auditStatusText = computed(() => AUDIT_MAP[merchantInfo.value?.auditStatus]?.text ?? '未知')
const auditTagType = computed(() => AUDIT_MAP[merchantInfo.value?.auditStatus]?.tagType ?? 'info')

async function fetchMerchantStatus() {
  loading.value = true
  try {
    const res = await getMerchantStatus()
    merchantInfo.value = res.data || null
  } catch {
    merchantInfo.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => { fetchMerchantStatus() })
</script>

<style lang="scss" scoped>
.merchant-home {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 欢迎区 */
.welcome-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  background: linear-gradient(135deg, #409eff, #79bbff);
  border-radius: 8px;
  color: #fff;

  h2 { margin: 0 0 6px; font-size: 22px; }
  p { margin: 0; font-size: 14px; opacity: 0.9; }
}

/* 信息卡片 */
.info-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 快捷操作 */
.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.action-card {
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }

  h3 {
    margin: 12px 0 6px;
    font-size: 16px;
    color: #303133;
  }

  p {
    margin: 0;
    font-size: 13px;
    color: #909399;
  }
}
</style>
