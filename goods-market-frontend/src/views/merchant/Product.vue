<template>
  <div class="product-page">
    <div class="page-header">
      <h2 class="page-title">商品管理</h2>
      <el-button type="primary" @click="openDialog('add')"><el-icon><Plus /></el-icon>新增商品</el-button>
    </div>
    <el-radio-group v-model="statusFilter" @change="handleFilterChange">
      <el-radio-button :value="undefined">全部</el-radio-button>
      <el-radio-button :value="1">在售</el-radio-button>
      <el-radio-button :value="0">已下架</el-radio-button>
    </el-radio-group>
    <el-table v-loading="loading" :data="productList" stripe style="margin-top: 16px">
      <el-table-column label="商品" min-width="260">
        <template #default="{ row }">
          <div class="product-cell">
            <el-image :src="row.mainImage" fit="cover" class="product-thumb">
              <template #error><div class="thumb-placeholder"><el-icon size="20"><Picture /></el-icon></div></template>
            </el-image>
            <div class="product-text">
              <span class="product-name">{{ row.name }}</span>
              <span class="product-desc">{{ row.subtitle }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="140" align="center">
        <template #default="{ row }">
          <span class="price-text">￥{{ row.price }}</span>
          <span v-if="row.originalPrice > row.price" class="original-price">￥{{ row.originalPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '在售' : '已下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog('edit', row)">编辑</el-button>
          <el-button :type="row.status === 1 ? 'warning' : 'success'" link size="small" @click="handleToggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next, jumper, ->, total" background @current-change="fetchProducts" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增商品' : '编辑商品'" width="560px" :close-on-click-modal="false" @closed="resetForm">
      <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="productForm.subtitle" placeholder="一句话描述商品卖点" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="售价(元)" prop="price">
          <el-input-number v-model="productForm.price" :min="0.01" :max="99999" :precision="2" :step="1" controls-position="right" style="width: 200px" />
        </el-form-item>
        <el-form-item label="原价(元)" prop="originalPrice">
          <el-input-number v-model="productForm.originalPrice" :min="0" :max="99999" :precision="2" :step="1" controls-position="right" style="width: 200px" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" :max="999999" controls-position="right" style="width: 200px" />
        </el-form-item>
        <el-form-item label="商品图片" prop="mainImage">
          <div class="image-upload-row">
            <el-upload
              :show-file-list="false"
              :http-request="handleUpload"
              :before-upload="beforeUpload"
              accept="image/jpeg,image/png,image/gif,image/webp"
            >
              <div v-if="productForm.mainImage" class="image-preview-box">
                <el-image :src="productForm.mainImage" fit="cover" style="width: 100px; height: 100px">
                  <template #error><div class="preview-error">图片加载失败</div></template>
                </el-image>
                <span class="reupload-tip">点击更换</span>
              </div>
              <el-button v-else type="primary" plain :loading="uploading">
                <el-icon><Upload /></el-icon>上传图片
              </el-button>
            </el-upload>
            <el-input v-model="productForm.mainImage" placeholder="也可直接填写图片 URL" style="flex: 1">
              <template #prepend>URL</template>
            </el-input>
          </div>
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="categoryPath"
            :options="categoryOptions"
            :props="{ value: 'id', label: 'name', emitPath: true }"
            placeholder="请选择一级/二级分类"
            style="width: 300px"
            @change="onCategoryChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSubmitting" @click="handleFormSubmit">{{ dialogMode === 'add' ? '确认新增' : '保存修改' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture, Upload } from '@element-plus/icons-vue'
import { getMyProducts, addProduct, editProduct, changeProductStatus, deleteProduct } from '@/api/merchant'
import { uploadImage } from '@/api/upload'
import { getCategoryTree } from '@/api/category'

const productList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref(undefined)

async function fetchProducts() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (statusFilter.value !== undefined) params.status = statusFilter.value
    const res = await getMyProducts(params)
    const pageData = res.data || {}
    productList.value = pageData.records || []
    total.value = pageData.total || 0
  } catch { productList.value = [] } finally { loading.value = false }
}

function handleFilterChange() { pageNum.value = 1; fetchProducts() }

const dialogVisible = ref(false)
const dialogMode = ref('add')
const editingId = ref(null)
const productFormRef = ref(null)
const formSubmitting = ref(false)

const productForm = reactive({ name: '', subtitle: '', price: null, originalPrice: null, stock: 100, mainImage: '', categoryId: null })
const productRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  subtitle: [{ required: true, message: '请输入副标题', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  mainImage: [{ required: true, message: '请上传商品图片或填写图片 URL', trigger: 'blur' }],
  categoryId: [{ required: true, validator: (rule, value, cb) => { value ? cb() : cb(new Error('请选择商品分类')) }, trigger: 'change' }],
}

function openDialog(mode, row) {
  dialogMode.value = mode
  if (mode === 'edit' && row) {
    editingId.value = row.id
    productForm.name = row.name
    productForm.subtitle = row.subtitle
    productForm.price = Number(row.price)
    productForm.originalPrice = Number(row.originalPrice || 0)
    productForm.stock = row.stock
    productForm.mainImage = row.mainImage
    productForm.categoryId = row.categoryId
  } else {
    productForm.categoryId = null
  }
  syncCategoryPath()
  dialogVisible.value = true
}

// ==================== 分类级联选择 ====================
const categoryOptions = ref([])
/** 级联选择器绑定值：[一级ID, 二级ID] */
const categoryPath = ref([])
// 删除 categoryIdRule 变量（校验规则已内联到 productRules）

async function fetchCategories() {
  try {
    const res = await getCategoryTree()
    categoryOptions.value = res.data || []
  } catch { categoryOptions.value = [] }
}

/** 根据分类 ID 反查级联路径（编辑回显用） */
function syncCategoryPath() {
  const targetId = productForm.categoryId
  if (!targetId) { categoryPath.value = []; return }
  for (const parent of categoryOptions.value) {
    if (parent.id === targetId) { categoryPath.value = [parent.id]; return }
    for (const child of parent.children || []) {
      if (child.id === targetId) { categoryPath.value = [parent.id, child.id]; return }
    }
  }
  categoryPath.value = []
}

function onCategoryChange() {
  // 取级联路径的最后一级作为分类 ID；仅选一级时用一级 ID
  productForm.categoryId = categoryPath.value.length > 0
    ? categoryPath.value[categoryPath.value.length - 1]
    : null
}

// ==================== 图片上传 ====================
const uploading = ref(false)

function beforeUpload(file) {
  const okTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!okTypes.includes(file.type)) {
    ElMessage.error('仅支持 jpg/png/gif/webp 格式图片')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

async function handleUpload({ file }) {
  if (!beforeUpload(file)) return
  uploading.value = true
  try {
    const res = await uploadImage(file)
    productForm.mainImage = res.data.url
    ElMessage.success('图片上传成功')
  } catch {} finally { uploading.value = false }
}

function resetForm() {
  productFormRef.value?.resetFields()
  editingId.value = null
}

async function handleFormSubmit() {
  try { await productFormRef.value.validate() } catch { return }
  formSubmitting.value = true
  try {
    const payload = { ...productForm }
    if (dialogMode.value === 'add') { await addProduct(payload); ElMessage.success('商品已新增') }
    else { await editProduct({ ...payload, id: editingId.value }); ElMessage.success('商品已更新') }
    dialogVisible.value = false; fetchProducts()
  } catch {} finally { formSubmitting.value = false }
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '上架' : '下架'
  try { await ElMessageBox.confirm(`确定${actionText}商品「${row.name}」？`, `${actionText}确认`, { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }) } catch { return }
  try { await changeProductStatus(row.id, newStatus); ElMessage.success(`已${actionText}`); fetchProducts() } catch {}
}

async function handleDelete(row) {
  try { await ElMessageBox.confirm(`确定删除商品「${row.name}」？删除后无法恢复！`, '删除确认', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }) } catch { return }
  try { await deleteProduct(row.id); ElMessage.success('商品已删除'); if (productList.value.length === 1 && pageNum.value > 1) pageNum.value--; fetchProducts() } catch {}
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.product-page { padding: 20px 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #303133; }
.product-cell { display: flex; align-items: center; gap: 12px; }
.product-thumb { flex-shrink: 0; width: 60px; height: 60px; border-radius: 4px; overflow: hidden; background: #f5f7fa; }
.thumb-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; }
.product-text { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.product-name { font-size: 14px; font-weight: 500; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-desc { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price-text { font-weight: 600; color: #f56c6c; }
.original-price { font-size: 12px; color: #c0c4cc; text-decoration: line-through; margin-left: 6px; }
.image-preview-box { position: relative; display: inline-block; border-radius: 4px; overflow: hidden; cursor: pointer; .reupload-tip { position: absolute; left: 0; right: 0; bottom: 0; padding: 2px 0; font-size: 11px; color: #fff; text-align: center; background: rgba(0,0,0,0.45); } }
.image-upload-row { display: flex; align-items: flex-start; gap: 12px; width: 100%; }
.preview-error { display: flex; align-items: center; justify-content: center; width: 100px; height: 100px; font-size: 12px; color: #c0c4cc; background: #f5f7fa; }
.pagination-wrap { display: flex; justify-content: center; padding: 24px 0; }
</style>
