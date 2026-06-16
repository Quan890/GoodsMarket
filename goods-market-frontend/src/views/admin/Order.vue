<template>
  <div class="order-page">
    <h2 class="page-title">订单管理</h2>
    <div class="filter-bar">
      <el-input v-model="filters.orderNo" placeholder="订单号搜索" clearable prefix-icon="Search" style="width: 220px" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-input v-model="filters.merchantId" placeholder="商家ID" clearable style="width: 120px" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-select v-model="filters.status" placeholder="订单状态" clearable style="width: 140px" @change="handleSearch">
        <el-option label="待付款" :value="0" /><el-option label="已付款" :value="1" /><el-option label="已取消" :value="2" /><el-option label="已完成" :value="3" />
      </el-select>
      <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
    </div>
    <el-table v-loading="loading" :data="orderList" stripe>
      <el-table-column label="订单号" min-width="200"><template #default="{ row }"><span class="order-no">{{ row.orderNo }}</span></template></el-table-column>
      <el-table-column label="商品" min-width="220">
        <template #default="{ row }">
          <div v-for="item in row.items" :key="item.productId" class="goods-cell">
            <el-image :src="item.productImage" fit="cover" class="goods-thumb">
              <template #error><div class="thumb-placeholder"><el-icon size="14"><Picture /></el-icon></div></template>
            </el-image>
            <span class="goods-name">{{ item.productName }} x{{ item.quantity }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="110" align="center"><template #default="{ row }"><span class="price-text">￥{{ row.totalAmount }}</span></template></el-table-column>
      <el-table-column prop="merchantId" label="商家ID" width="90" align="center" />
      <el-table-column label="状态" width="100" align="center"><template #default="{ row }"><el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="170" />
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 3 && row.status !== 2" type="warning" link size="small" @click="openHandleDialog(row)">处理</el-button>
          <span v-else class="no-action">-</span>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next, jumper, ->, total" background @current-change="fetchOrders" />
    </div>

    <el-dialog v-model="handleDialogVisible" title="处理异常订单" width="440px">
      <p style="margin-bottom: 12px; color: #606266">订单号：<strong>{{ handlingOrder?.orderNo }}</strong></p>
      <p style="margin-bottom: 16px; color: #606266">当前状态：<el-tag :type="statusTagType(handlingOrder?.status)" size="small">{{ statusText(handlingOrder?.status) }}</el-tag></p>
      <el-form label-width="80px">
        <el-form-item label="变更至">
          <el-select v-model="targetStatus" style="width: 100%">
            <el-option v-for="opt in availableTargets" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="handleSubmitting" @click="handleOrderSubmit">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Search } from '@element-plus/icons-vue'
import { getAllOrders, handleAbnormalOrder } from '@/api/admin'

// 后端：0待支付 1已支付 2已取消 3已完成
const STATUS_OPTIONS = [
  { value: 2, label: '已取消' },
  { value: 3, label: '已完成' },
]

function statusText(s) { return { 0: '待付款', 1: '已付款', 2: '已取消', 3: '已完成' }[s] ?? '未知' }
function statusTagType(s) { return { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success' }[s] ?? 'info' }

const orderList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filters = reactive({ orderNo: '', merchantId: '', status: undefined })

async function fetchOrders() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filters.orderNo) params.orderNo = filters.orderNo
    if (filters.merchantId) params.merchantId = Number(filters.merchantId)
    if (filters.status !== undefined) params.status = filters.status
    const res = await getAllOrders(params)
    const page = res.data || {}
    orderList.value = page.records || []
    total.value = page.total || 0
  } catch { orderList.value = [] } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchOrders() }

const handleDialogVisible = ref(false)
const handlingOrder = ref(null)
const targetStatus = ref(3)
const handleSubmitting = ref(false)

const availableTargets = computed(() => {
  if (!handlingOrder.value) return STATUS_OPTIONS
  return STATUS_OPTIONS.filter((opt) => opt.value !== handlingOrder.value.status)
})

function openHandleDialog(row) {
  handlingOrder.value = row
  targetStatus.value = availableTargets.value[0]?.value ?? 3
  handleDialogVisible.value = true
}

async function handleOrderSubmit() {
  try { await ElMessageBox.confirm(`确定将订单 ${handlingOrder.value.orderNo} 状态变更为「${statusText(targetStatus.value)}」？`, '处理确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }) } catch { return }
  handleSubmitting.value = true
  try { await handleAbnormalOrder({ orderNo: handlingOrder.value.orderNo, targetStatus: targetStatus.value }); ElMessage.success('订单已处理'); handleDialogVisible.value = false; fetchOrders() }
  catch {} finally { handleSubmitting.value = false }
}

onMounted(() => fetchOrders())
</script>

<style lang="scss" scoped>
.order-page { padding: 0; }
.page-title { margin-bottom: 20px; font-size: 20px; font-weight: 600; color: #303133; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination-wrap { display: flex; justify-content: center; padding: 24px 0; }
.order-no { font-family: monospace; font-size: 13px; color: #606266; }
.goods-cell { display: flex; align-items: center; gap: 8px; padding: 3px 0; }
.goods-thumb { flex-shrink: 0; width: 40px; height: 40px; border-radius: 4px; overflow: hidden; background: #f5f7fa; }
.thumb-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; }
.goods-name { font-size: 13px; color: #303133; }
.price-text { font-weight: 600; color: #f56c6c; }
.no-action { color: #c0c4cc; }
</style>
