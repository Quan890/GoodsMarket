<template>
  <div class="product-page">
    <h2 class="page-title">商品管控</h2>
    <div class="filter-bar">
      <el-input v-model="filters.name" placeholder="商品名称搜索" clearable prefix-icon="Search" style="width: 200px" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-input v-model="filters.merchantId" placeholder="商家ID" clearable style="width: 120px" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
        <el-option label="在售" :value="1" /><el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
    </div>
    <el-table v-loading="loading" :data="productList" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="商品" min-width="240">
        <template #default="{ row }">
          <div class="product-cell">
            <el-image :src="row.mainImage" fit="cover" class="product-thumb">
              <template #error><div class="thumb-placeholder"><el-icon size="18"><Picture /></el-icon></div></template>
            </el-image>
            <div class="product-text">
              <span class="product-name">{{ row.name }}</span>
              <span class="product-desc">{{ row.subtitle }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="110" align="center"><template #default="{ row }"><span class="price-text">￥{{ row.price }}</span></template></el-table-column>
      <el-table-column prop="stock" label="库存" width="80" align="center" />
      <el-table-column prop="merchantId" label="商家ID" width="90" align="center" />
      <el-table-column label="状态" width="90" align="center"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '在售' : '已下架' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" type="warning" link size="small" @click="handleOffShelf(row)">强制下架</el-button>
          <el-button v-if="row.status === 0" type="success" link size="small" @click="handleOnShelf(row)">重新上架</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">强制删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next, jumper, ->, total" background @current-change="fetchProducts" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Search } from '@element-plus/icons-vue'
import { getAllProducts, forceOffShelfProduct, forceOnShelfProduct, forceDeleteProduct } from '@/api/admin'

const productList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filters = reactive({ name: '', merchantId: '', status: undefined })

async function fetchProducts() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filters.name) params.name = filters.name
    if (filters.merchantId) params.merchantId = Number(filters.merchantId)
    if (filters.status !== undefined) params.status = filters.status
    const res = await getAllProducts(params)
    const page = res.data || {}
    productList.value = page.records || []
    total.value = page.total || 0
  } catch { productList.value = [] } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchProducts() }

async function handleOffShelf(row) {
  try { await ElMessageBox.confirm(`确定强制下架商品「${row.name}」？`, '强制下架', { confirmButtonText: '确定下架', cancelButtonText: '取消', type: 'warning' }) } catch { return }
  try { await forceOffShelfProduct(row.id); ElMessage.success('已下架'); fetchProducts() } catch {}
}

async function handleOnShelf(row) {
  try { await ElMessageBox.confirm(`确定重新上架商品「${row.name}」？`, '重新上架', { confirmButtonText: '确定上架', cancelButtonText: '取消', type: 'info' }) } catch { return }
  try { await forceOnShelfProduct(row.id); ElMessage.success('已上架'); fetchProducts() } catch {}
}

async function handleDelete(row) {
  try { await ElMessageBox.confirm(`确定强制删除商品「${row.name}」？此操作不可恢复！`, '强制删除', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }) } catch { return }
  try { await forceDeleteProduct(row.id); ElMessage.success('已删除'); if (productList.value.length === 1 && pageNum.value > 1) pageNum.value--; fetchProducts() } catch {}
}

onMounted(() => fetchProducts())
</script>

<style lang="scss" scoped>
.product-page { padding: 0; }
.page-title { margin-bottom: 20px; font-size: 20px; font-weight: 600; color: #303133; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination-wrap { display: flex; justify-content: center; padding: 24px 0; }
.product-cell { display: flex; align-items: center; gap: 10px; }
.product-thumb { flex-shrink: 0; width: 56px; height: 56px; border-radius: 4px; overflow: hidden; background: #f5f7fa; }
.thumb-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; }
.product-text { display: flex; flex-direction: column; gap: 3px; min-width: 0; }
.product-name { font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-desc { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price-text { font-weight: 600; color: #f56c6c; }
</style>
