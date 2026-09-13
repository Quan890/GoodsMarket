<template>
  <div class="home-page">
    <div class="page-container">

      <!-- ==================== 分类 + 轮播图（同一水平线） ==================== -->
      <div class="hero-section">
        <!-- 分类区域（列表 + 抽屉共用一个父容器，解决鼠标移入抽屉时关闭的问题） -->
        <div
          class="category-wrapper"
          @mouseleave="hoverCatId = null"
        >
          <!-- 左侧分类列表 -->
          <div class="category-panel">
            <div class="category-title">全部分类</div>
            <ul class="category-list">
              <li
                v-for="cat in categoryTree"
                :key="cat.id"
                class="category-item"
                :class="{ active: hoverCatId === cat.id }"
                @mouseenter="hoverCatId = cat.id"
                @click="handleCategoryClick(cat.id)"
              >
                <img v-if="cat.icon" :src="cat.icon" class="cat-icon" />
                <span class="cat-name">{{ cat.name }}</span>
                <span class="cat-arrow">›</span>
              </li>
            </ul>
          </div>

          <!-- 抽屉式分类详情面板（悬浮一级分类时显示） -->
          <transition name="drawer-slide">
            <div
              v-if="hoverCatId && hoverCategory"
              class="category-drawer"
            >
              <!-- 抽屉头部 -->
              <div class="drawer-header">
                <img v-if="hoverCategory.icon" :src="hoverCategory.icon" class="drawer-icon" />
                <h3 class="drawer-title">{{ hoverCategory.name }}</h3>
                <el-button text type="primary" size="small" @click="handleCategoryClick(hoverCategory.id)">
                  查看全部 ›
                </el-button>
              </div>

              <!-- 二级分类网格 -->
              <div class="drawer-body" v-if="hoverCategory.children && hoverCategory.children.length">
                <div
                  v-for="sub in hoverCategory.children"
                  :key="sub.id"
                  class="drawer-group"
                >
                  <div class="group-title" @click.stop="handleCategoryClick(sub.id)">
                    {{ sub.name }}
                  </div>
                  <div class="group-items">
                    <span
                      v-for="p in getProductsByCategory(sub.id)"
                      :key="p.id"
                      class="group-item"
                      @click.stop="goDetail(p.id)"
                    >
                      <img :src="p.mainImage" class="item-thumb" />
                      <span class="item-name">{{ p.name }}</span>
                    </span>
                    <span class="group-item more" @click.stop="handleCategoryClick(sub.id)">
                      更多 ›
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </div>

        <!-- 右侧轮播图 -->
        <div class="carousel-panel">
          <el-carousel height="360px" :interval="4000" arrow="hover">
            <el-carousel-item v-for="(banner, index) in bannerList" :key="index">
              <div class="banner-slide" :style="{ background: banner.bg }">
                <div class="banner-content">
                  <h2 class="banner-title">{{ banner.title }}</h2>
                  <p class="banner-desc">{{ banner.desc }}</p>
                  <el-button type="primary" round @click="handleCategoryClick(banner.categoryId)">
                    立即查看
                  </el-button>
                </div>
                <img :src="banner.image" class="banner-image" />
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>

      <!-- ==================== 搜索栏 + 布局切换 ==================== -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            placeholder="搜索好物..."
            clearable
            size="large"
            prefix-icon="Search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
            class="search-input"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
            </template>
          </el-input>
          <!-- 当前筛选分类标签 -->
          <el-tag
            v-if="activeCategoryId"
            closable
            @close="clearCategoryFilter"
            class="filter-tag"
          >{{ activeCategoryName }}</el-tag>
        </div>

        <div class="layout-switch">
          <el-tooltip content="网格视图" placement="top">
            <el-button :type="layoutMode === 'grid' ? 'primary' : 'default'" @click="layoutMode = 'grid'">
              <el-icon size="18"><Grid /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="列表视图" placement="top">
            <el-button :type="layoutMode === 'list' ? 'primary' : 'default'" @click="layoutMode = 'list'">
              <el-icon size="18"><List /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>

      <!-- ==================== 网格布局 ==================== -->
      <div v-if="layoutMode === 'grid'" v-loading="loading" class="product-grid">
        <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />
        <el-card
          v-for="item in productList"
          :key="item.id"
          shadow="hover"
          class="grid-card"
          @click="goDetail(item.id)"
        >
          <div class="card-image">
            <el-image :src="item.mainImage" fit="cover" loading="lazy">
              <template #error>
                <div class="image-placeholder"><el-icon size="40"><Picture /></el-icon></div>
              </template>
            </el-image>
          </div>
          <div class="card-body">
            <h3 class="product-name">{{ item.name }}</h3>
            <p class="product-desc">{{ item.subtitle }}</p>
            <div class="product-price">
              <span class="price-symbol">￥</span>
              <span class="price-value">{{ item.price }}</span>
              <span v-if="item.originalPrice > item.price" class="original-price">￥{{ item.originalPrice }}</span>
            </div>
            <div class="product-meta">
              <span class="shop-name">{{ item.shopName }}</span>
              <span class="sales">已售 {{ item.sales }}</span>
            </div>
            <div v-if="userStore.isLoggedIn" class="card-actions" @click.stop>
              <el-button type="warning" size="small" plain @click.stop="handleAddCart(item)" :loading="cartLoadingMap[item.id]">
                <el-icon><ShoppingCart /></el-icon> 加入购物车
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- ==================== 列表布局 ==================== -->
      <div v-if="layoutMode === 'list'" v-loading="loading" class="product-list">
        <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />
        <div
          v-for="item in productList"
          :key="item.id"
          class="list-item"
          @click="goDetail(item.id)"
        >
          <div class="list-image">
            <el-image :src="item.mainImage" fit="cover" loading="lazy">
              <template #error>
                <div class="image-placeholder"><el-icon size="32"><Picture /></el-icon></div>
              </template>
            </el-image>
          </div>
          <div class="list-info">
            <h3 class="product-name">{{ item.name }}</h3>
            <p class="product-desc">{{ item.subtitle }}</p>
            <div class="list-meta">
              <span class="shop-name">{{ item.shopName }}</span>
              <span class="sales">已售 {{ item.sales }} 件</span>
            </div>
          </div>
          <div class="list-right">
            <div class="product-price">
              <span class="price-symbol">￥</span>
              <span class="price-value">{{ item.price }}</span>
            </div>
            <span v-if="item.originalPrice > item.price" class="original-price">￥{{ item.originalPrice }}</span>
            <div v-if="userStore.isLoggedIn" class="list-actions" @click.stop>
              <el-button type="primary" size="small" @click.stop="handleAddCart(item)" :loading="cartLoadingMap[item.id]">
                <el-icon><ShoppingCart /></el-icon> 加入购物车
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- ==================== 分页 ==================== -->
      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, jumper, ->, total"
          background
          @current-change="fetchProducts"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Picture, ShoppingCart, Grid, List } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'
