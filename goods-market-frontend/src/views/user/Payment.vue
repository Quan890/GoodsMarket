<template>
  <div class="payment-page">
    <div class="payment-card">
      <div v-if="loading" v-loading="true" style="min-height: 300px" />
      <template v-else-if="orders.length > 0">
        <div class="pay-status">
          <el-icon :size="48" color="#e6a23c"><Clock /></el-icon>
          <h2 class="status-text">等待支付</h2>
          <p class="status-tip">请在30分钟内完成支付，超时订单将自动取消</p>
        </div>

        <!-- 单订单信息 -->
        <div v-if="orders.length === 1" class="order-info">
          <div class="info-row">
            <span class="info-label">订单号</span>
            <span class="info-value order-no">{{ orders[0].orderNo }}
              <el-button type="primary" link size="small" @click="copyOrderNo(orders[0].orderNo)">复制</el-button>
            </span>
          </div>
          <div class="info-row"><span class="info-label">下单时间</span><span class="info-value">{{ orders[0].createTime }}</span></div>
        </div>
        <!-- 多订单（跨商家拆单）信息 -->
        <div v-else class="order-info">
          <div class="info-row"><span class="info-label">子订单</span><span class="info-value">{{ orders.length }} 个（已按商家拆分）</span></div>
          <div v-for="o in orders" :key="o.orderNo" class="info-row">
            <span class="info-label">{{ o.shopName || '订单' }}</span>
            <span class="info-value">￥{{ o.payAmount || o.totalAmount }}</span>
          </div>
        </div>

        <div class="pay-amount">
          <span class="amount-label">应付金额</span>
          <span class="amount-symbol">￥</span>
          <span class="amount-value">{{ totalPayAmountYuan }}</span>
        </div>

        <div class="pay-methods">
          <div class="methods-title">选择支付方式</div>
          <div class="method-item" :class="{ active: payMethod === 'mock' }" @click="payMethod = 'mock'">
            <div class="method-icon mock-icon"><el-icon :size="24"><Monitor /></el-icon></div>
            <div class="method-info"><span class="method-name">模拟支付</span><span class="method-desc">演示环境快捷支付，直接标记为已支付</span></div>
            <el-radio :value="'mock'" v-model="payMethod" />
          </div>
          <div class="method-item" :class="{ active: payMethod === 'wx' }" @click="payMethod = 'wx'">
            <div class="method-icon wx-icon"><el-icon :size="24"><ChatDotRound /></el-icon></div>
            <div class="method-info"><span class="method-name">微信支付</span><span class="method-desc">需配置微信商户参数后可用</span></div>
            <el-radio :value="'wx'" v-model="payMethod" />
          </div>
        </div>

        <div class="pay-actions">
          <el-button type="danger" size="large" :loading="payLoading" class="pay-btn" @click="handlePay">
            {{ payMethod === 'mock' ? '确认支付（模拟）' : '立即支付' }}
          </el-button>
        </div>
        <div class="pay-footer">
          <el-button type="primary" link @click="goOrderList">返回我的订单</el-button>
        </div>
      </template>
      <el-empty v-else description="订单不存在">
        <el-button type="primary" @click="router.push({ name: 'UserOrder' })">查看我的订单</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Monitor, ChatDotRound } from '@element-plus/icons-vue'
import { getOrderDetail, mockPay, getPayResult } from '@/api/order'

const route = useRoute()
const router = useRouter()

/**
 * 支持一次支付多个子订单（跨商家拆单场景）
 * 路由参数：/user/payment/:orderNo，多个订单号用英文逗号拼接
 */
const rawOrderNo = computed(() => String(route.params.orderNo || ''))
const orderNos = ref([])
const orders = ref([])
const loading = ref(true)
const payMethod = ref('mock')
const payLoading = ref(false)
let pollTimer = null

const totalPayAmount = computed(() =>
  orders.value.reduce((sum, o) => sum + Number(o.payAmount || o.totalAmount || 0), 0)
)
const totalPayAmountYuan = computed(() => totalPayAmount.value.toFixed(2))

/** 拉取所有子订单详情 */
async function fetchOrders() {
  loading.value = true
  try {
    const results = await Promise.all(
      orderNos.value.map((no) => getOrderDetail(no).then((r) => r.data).catch(() => null))
    )
    orders.value = results.filter(Boolean)
    // 全部订单都已不是待支付 → 无需支付
    if (orders.value.length > 0 && orders.value.every((o) => o.status !== 0)) {
      ElMessage.info('订单无需支付')
      goOrderList()
    }
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

/** 模拟支付：逐个支付所有待支付子订单，然后轮询支付结果 */
async function handlePay() {
  if (payLoading.value) return
  if (payMethod.value === 'wx') {
    ElMessage.info('微信支付需在服务端配置商户参数后可用，演示环境请使用模拟支付')
    return
  }
  const amountText = `￥${totalPayAmountYuan.value}`
  try {
    await ElMessageBox.confirm(`确认支付 ${amountText}？（模拟支付）`, '确认支付', { confirmButtonText: '确认', cancelButtonText: '取消' })
  } catch { return }

  payLoading.value = true
  try {
    const pendingOrders = orders.value.filter((o) => o.status === 0)
    // 逐个支付（演示环境模拟支付，真实环境这里改为微信预下单）
    for (const order of pendingOrders) {
      await mockPay({ orderNo: order.orderNo, payMethod: 2 })
    }
    // 轮询支付结果（最多10次，每次500ms）
    const paid = await pollPayResult(pendingOrders.map((o) => o.orderNo))
    if (paid) {
      ElMessage.success('支付成功')
      goOrderList()
    } else {
      ElMessage.warning('支付结果确认中，请稍后在订单列表查看')
      goOrderList()
    }
  } catch {} finally { payLoading.value = false }
}

/**
 * 轮询支付结果
 * @param {string[]} nos 订单号列表
 * @returns {Promise<boolean>} 是否全部支付成功
 */
function pollPayResult(nos) {
  let attempts = 0
  return new Promise((resolve) => {
    pollTimer = setInterval(async () => {
      attempts += 1
      try {
        const results = await Promise.all(nos.map((no) => getPayResult(no)))
        if (results.every((r) => r.data?.paid)) {
          clearInterval(pollTimer)
          resolve(true)
          return
        }
      } catch { /* 轮询失败继续重试 */ }
      if (attempts >= 10) {
        clearInterval(pollTimer)
        resolve(false)
      }
    }, 500)
  })
}

function copyOrderNo(no) {
  navigator.clipboard?.writeText(no)
    .then(() => { ElMessage.success('订单号已复制') })
    .catch(() => { ElMessage.error('复制失败') })
}

function goOrderList() { router.replace({ name: 'UserOrder' }) }

onMounted(() => {
  orderNos.value = rawOrderNo.value.split(',').map((s) => s.trim()).filter(Boolean)
  if (orderNos.value.length === 0) {
    loading.value = false
    return
  }
  fetchOrders()
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style lang="scss" scoped>
.payment-page { display: flex; justify-content: center; min-height: calc(100vh - 60px); padding: 30px 16px; background: #f5f5f5; }
.payment-card { width: 100%; max-width: 560px; padding: 36px 32px; background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.06); }
.pay-status { text-align: center; margin-bottom: 28px; }
.status-text { margin-top: 12px; font-size: 22px; font-weight: 700; color: #e6a23c; }
.status-tip { margin-top: 8px; font-size: 13px; color: #909399; }
.order-info { margin-bottom: 24px; padding: 16px 20px; background: #fafafa; border-radius: 8px; }
.info-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; font-size: 14px; }
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
