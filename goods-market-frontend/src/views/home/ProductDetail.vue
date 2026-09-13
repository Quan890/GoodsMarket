<template>
  <div class="detail-page">
    <div class="page-container">
      <div class="back-bar" @click="router.back()">
        <el-icon><ArrowLeftBold /></el-icon>
        <span>返回</span>
      </div>
      <div v-if="loading" class="skeleton-wrap"><el-skeleton :rows="6" animated /></div>
      <div v-else-if="product" class="detail-main">
        <div class="detail-image">
          <el-image :src="product.mainImage" fit="contain" class="main-image">
            <template #error>
              <div class="image-placeholder"><el-icon size="60"><Picture /></el-icon><span>暂无图片</span></div>
            </template>
          </el-image>
        </div>
        <div class="detail-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-desc">{{ product.subtitle }}</p>
          <div class="price-block">
            <span class="price-label">价格</span>
            <span class="price-symbol">￥</span>
            <span class="price-value">{{ product.price }}</span>
            <span v-if="product.originalPrice > product.price" class="original-price">￥{{ product.originalPrice }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">库存</span>
            <span :class="['info-value', { 'low-stock': product.stock < 10 }]">
              {{ product.stock > 0 ? `${product.stock} 件` : '已售罄' }}
            </span>
          </div>
          <div v-if="product.stock > 0" class="info-row">
            <span class="info-label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" size="default" />
          </div>
          <div class="action-bar">
            <el-button v-if="userStore.isLoggedIn" size="large" :type="collected ? 'warning' : 'default'" @click="handleCollect" :loading="collectLoading">
              <el-icon><StarFilled v-if="collected" /><Star v-else /></el-icon>
              {{ collected ? '已收藏' : '收藏' }}
            </el-button>
            <el-button v-if="userStore.isLoggedIn" type="primary" size="large" :disabled="product.stock <= 0" @click="handleAddCart" :loading="cartLoading">
              <el-icon><ShoppingCart /></el-icon>
              加入购物车
            </el-button>
            <el-button v-if="userStore.isLoggedIn" type="danger" size="large" :disabled="product.stock <= 0" @click="handleBuyNow">
              立即购买
            </el-button>
            <el-button v-if="!userStore.isLoggedIn" type="primary" size="large" @click="goLogin">
              登录后购买
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="商品不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, Picture, ShoppingCart, ArrowLeftBold } from '@element-plus/icons-vue'
import { useUserStore, useCartStore } from '@/stores'
import { getProductDetail } from '@/api/product'
import { addCollect, cancelCollect, checkCollected } from '@/api/collect'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const product = ref(null)
const loading = ref(true)
const quantity = ref(1)
const collected = ref(false)
const collectLoading = ref(false)
const cartLoading = ref(false)

async function fetchProduct() {
  loading.value = true
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
    if (userStore.isLoggedIn && product.value) {
      try {
        const r = await checkCollected(product.value.id)
        collected.value = r.data?.collected ?? false
      } catch { collected.value = false }
    }
  } catch { product.value = null } finally { loading.value = false }
}

async function handleCollect() {
  if (!product.value) return
  collectLoading.value = true
  try {
    if (collected.value) { await cancelCollect(product.value.id); collected.value = false; ElMessage.success('已取消收藏') }
    else { await addCollect(product.value.id); collected.value = true; ElMessage.success('收藏成功') }
  } catch {} finally { collectLoading.value = false }
}

async function handleAddCart() {
  if (!product.value) return
  cartLoading.value = true
  try { await cartStore.addItem({ productId: product.value.id, quantity: quantity.value }) }
  catch {} finally { cartLoading.value = false }
}

/** 立即购买：携带商品与数量直接进入结算页（不写入购物车） */
function handleBuyNow() {
  if (!product.value) return
  router.push({
    name: 'OrderConfirm',
    query: { productId: product.value.id, quantity: quantity.value },
  })
}

function goLogin() { router.push({ name: 'Login', query: { redirect: route.fullPath } }) }

onMounted(() => { fetchProduct() })
</script>

<style lang="scss" scoped>
.detail-page { min-height: calc(100vh - 60px); background: #f5f5f5; }
.back-bar {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #409eff;
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    color: #fff;
    background: #409eff;
    border-color: #409eff;
  }
}
.skeleton-wrap { max-width: 800px; margin: 40px auto; padding: 30px; background: #fff; border-radius: 8px; }
.detail-main { display: flex; gap: 40px; margin-top: 30px; padding: 30px; background: #fff; border-radius: 8px; }
.detail-image { flex-shrink: 0; width: 420px; height: 420px; border-radius: 8px; overflow: hidden; background: #fafafa; .main-image { width: 100%; height: 100%; } .image-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; gap: 10px; } }
.detail-info { flex: 1; min-width: 0; }
.product-name { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.4; }
.product-desc { margin-top: 10px; font-size: 14px; color: #909399; line-height: 1.6; }
.price-block { margin-top: 20px; padding: 16px 20px; background: #fff9f0; border-radius: 6px; .price-label { margin-right: 12px; font-size: 13px; color: #909399; } .price-symbol { font-size: 16px; color: #f56c6c; font-weight: 600; } .price-value { font-size: 28px; font-weight: 700; color: #f56c6c; } }
.original-price { font-size: 14px; color: #c0c4cc; text-decoration: line-through; margin-left: 10px; }
.info-row { display: flex; align-items: center; margin-top: 16px; font-size: 14px; .info-label { width: 50px; color: #909399; flex-shrink: 0; } .info-value { color: #303133; } .low-stock { color: #e6a23c; font-weight: 600; } }
.action-bar { display: flex; gap: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ebeef5; }
@media (max-width: 768px) { .detail-main { flex-direction: column; gap: 20px; padding: 16px; } .detail-image { width: 100%; height: 300px; } .action-bar { flex-wrap: wrap; } }
</style>