import { getProductList } from '@/api/product'
import { getCategoryTree } from '@/api/category'

const router = useRouter()
const userStore = useUserStore()

// ==================== 分类数据 ====================
const categoryTree = ref([])
const hoverCatId = ref(null)  // 当前悬浮的一级分类ID

// 当前悬浮的一级分类对象（含 children）
const hoverCategory = computed(() => {
  return categoryTree.value.find(c => c.id === hoverCatId.value) || null
})

// 缓存所有商品数据（用于抽屉内展示各分类下的商品预览）
const allProductsCache = ref([])

/** 获取某个二级分类下的商品（从缓存中取前4个） */
function getProductsByCategory(subCategoryId) {
  return allProductsCache.value
    .filter(p => p.categoryId === subCategoryId)
    .slice(0, 4)
}

// 轮播图数据（使用各品类图片 + 渐变背景）
const bannerList = ref([
  {
    title: '潮流服饰 焕新登场',
    desc: '精选好物 低至5折',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    image: '/images/Cosmetics/a.png',
  
    categoryId: 1,
  },
  {
    title: '美妆盛典 限时特惠',
    desc: '大牌美妆 满减优惠',
    bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    image:  '/images/Clothing/2b8e92c98c34000e7c77044a508f03f9.png',
    categoryId: 2,
  },
  {
    title: '数码好物 直降到底',
    desc: '爆款3C 每满300减50',
    bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    image: '/images/Electronics/03a0828dbcbf74c7133a4ae1f95cf993.png',
    categoryId: 3,
  },
  {
    title: '零食狂欢 嗨吃不停',
    desc: '全球零食 满99减20',
    bg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    image: '/images/Snacks/1b435e07a0d8754cf9bfd7a0433016ce.png',
    categoryId: 4,
  },
])

