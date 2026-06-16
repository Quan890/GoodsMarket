<template>
  <div class="merchant-page">
    <h2 class="page-title">商家审核</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="待审核" name="pending">
        <el-table v-loading="pendingLoading" :data="pendingList" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="shopName" label="店铺名称" min-width="150" />
          <el-table-column prop="contactPhone" label="联系电话" width="140" />
          <el-table-column prop="description" label="店铺描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="licenseNo" label="营业执照号" width="160"><template #default="{ row }">{{ row.licenseNo || '-' }}</template></el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="170" />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="success" link size="small" @click="handleAudit(row, 1)">通过</el-button>
              <el-button type="danger" link size="small" @click="openRejectDialog(row)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="pendingTotal > pageSize" class="pagination-wrap">
          <el-pagination v-model:current-page="pendingPage" :page-size="pageSize" :total="pendingTotal" layout="prev, pager, next, ->, total" background @current-change="fetchPending" />
        </div>
        <el-empty v-if="!pendingLoading && pendingList.length === 0" description="暂无待审核申请" />
      </el-tab-pane>

      <el-tab-pane label="全部商家" name="all">
        <div class="filter-bar">
          <el-input v-model="merchantFilters.shopName" placeholder="店铺名搜索" clearable prefix-icon="Search" style="width: 200px" @clear="handleMerchantSearch" @keyup.enter="handleMerchantSearch" />
          <el-select v-model="merchantFilters.auditStatus" placeholder="审核状态" clearable style="width: 140px" @change="handleMerchantSearch">
            <el-option label="待审核" :value="0" /><el-option label="已通过" :value="1" /><el-option label="已驳回" :value="2" />
          </el-select>
          <el-button type="primary" @click="handleMerchantSearch"><el-icon><Search /></el-icon>搜索</el-button>
        </div>
        <el-table v-loading="merchantLoading" :data="merchantList" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="shopName" label="店铺名称" min-width="150" />
          <el-table-column prop="contactPhone" label="联系电话" width="140" />
          <el-table-column label="审核状态" width="100" align="center">
            <template #default="{ row }"><el-tag :type="auditTagType(row.auditStatus)" size="small">{{ auditText(row.auditStatus) }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="auditRemark" label="驳回原因" min-width="160" show-overflow-tooltip><template #default="{ row }">{{ row.auditRemark || '-' }}</template></el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="170" />
        </el-table>
        <div v-if="merchantTotal > pageSize" class="pagination-wrap">
          <el-pagination v-model:current-page="merchantPage" :page-size="pageSize" :total="merchantTotal" layout="prev, pager, next, ->, total" background @current-change="fetchMerchants" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回申请" width="440px">
      <p style="margin-bottom: 12px; color: #606266">店铺：<strong>{{ rejectingMerchant?.shopName }}</strong></p>
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因（必填）" maxlength="200" show-word-limit />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectSubmitting" @click="handleReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getPendingMerchants, auditMerchant, getAllMerchants } from '@/api/admin'

const activeTab = ref('pending')
const pageSize = ref(10)
function auditText(s) { return { 0: '待审核', 1: '已通过', 2: '已驳回' }[s] ?? '未知' }
function auditTagType(s) { return { 0: 'warning', 1: 'success', 2: 'danger' }[s] ?? 'info' }

const pendingList = ref([])
const pendingLoading = ref(false)
const pendingPage = ref(1)
const pendingTotal = ref(0)

async function fetchPending() {
  pendingLoading.value = true
  try {
    const res = await getPendingMerchants({ pageNum: pendingPage.value, pageSize: pageSize.value })
    const page = res.data || {}
    pendingList.value = page.records || []
    pendingTotal.value = page.total || 0
  } catch { pendingList.value = [] } finally { pendingLoading.value = false }
}

async function handleAudit(row, status) {
  try { await ElMessageBox.confirm(`确定通过「${row.shopName}」的入驻申请？`, '审核通过', { confirmButtonText: '确定通过', cancelButtonText: '取消', type: 'success' }) } catch { return }
  try { await auditMerchant({ merchantId: row.id, auditStatus: status, auditRemark: '审核通过' }); ElMessage.success('已通过'); fetchPending() } catch {}
}

const rejectDialogVisible = ref(false)
const rejectingMerchant = ref(null)
const rejectReason = ref('')
const rejectSubmitting = ref(false)

function openRejectDialog(row) { rejectingMerchant.value = row; rejectReason.value = ''; rejectDialogVisible.value = true }

async function handleReject() {
  if (!rejectReason.value.trim()) { ElMessage.warning('请输入驳回原因'); return }
  rejectSubmitting.value = true
  try {
    await auditMerchant({ merchantId: rejectingMerchant.value.id, auditStatus: 2, auditRemark: rejectReason.value.trim() })
    ElMessage.success('已驳回'); rejectDialogVisible.value = false; fetchPending()
  } catch {} finally { rejectSubmitting.value = false }
}

const merchantList = ref([])
const merchantLoading = ref(false)
const merchantPage = ref(1)
const merchantTotal = ref(0)
const merchantFilters = reactive({ shopName: '', auditStatus: undefined })

async function fetchMerchants() {
  merchantLoading.value = true
  try {
    const params = { pageNum: merchantPage.value, pageSize: pageSize.value }
    if (merchantFilters.shopName) params.shopName = merchantFilters.shopName
    if (merchantFilters.auditStatus !== undefined) params.auditStatus = merchantFilters.auditStatus
    const res = await getAllMerchants(params)
    const page = res.data || {}
    merchantList.value = page.records || []
    merchantTotal.value = page.total || 0
  } catch { merchantList.value = [] } finally { merchantLoading.value = false }
}

function handleMerchantSearch() { merchantPage.value = 1; fetchMerchants() }

watch(activeTab, (val) => { if (val === 'all' && merchantList.value.length === 0) fetchMerchants() })

onMounted(() => fetchPending())
</script>

<style lang="scss" scoped>
.merchant-page { padding: 0; }
.page-title { margin-bottom: 16px; font-size: 20px; font-weight: 600; color: #303133; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination-wrap { display: flex; justify-content: center; padding: 24px 0; }
</style>
