<template>
  <div class="collect-page">
    <h2 class="page-title">我的收藏</h2>
    <div v-loading="loading" style="min-height: 200px">
      <el-empty v-if="!loading && collectList.length === 0" description="还没有收藏商品">
        <el-button type="primary" @click="router.push({ name: 'Home' })">去逛逛</el-button>
      </el-empty>
      <div v-else class="collect-grid">
        <div v-for="item in collectList" :key="item.id" class="collect-card">
          <div class="card-image" @click="goDetail(item.productId)">
            <el-image :src="item.productImage" fit="cover" loading="lazy">
              <template #error>
                <div class="image-placeholder"><el-icon size="32"><Picture /></el-icon></div>
              </template>
            </el-image>
          </div>
          <div class="card-body">
            <h3 class="product-name" @click="goDetail(item.productId)">{{ item.productName }}</h3>
            <p class="product-subtitle">{{ item.subtitle }}</p>
            <div class="product-price">
              <span class="price-symbol">￥</span>
              <span class="price-value">{{ item.price }}</span>
              <span v-if="item.originalPrice > item.price" class="original-price">￥{{ item.originalPrice }}</span>
            </div>
            <div class="card-actions">
              <el-button type="danger" size="small" plain :loading="cancelLoadingMap[item.productId]" @click="handleCancel(item)">取消收藏</el-button>
              <el-button type="primary" size="small" @click="goDetail(item.productId)">查看商品</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next, jumper, ->, total" background @current-change="fetchCollects" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getCollectList, cancelCollect } from '@/api/collect'

const router = useRouter()
const collectList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const cancelLoadingMap = reactive({})

async function fetchCollects() {
  loading.value = true
  try {
    const res = await getCollectList({ pageNum: pageNum.value, pageSize: pageSize.value })
    const pageData = res.data || {}
    collectList.value = pageData.records || []
    total.value = pageData.total || 0
  } catch { collectList.value = [] } finally { loading.value = false }
}

async function handleCancel(item) {
  try {
    await ElMessageBox.confirm(`确定取消收藏「${item.productName}」？`, '取消收藏', { confirmButtonText: '确定', cancelButtonText: '再想想', type: 'warning' })
  } catch { return }
  cancelLoadingMap[item.productId] = true
  try {
    await cancelCollect(item.productId)
    ElMessage.success('已取消收藏')
    if (collectList.value.length === 1 && pageNum.value > 1) pageNum.value--
    fetchCollects()
  } catch {} finally { cancelLoadingMap[item.productId] = false }
}

function goDetail(productId) { router.push({ name: 'ProductDetail', params: { id: productId } }) }

onMounted(() => { fetchCollects() })
</script>

<style lang="scss" scoped>
.collect-page { padding: 20px 0; }
.page-title { margin-bottom: 20px; font-size: 20px; font-weight: 600; color: #303133; }
.collect-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; }
.collect-card { background: #fff; border-radius: 8px; overflow: hidden; transition: box-shadow 0.2s; &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); } }
.card-image { width: 100%; height: 200px; overflow: hidden; cursor: pointer; background: #fafafa; .el-image { width: 100%; height: 100%; } .image-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; } }
.card-body { padding: 14px; }
.product-name { font-size: 14px; font-weight: 500; color: #303133; cursor: pointer; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; &:hover { color: #409eff; } }
.product-subtitle { font-size: 12px; color: #909399; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price { margin-top: 8px; color: #f56c6c; .price-symbol { font-size: 12px; } .price-value { font-size: 18px; font-weight: 700; } }
.original-price { font-size: 12px; color: #c0c4cc; text-decoration: line-through; margin-left: 6px; }
.card-actions { display: flex; gap: 8px; margin-top: 12px; }
.pagination-wrap { display: flex; justify-content: center; padding: 30px 0; }
</style>