// ==================== 商品数据 ====================
const keyword = ref('')
const productList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const cartLoadingMap = reactive({})
const layoutMode = ref('grid')

// 当前筛选的分类
const activeCategoryId = ref(null)
const activeCategoryName = computed(() => {
  for (const cat of categoryTree.value) {
    if (cat.id === activeCategoryId.value) return cat.name
    if (cat.children) {
      const sub = cat.children.find(c => c.id === activeCategoryId.value)
      if (sub) return sub.name
    }
  }
  return ''
})

// ==================== 加载分类 ====================
async function fetchCategories() {
  try {
    const res = await getCategoryTree()
    categoryTree.value = res.data || []
  } catch {
    categoryTree.value = []
  }
}

// ==================== 加载商品 ====================
async function fetchProducts() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (keyword.value) params.keyword = keyword.value
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value

    const res = await getProductList(params)
    const pageData = res.data || {}
    productList.value = pageData.records || []
    total.value = pageData.total || 0
  } catch {
    productList.value = []
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================
function handleSearch() {
  activeCategoryId.value = null
  pageNum.value = 1
  fetchProducts()
}

/** 点击分类筛选商品 */
function handleCategoryClick(categoryId) {
  activeCategoryId.value = categoryId
  keyword.value = ''
  pageNum.value = 1
  fetchProducts()
  // 滚动到商品列表区域
  document.querySelector('.toolbar')?.scrollIntoView({ behavior: 'smooth' })
}

/** 清除分类筛选 */
function clearCategoryFilter() {
  activeCategoryId.value = null
  pageNum.value = 1
  fetchProducts()
}

async function handleAddCart(item) {
  cartLoadingMap[item.id] = true
  try {
    // 走 cart store，加购成功后顶栏购物车徽标实时更新
    await cartStore.addItem({ productId: item.id, quantity: 1 })
  } catch {} finally { cartLoadingMap[item.id] = false }
}

function goDetail(id) { router.push({ name: 'ProductDetail', params: { id } }) }

/** 加载全部商品缓存（用于抽屉内展示各分类下的商品预览） */
async function fetchAllProductsCache() {
  try {
    const res = await getProductList({ pageNum: 1, pageSize: 200 })
    allProductsCache.value = res.data?.records || []
  } catch {
    allProductsCache.value = []
  }
}

onMounted(() => {
  fetchCategories()
  fetchProducts()
  fetchAllProductsCache()
})
</script>

<style lang="scss" scoped>
.home-page { min-height: calc(100vh - 60px); background: #f5f5f5; }

/* ========== 分类 + 轮播图 水平布局 ========== */
.hero-section {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  position: relative;
}

/* 分类区域包裹器（列表+抽屉共用父容器，鼠标移入抽屉不会触发关闭） */
.category-wrapper {
  position: relative;
  flex-shrink: 0;
  width: 210px;
  z-index: 200;
}

/* 左侧分类面板 */
.category-panel {
  width: 210px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.category-title {
  padding: 14px 16px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  background: #f8f8f8;
  border-bottom: 1px solid #eee;
  border-radius: 8px 8px 0 0;
}
.category-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.category-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 11px 16px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  border-bottom: 1px solid #f5f5f5;
  &:hover, &.active {
    background: #f0f5ff;
    color: #409eff;
    .cat-name { color: #409eff; }
    .cat-arrow { color: #409eff; }
  }
}
.cat-icon { width: 24px; height: 24px; border-radius: 4px; object-fit: cover; }
.cat-name { flex: 1; font-size: 14px; color: #303133; transition: color 0.15s; }
.cat-arrow { font-size: 16px; color: #c0c4cc; transition: color 0.15s; }

/* ========== 抽屉式分类详情面板 ========== */
.category-drawer {
  position: absolute;
  left: 100%;
  top: 0;
  width: 700px;
  min-height: 360px;
  background: #fff;
  border-radius: 0 8px 8px 0;
  box-shadow: 6px 0 24px rgba(0,0,0,0.1);
  z-index: 199;
  padding: 20px 24px;
  overflow-y: auto;
}
.drawer-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}
.drawer-icon { width: 32px; height: 32px; border-radius: 6px; object-fit: cover; }
.drawer-title { font-size: 18px; font-weight: 600; color: #303133; margin: 0; flex: 1; }

.drawer-body { display: flex; flex-direction: column; gap: 20px; }
.drawer-group {}
.group-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  padding: 6px 0;
  cursor: pointer;
  &:hover { color: #409eff; }
}
.group-items {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.group-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 80px;
  cursor: pointer;
  transition: transform 0.15s;
  &:hover { transform: translateY(-2px); .item-name { color: #409eff; } }
  &.more {
    justify-content: center;
    font-size: 12px;
    color: #909399;
    &:hover { color: #409eff; }
  }
}
.item-thumb {
  width: 64px; height: 64px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f7fa;
  border: 1px solid #eee;
}
.item-name {
  font-size: 12px;
  color: #606266;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  transition: color 0.15s;
}

/* 抽屉动画 */
.drawer-slide-enter-active { transition: all 0.2s ease-out; }
.drawer-slide-leave-active { transition: all 0.15s ease-in; }
.drawer-slide-enter-from { opacity: 0; transform: translateX(-10px); }
.drawer-slide-leave-to { opacity: 0; transform: translateX(-10px); }

/* 右侧轮播图面板 */
.carousel-panel {
  flex: 1;
  margin-left: 12px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.banner-slide {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 360px;
  padding: 0 50px;
}
.banner-content {
  color: #fff;
  z-index: 2;
}
.banner-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}
.banner-desc {
  font-size: 16px;
  margin: 0 0 24px;
  opacity: 0.9;
}
.banner-image {
  width: 240px;
  height: 240px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
}

/* ========== 工具栏 ========== */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  gap: 16px;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}
.search-input { max-width: 480px; }
.filter-tag { font-size: 13px; }
.layout-switch {
  display: flex; gap: 0;
  .el-button { border-radius: 0; }
  .el-button:first-child { border-radius: 4px 0 0 4px; }
  .el-button:last-child { border-radius: 0 4px 4px 0; }
}

/* ========== 网格布局 ========== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  min-height: 300px;
}
.grid-card {
  cursor: pointer;
  transition: transform 0.2s;
  border-radius: 8px;
  overflow: hidden;
  &:hover { transform: translateY(-4px); }
  :deep(.el-card__body) { padding: 0; }
}
.card-image {
  width: 100%; height: 220px; overflow: hidden; background: #fafafa;
  .el-image { width: 100%; height: 100%; }
  .image-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; background: #f5f7fa; }
}
.card-body { padding: 14px; }
.product-name { font-size: 15px; font-weight: 600; color: #303133; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-desc { margin-top: 4px; font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price { margin-top: 8px; color: #f56c6c; .price-symbol { font-size: 13px; } .price-value { font-size: 20px; font-weight: 700; } }
.original-price { font-size: 12px; color: #c0c4cc; text-decoration: line-through; margin-left: 6px; }
.product-meta { display: flex; justify-content: space-between; margin-top: 6px; font-size: 12px; color: #909399; }
.card-actions { display: flex; gap: 8px; margin-top: 10px; }

/* ========== 列表布局 ========== */
.product-list { display: flex; flex-direction: column; gap: 12px; min-height: 300px; }
.list-item {
  display: flex; align-items: center; background: #fff; border-radius: 8px; padding: 16px;
  cursor: pointer; transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
}
.list-image {
  flex-shrink: 0; width: 140px; height: 140px; border-radius: 6px; overflow: hidden; background: #fafafa;
  .el-image { width: 100%; height: 100%; }
  .image-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; background: #f5f7fa; }
}
.list-info { flex: 1; padding: 0 20px; overflow: hidden; .product-name { font-size: 16px; } .product-desc { margin-top: 8px; font-size: 13px; } }
.list-meta { display: flex; gap: 16px; margin-top: 10px; font-size: 12px; color: #909399; }
.list-right { flex-shrink: 0; text-align: right; min-width: 140px; .product-price { margin-top: 0; } .original-price { display: block; margin: 4px 0 0 0; text-align: right; } }
.list-actions { margin-top: 12px; }

/* ========== 分页 ========== */
.pagination-wrap { display: flex; justify-content: center; padding: 30px 0; }
</style>
