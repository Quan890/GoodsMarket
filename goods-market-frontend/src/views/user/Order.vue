<template>
  <div class="order-page">
    <h2 class="page-title">我的订单</h2>
    <el-tabs v-model="activeStatus" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待付款" name="0" />
      <el-tab-pane label="待发货" name="1" />
      <el-tab-pane label="待收货" name="4" />
      <el-tab-pane label="已完成" name="3" />
      <el-tab-pane label="已取消" name="2" />
    </el-tabs>
    <div v-loading="loading" style="min-height: 200px">
      <el-empty v-if="!loading && orderList.length === 0" description="暂无订单">
        <el-button type="primary" @click="router.push({ name: 'Home' })">去购物</el-button>
      </el-empty>
      <div v-else class="order-list">
        <div v-for="order in orderList" :key="order.orderNo" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ order.createTime }}</span>
            <el-tag :type="statusTagType(order.status)" size="small">{{ statusText(order.status) }}</el-tag>
          </div>
          <div v-for="item in order.items" :key="item.productId" class="order-item" @click="goDetail(order.orderNo)">
            <el-image :src="item.productImage" fit="cover" class="item-image">
              <template #error>
                <div class="image-placeholder"><el-icon size="20"><Picture /></el-icon></div>
              </template>
            </el-image>
            <div class="item-info">
              <span class="item-name">{{ item.productName }}</span>
              <span class="item-spec">x{{ item.quantity }}</span>
            </div>
            <span class="item-price">￥{{ item.unitPrice }}</span>
          </div>
          <div class="order-footer">
            <span class="order-total">共 {{ totalQuantity(order) }} 件，合计：<strong>￥{{ order.totalAmount }}</strong></span>
            <div class="order-actions">
              <template v-if="order.status === 0">
                <el-button type="danger" size="small" @click="goPay(order.orderNo)">去支付</el-button>
                <el-button size="small" :loading="cancelLoadingMap[order.orderNo]" @click="handleCancel(order)">取消订单</el-button>
              </template>
              <el-button v-if="order.status === 4" type="success" size="small" :loading="receiptLoadingMap[order.orderNo]" @click="handleReceipt(order)">确认收货</el-button>
              <el-button v-if="[1, 2, 3, 4].includes(order.status)" size="small" @click="goDetail(order.orderNo)">查看详情</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next, jumper, ->, total" background @current-change="fetchOrders" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getOrderList, cancelOrder, confirmReceipt } from '@/api/order'

const router = useRouter()
const orderList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeStatus = ref('all')
const cancelLoadingMap = reactive({})
const receiptLoadingMap = reactive({})

// 后端状态：0待支付 1已支付(待发货) 2已取消 3已完成 4已发货(待收货)
const STATUS_TEXT = { 0: '待付款', 1: '待发货', 2: '已取消', 3: '已完成', 4: '待收货' }
const STATUS_TAG = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'success' }
function statusText(s) { return STATUS_TEXT[s] ?? '未知' }
function statusTagType(s) { return STATUS_TAG[s] ?? 'info' }

function totalQuantity(order) {
  return (order.items || []).reduce((sum, item) => sum + (item.quantity || 0), 0)
}

async function fetchOrders() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (activeStatus.value !== 'all') params.status = Number(activeStatus.value)
    const res = await getOrderList(params)
    const pageData = res.data || {}
    orderList.value = pageData.records || []
    total.value = pageData.total || 0
  } catch { orderList.value = [] } finally { loading.value = false }
}

function handleTabChange() { pageNum.value = 1; fetchOrders() }

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm(`确定取消订单 ${order.orderNo}？取消后无法恢复`, '取消订单', { confirmButtonText: '确定取消', cancelButtonText: '再想想', type: 'warning' })
  } catch { return }
  cancelLoadingMap[order.orderNo] = true
  try { await cancelOrder(order.orderNo); ElMessage.success('订单已取消'); fetchOrders() }
  catch {} finally { cancelLoadingMap[order.orderNo] = false }
}

/** 确认收货（仅已发货订单） */
async function handleReceipt(order) {
  try {
    await ElMessageBox.confirm(`确认已收到订单 ${order.orderNo} 的商品？`, '确认收货', { confirmButtonText: '确认收货', cancelButtonText: '取消', type: 'info' })
  } catch { return }
  receiptLoadingMap[order.orderNo] = true
  try { await confirmReceipt(order.orderNo); ElMessage.success('已确认收货'); fetchOrders() }
  catch {} finally { receiptLoadingMap[order.orderNo] = false }
}

/** 去收银台支付 */
function goPay(orderNo) {
  router.push({ name: 'Payment', params: { orderNo } })
}

function goDetail(orderNo) { router.push({ name: 'OrderDetail', params: { orderNo } }) }

onMounted(() => { fetchOrders() })
</script>

<style lang="scss" scoped>
.order-page { padding: 20px 0; }
.page-title { margin-bottom: 16px; font-size: 20px; font-weight: 600; color: #303133; }
.order-card { margin-bottom: 16px; background: #fff; border-radius: 8px; overflow: hidden; border: 1px solid #ebeef5; &:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.05); } }
.order-header { display: flex; align-items: center; gap: 16px; padding: 14px 20px; background: #fafafa; border-bottom: 1px solid #ebeef5; font-size: 13px; }
.order-no { color: #606266; font-family: monospace; }
.order-time { color: #909399; margin-left: auto; }
.order-item { display: flex; align-items: center; padding: 14px 20px; gap: 14px; cursor: pointer; &:hover { background: #fafafa; } & + .order-item { border-top: 1px solid #f5f5f5; } }
.item-image { flex-shrink: 0; width: 64px; height: 64px; border-radius: 4px; overflow: hidden; background: #f5f7fa; }
.image-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; }
.item-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.item-name { font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-spec { font-size: 12px; color: #909399; }
.item-price { font-size: 14px; color: #606266; flex-shrink: 0; }
.order-footer { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; border-top: 1px solid #ebeef5; background: #fafafa; }
.order-total { font-size: 14px; color: #606266; strong { font-size: 18px; color: #f56c6c; } }
.order-actions { display: flex; gap: 8px; }
.pagination-wrap { display: flex; justify-content: center; padding: 30px 0; }
</style>
