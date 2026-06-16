<template>
  <div class="payment-page">
    <div class="payment-card">
      <div v-if="loading" v-loading="true" style="min-height: 300px" />
      <template v-else-if="order">
        <div class="pay-status">
          <el-icon :size="48" color="#e6a23c"><Clock /></el-icon>
          <h2 class="status-text">等待支付</h2>
          <p class="status-tip">请在30分钟内完成支付，超时订单将自动取消</p>
        </div>
        <div class="order-info">
          <div class="info-row"><span class="info-label">订单号</span><span class="info-value order-no">{{ order.orderNo }} <el-button type="primary" link size="small" @click="copyOrderNo">复制</el-button></span></div>
          <div class="info-row"><span class="info-label">下单时间</span><span class="info-value">{{ order.createTime }}</span></div>
        </div>
        <div class="pay-amount">
          <span class="amount-label">应付金额</span>
          <span class="amount-symbol">￥</span>
          <span class="amount-value">{{ order.payAmount || order.totalAmount }}</span>
        </div>
        <div class="pay-methods">
          <div class="methods-title">选择支付方式</div>
          <div class="method-item" :class="{ active: payMethod === 'mock' }" @click="payMethod = 'mock'">
            <div class="method-icon mock-icon"><el-icon :size="24"><Monitor /></el-icon></div>
            <div class="method-info"><span class="method-name">模拟支付</span><span class="method-desc">开发调试用，直接标记为已支付</span></div>
            <el-radio :value="'mock'" v-model="payMethod" />
          </div>
          <div class="method-item" :class="{ active: payMethod === 'wx' }" @click="payMethod = 'wx'">
            <div class="method-icon wx-icon"><el-icon :size="24"><ChatDotRound /></el-icon></div>
            <div class="method-info"><span class="method-name">微信支付</span><span class="method-desc">使用微信扫码或唤起支付</span></div>
            <el-radio :value="'wx'" v-model="payMethod" />
          </div>
        </div>
        <div class="pay-actions">
          <el-button type="danger" size="large" :loading="payLoading" class="pay-btn" @click="handlePay">
            {{ payMethod === 'mock' ? '确认支付（模拟）' : '立即支付' }}
          </el-button>
        </div>
        <div class="pay-footer">
          <el-button type="primary" link @click="goOrderDetail">查看订单详情</el-button>
          <el-button type="info" link @click="goOrderList">返回我的订单</el-button>
        </div>
      </template>
      <el-empty v-else description="订单不存在">
        <el-button type="primary" @click="router.push({ name: 'UserOrder' })">查看我的订单</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Monitor, ChatDotRound } from '@element-plus/icons-vue'
import { getOrderDetail, mockPay } from '@/api/order'

const route = useRoute()
const router = useRouter()
const orderNo = route.params.orderNo
const order = ref(null)
const loading = ref(true)
const payMethod = ref('mock')
const payLoading = ref(false)

async function fetchOrder() {
  loading.value = true
  try {
    const res = await getOrderDetail(orderNo)
    order.value = res.data
    if (order.value && order.value.status !== 0) {
      ElMessage.info('该订单无需支付')
      router.replace({ name: 'OrderDetail', params: { orderNo } })
    }
  } catch { order.value = null } finally { loading.value = false }
}

async function handlePay() {
  if (payLoading.value) return
  if (payMethod.value === 'mock') {
    try {
      await ElMessageBox.confirm(`确认支付 ￥${order.value.payAmount || order.value.totalAmount} ？（模拟支付）`, '确认支付', { confirmButtonText: '确认', cancelButtonText: '取消' })
    } catch { return }
    payLoading.value = true
    try {
      // 后端需要 { orderNo, payMethod: 2 }
      await mockPay({ orderNo, payMethod: 2 })
      ElMessage.success('支付成功')
      goOrderList()
    } catch {} finally { payLoading.value = false }
  } else {
    ElMessage.info('微信支付功能开发中，请使用模拟支付')
  }
}

function copyOrderNo() {
  navigator.clipboard.writeText(orderNo).then(() => { ElMessage.success('订单号已复制') }).catch(() => { ElMessage.error('复制失败') })
}
function goOrderDetail() { router.push({ name: 'OrderDetail', params: { orderNo } }) }
function goOrderList() { router.replace({ name: 'UserOrder' }) }

onMounted(() => { fetchOrder() })
</script>

<style lang="scss" scoped>
.payment-page { display: flex; justify-content: center; min-height: calc(100vh - 60px); padding: 30px 16px; background: #f5f5f5; }
.payment-card { width: 100%; max-width: 560px; padding: 36px 32px; background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }
.pay-status { text-align: center; margin-bottom: 28px; }
.status-text { margin-top: 12px; font-size: 22px; font-weight: 700; color: #e6a23c; }
.status-tip { margin-top: 8px; font-size: 13px; color: #909399; }
.order-info { margin-bottom: 24px; padding: 16px 20px; background: #fafafa; border-radius: 8px; }
.info-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
.info-label { color: #909399; flex-shrink: 0; }
.info-value { color: #303133; text-align: right; }
.order-no { font-family: monospace; display: flex; align-items: center; gap: 8px; }
.pay-amount { text-align: center; margin-bottom: 28px; padding: 20px 0; background: #fff9f0; border-radius: 8px; }
.amount-label { display: block; font-size: 13px; color: #909399; margin-bottom: 8px; }
.amount-symbol { font-size: 18px; font-weight: 600; color: #f56c6c; }
.amount-value { font-size: 36px; font-weight: 700; color: #f56c6c; }
.pay-methods { margin-bottom: 24px; }
.methods-title { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 12px; }
.method-item { display: flex; align-items: center; gap: 14px; padding: 16px; margin-bottom: 8px; border: 1px solid #ebeef5; border-radius: 8px; cursor: pointer; transition: all 0.2s; &:hover { border-color: #c0c4cc; } &.active { border-color: #409eff; background: #f0f7ff; } }
.method-icon { flex-shrink: 0; width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; }
.mock-icon { background: #909399; }
.wx-icon { background: #07c160; }
.method-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.method-name { font-size: 14px; font-weight: 600; color: #303133; }
.method-desc { font-size: 12px; color: #909399; }
.pay-actions { margin-bottom: 20px; }
.pay-btn { width: 100%; height: 48px; font-size: 16px; letter-spacing: 2px; }
.pay-footer { display: flex; justify-content: center; gap: 20px; }
</style>
