<template>
  <div class="order-detail-page" v-loading="loading">
    <el-page-header @back="goBack" content="订单详情" class="page-header" />

    <template v-if="order">
      <!-- 订单状态条 -->
      <div class="status-bar" :class="statusClass">
        <el-icon :size="24"><component :is="statusIcon" /></el-icon>
        <div class="status-info">
          <span class="status-text">{{ statusText }}</span>
          <span class="status-desc">{{ statusDesc }}</span>
        </div>
      </div>

      <!-- 收货信息 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="card-title">收货信息</span>
        </template>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ order.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item v-if="order.remark" label="订单备注" :span="2">{{ order.remark }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 商品明细 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="card-title">商品明细</span>
        </template>
        <div class="item-list">
          <div v-for="item in order.items" :key="item.productId" class="item-row">
            <el-image :src="item.productImage" fit="cover" class="item-image">
              <template #error>
                <div class="image-placeholder"><el-icon size="20"><Picture /></el-icon></div>
              </template>
            </el-image>
            <div class="item-info">
              <span class="item-name">{{ item.productName }}</span>
              <span class="item-price">￥{{ item.unitPrice }} × {{ item.quantity }}</span>
            </div>
            <span class="item-total">￥{{ item.totalPrice }}</span>
          </div>
        </div>
      </el-card>

      <!-- 订单信息 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="card-title">订单信息</span>
        </template>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">
            <span class="order-no">{{ order.orderNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item v-if="order.shopName" label="店铺">{{ order.shopName }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusTagType" size="small">{{ statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="order.payTime" label="支付时间">{{ order.payTime }}</el-descriptions-item>
          <el-descriptions-item v-if="order.payMethodDesc" label="支付方式">{{ order.payMethodDesc }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 金额汇总 + 操作 -->
      <div class="bottom-bar">
        <div class="amount-summary">
          <span class="label">实付金额：</span>
          <span class="amount">￥{{ order.payAmount }}</span>
        </div>
        <div class="actions">
          <template v-if="order.status === 0">
            <el-button type="danger" size="large" @click="goPay">立即支付</el-button>
            <el-button size="large" :loading="cancelLoading" @click="handleCancel">取消订单</el-button>
          </template>
          <el-button v-if="order.status === 4" type="success" size="large" :loading="receiptLoading" @click="handleReceipt">确认收货</el-button>
          <el-button v-if="order.status === 3" type="primary" size="large" @click="goHome">再次购买</el-button>
          <el-button size="large" @click="goBack">返回列表</el-button>
        </div>
      </div>
    </template>

    <el-empty v-if="!loading && !order" description="订单不存在">
      <el-button type="primary" @click="goHome">去首页</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, CircleCheckFilled, Clock, WarningFilled, Van } from '@element-plus/icons-vue'
import { getOrderDetail, cancelOrder, confirmReceipt } from '@/api/order'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(false)
const cancelLoading = ref(false)
const receiptLoading = ref(false)

// 状态映射（0待支付 1已支付/待发货 2已取消 3已完成 4已发货/待收货）
const STATUS_MAP = {
  0: { text: '待付款', desc: '请尽快完成支付，超时将自动取消', tagType: 'warning', icon: Clock, cls: 'status-unpaid' },
  1: { text: '待发货', desc: '订单已支付成功，等待商家发货', tagType: 'primary', icon: CircleCheckFilled, cls: 'status-paid' },
  2: { text: '已取消', desc: '订单已取消', tagType: 'info', icon: WarningFilled, cls: 'status-cancelled' },
  3: { text: '已完成', desc: '交易完成，感谢您的购买', tagType: 'success', icon: CircleCheckFilled, cls: 'status-completed' },
  4: { text: '待收货', desc: '商家已发货，请注意查收', tagType: 'success', icon: Van, cls: 'status-shipped' },
}

const statusText = computed(() => STATUS_MAP[order.value?.status]?.text ?? '未知')
const statusDesc = computed(() => STATUS_MAP[order.value?.status]?.desc ?? '')
const statusTagType = computed(() => STATUS_MAP[order.value?.status]?.tagType ?? 'info')
const statusIcon = computed(() => STATUS_MAP[order.value?.status]?.icon ?? Clock)
const statusClass = computed(() => STATUS_MAP[order.value?.status]?.cls ?? '')

/** 获取订单详情 */
async function fetchDetail() {
  const orderNo = route.params.orderNo
  if (!orderNo) return
  loading.value = true
  try {
    const res = await getOrderDetail(orderNo)
    order.value = res.data || null
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

/** 去收银台支付 */
function goPay() {
  router.push({ name: 'Payment', params: { orderNo: order.value.orderNo } })
}

/** 确认收货 */
async function handleReceipt() {
  try {
    await ElMessageBox.confirm('确认已收到商品？确认后订单将变为已完成', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info',
    })
  } catch { return }
  receiptLoading.value = true
  try {
    await confirmReceipt(order.value.orderNo)
    ElMessage.success('已确认收货')
    fetchDetail()
  } catch {} finally {
    receiptLoading.value = false
  }
}

/** 取消订单 */
async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定取消该订单？取消后无法恢复', '取消订单', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning',
    })
  } catch { return }
  cancelLoading.value = true
  try {
    await cancelOrder(order.value.orderNo)
    ElMessage.success('订单已取消')
    fetchDetail()
  } catch {} finally {
    cancelLoading.value = false
  }
}

function goBack() { router.push({ name: 'UserOrder' }) }
function goHome() { router.push({ name: 'Home' }) }

onMounted(() => { fetchDetail() })
</script>

<style lang="scss" scoped>
.order-detail-page {
  max-width: 800px;
  padding: 20px 0;
}

.page-header {
  margin-bottom: 20px;
}

/* 状态条 */
.status-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-radius: 8px;
  margin-bottom: 16px;
  color: #fff;

  &.status-unpaid { background: linear-gradient(135deg, #e6a23c, #f0c78a); }
  &.status-paid { background: linear-gradient(135deg, #409eff, #79bbff); }
  &.status-shipped { background: linear-gradient(135deg, #67c23a, #95d475); }
  &.status-completed { background: linear-gradient(135deg, #67c23a, #95d475); }
  &.status-cancelled { background: linear-gradient(135deg, #909399, #b1b3b8); }
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .status-text { font-size: 18px; font-weight: 600; }
  .status-desc { font-size: 13px; opacity: 0.9; }
}

/* 卡片 */
.section-card {
  margin-bottom: 16px;
  border-radius: 8px;

  .card-title {
    font-weight: 600;
    color: #303133;
  }
}

.order-no {
  font-family: monospace;
  color: #606266;
}

/* 商品明细 */
.item-list {
  display: flex;
  flex-direction: column;
}

.item-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  gap: 14px;

  & + .item-row {
    border-top: 1px solid #f5f5f5;
  }
}

.item-image {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  border-radius: 4px;
  overflow: hidden;
  background: #f5f7fa;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #c0c4cc;
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;

  .item-name { font-size: 14px; color: #303133; }
  .item-price { font-size: 13px; color: #909399; }
}

.item-total {
  flex-shrink: 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

/* 底部操作栏 */
.bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.amount-summary {
  .label { font-size: 14px; color: #606266; }
  .amount { font-size: 24px; font-weight: 700; color: #f56c6c; }
}

.actions {
  display: flex;
  gap: 12px;
}
</style>
