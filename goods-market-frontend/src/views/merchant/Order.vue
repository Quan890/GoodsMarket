<template>
  <div class="merchant-order-page">
    <h2 class="page-title">店铺订单</h2>
    <el-tabs v-model="activeStatus" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待付款" name="0" />
      <el-tab-pane label="已付款" name="1" />
      <el-tab-pane label="已完成" name="3" />
      <el-tab-pane label="已取消" name="2" />
    </el-tabs>
    <el-table v-loading="loading" :data="orderList" stripe style="margin-top: 8px">
      <el-table-column label="订单号" min-width="200">
        <template #default="{ row }"><span class="order-no">{{ row.orderNo }}</span></template>
      </el-table-column>
      <el-table-column label="商品" min-width="240">
        <template #default="{ row }">
          <div v-for="item in row.items" :key="item.productId" class="goods-cell">
            <el-image :src="item.productImage" fit="cover" class="goods-thumb">
              <template #error><div class="thumb-placeholder"><el-icon size="16"><Picture /></el-icon></div></template>
            </el-image>
            <div class="goods-info">
              <span class="goods-name">{{ item.productName }}</span>
              <span class="goods-qty">x{{ item.quantity }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="订单金额" width="120" align="center">
        <template #default="{ row }"><span class="price-text">￥{{ row.totalAmount }}</span></template>
      </el-table-column>
      <el-table-column label="下单时间" width="170" align="center">
        <template #default="{ row }"><span class="time-text">{{ row.createTime }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="viewDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && orderList.length === 0" description="暂无订单" />
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next, jumper, ->, total" background @current-change="fetchOrders" />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <template v-if="detailOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号" :span="2">{{ detailOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detailOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="订单状态"><el-tag :type="statusTagType(detailOrder.status)" size="small">{{ statusText(detailOrder.status) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailOrder.receiverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailOrder.receiverPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detailOrder.receiverAddress || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 10px">商品明细</h4>
        <el-table :data="detailOrder.items" border size="small">
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div class="detail-goods-cell">
                <el-image :src="row.productImage" fit="cover" style="width: 40px; height: 40px; border-radius: 4px" />
                <span>{{ row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="100" align="center"><template #default="{ row }">￥{{ row.unitPrice }}</template></el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column label="小计" width="100" align="center"><template #default="{ row }"><strong>￥{{ row.totalPrice }}</strong></template></el-table-column>
        </el-table>
        <div class="detail-total">合计：<strong>￥{{ detailOrder.totalAmount }}</strong></div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getMyProductOrders } from '@/api/merchant'
import { getOrderDetail } from '@/api/order'

const orderList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeStatus = ref('all')

// 后端：0待支付 1已支付 2已取消 3已完成
function statusText(s) { return { 0: '待付款', 1: '已付款', 2: '已取消', 3: '已完成' }[s] ?? '未知' }
function statusTagType(s) { return { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success' }[s] ?? 'info' }

async function fetchOrders() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (activeStatus.value !== 'all') params.status = Number(activeStatus.value)
    const res = await getMyProductOrders(params)
    const pageData = res.data || {}
    orderList.value = pageData.records || []
    total.value = pageData.total || 0
  } catch { orderList.value = [] } finally { loading.value = false }
}

function handleTabChange() { pageNum.value = 1; fetchOrders() }

const detailVisible = ref(false)
const detailOrder = ref(null)
async function viewDetail(row) {
  try { const res = await getOrderDetail(row.orderNo); detailOrder.value = res.data; detailVisible.value = true }
  catch { ElMessage.error('获取订单详情失败') }
}

onMounted(() => { fetchOrders() })
</script>

<style lang="scss" scoped>
.merchant-order-page { padding: 20px 0; }
.page-title { margin-bottom: 16px; font-size: 20px; font-weight: 600; color: #303133; }
.order-no { font-family: monospace; font-size: 13px; color: #606266; }
.goods-cell { display: flex; align-items: center; gap: 10px; padding: 4px 0; & + .goods-cell { border-top: 1px solid #f5f5f5; } }
.goods-thumb { flex-shrink: 0; width: 48px; height: 48px; border-radius: 4px; overflow: hidden; background: #f5f7fa; }
.thumb-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; }
.goods-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.goods-name { font-size: 13px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.goods-qty { font-size: 12px; color: #909399; }
.price-text { font-weight: 600; color: #f56c6c; }
.time-text { font-size: 13px; color: #909399; }
.detail-goods-cell { display: flex; align-items: center; gap: 8px; }
.detail-total { margin-top: 16px; text-align: right; font-size: 16px; color: #303133; strong { font-size: 20px; color: #f56c6c; } }
.pagination-wrap { display: flex; justify-content: center; padding: 24px 0; }
</style>
