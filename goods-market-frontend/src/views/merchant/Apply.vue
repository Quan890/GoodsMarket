<template>
  <div class="apply-page">
    <h2 class="page-title">商家入驻申请</h2>

    <!-- 加载中 -->
    <div v-if="statusLoading" v-loading="true" style="min-height: 200px" />

    <!-- ==================== 已有状态：展示审核结果 ==================== -->
    <template v-else-if="merchantStatus">
      <!-- 待审核 -->
      <div v-if="merchantStatus.auditStatus === 0" class="status-card pending">
        <el-icon :size="48" color="#e6a23c"><Clock /></el-icon>
        <h3>审核中</h3>
        <p>您的入驻申请正在审核中，请耐心等待管理员审核。</p>
        <div class="status-info">
          <p><strong>店铺名称：</strong>{{ merchantStatus.shopName }}</p>
          <p><strong>提交时间：</strong>{{ merchantStatus.createTime }}</p>
        </div>
      </div>

      <!-- 审核通过 -->
      <div v-else-if="merchantStatus.auditStatus === 1" class="status-card approved">
        <el-icon :size="48" color="#67c23a"><CircleCheckFilled /></el-icon>
        <h3>已通过</h3>
        <p>恭喜您，入驻申请已通过！您现在可以管理商品和店铺订单。</p>
        <div class="status-info">
          <p><strong>店铺名称：</strong>{{ merchantStatus.shopName }}</p>
        </div>
        <el-button type="primary" @click="router.push({ name: 'MerchantProduct' })">
          进入商品管理
        </el-button>
      </div>

      <!-- 审核驳回 -->
      <div v-else-if="merchantStatus.auditStatus === 2" class="status-card rejected">
        <el-icon :size="48" color="#f56c6c"><CircleCloseFilled /></el-icon>
        <h3>已驳回</h3>
        <p>很抱歉，您的入驻申请未通过审核。</p>
        <div class="status-info">
          <p><strong>驳回原因：</strong>{{ merchantStatus.auditRemark || '未说明' }}</p>
        </div>
        <el-button type="primary" @click="handleReapply">
          重新申请
        </el-button>
      </div>
    </template>

    <!-- ==================== 申请表单 ==================== -->
    <template v-else>
      <div class="apply-form-card">
        <p class="form-tip">填写以下信息提交入驻申请，审核通过后即可成为商家。</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          size="large"
        >
          <el-form-item label="店铺名称" prop="shopName">
            <el-input
              v-model="form.shopName"
              placeholder="请输入店铺名称（2-20个字符）"
              maxlength="20"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="联系电话" prop="contactPhone">
            <el-input
              v-model="form.contactPhone"
              placeholder="请输入联系电话"
              maxlength="11"
            />
          </el-form-item>

          <el-form-item label="联系人" prop="contactName">
            <el-input
              v-model="form.contactName"
              placeholder="请输入联系人姓名"
              maxlength="50"
            />
          </el-form-item>

          <el-form-item label="经营地址" prop="address">
            <el-input
              v-model="form.address"
              placeholder="请输入经营地址"
              maxlength="300"
            />
          </el-form-item>

          <el-form-item label="店铺描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请描述您的店铺主营方向（10-200个字符）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="营业执照号" prop="licenseNo">
            <el-input
              v-model="form.licenseNo"
              placeholder="请输入营业执照号（选填）"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="submitting"
              @click="handleSubmit"
            >
              提交申请
            </el-button>
            <el-button @click="formRef?.resetFields()">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { merchantApply, getMerchantStatus } from '@/api/merchant'
import { isValidPhone } from '@/utils/common'

const router = useRouter()

// ==================== 审核状态 ====================

const merchantStatus = ref(null)
const statusLoading = ref(true)

/**
 * 查询商家入驻状态
 */
async function fetchStatus() {
  statusLoading.value = true
  try {
    const res = await getMerchantStatus()
    merchantStatus.value = res.data || null
  } catch {
    // 未入驻或接口异常，保持 null → 显示申请表单
    merchantStatus.value = null
  } finally {
    statusLoading.value = false
  }
}

// ==================== 申请表单 ====================

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  shopName: '',
  contactPhone: '',
  contactName: '',
  address: '',
  description: '',
  licenseNo: '',
})

const rules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 20, message: '店铺名称长度为 2-20 个字符', trigger: 'blur' },
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!isValidPhone(value)) {
          callback(new Error('手机号格式不正确'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
  ],
  address: [
    { required: true, message: '请输入经营地址', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入店铺描述', trigger: 'blur' },
    { min: 10, max: 200, message: '店铺描述长度为 10-200 个字符', trigger: 'blur' },
  ],
  licenseNo: [], // 选填，无校验
}

/**
 * 提交入驻申请
 */
async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    await merchantApply({
      shopName: form.shopName,
      contactPhone: form.contactPhone,
      contactName: form.contactName,
      address: form.address,
      description: form.description,
      licenseNo: form.licenseNo || undefined,
    })
    ElMessage.success('申请已提交，5秒后自动返回个人中心')
    // 5秒后跳转到个人中心
    setTimeout(() => {
      router.push({ name: 'UserCenter' })
    }, 5000)
  } catch {
    // 错误已由 request.js 拦截器处理
  } finally {
    submitting.value = false
  }
}

/**
 * 重新申请（清空状态，回到表单）
 */
function handleReapply() {
  merchantStatus.value = null
}

// ==================== 初始化 ====================

onMounted(() => {
  fetchStatus()
})
</script>

<style lang="scss" scoped>
.apply-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 60px);
  padding: 20px;
}

.page-title {
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

/* 审核状态卡片 */
.status-card {
  width: 100%;
  max-width: 700px;
  text-align: center;
  padding: 40px 30px;
  background: #fff;
  border-radius: 8px;

  h3 {
    margin-top: 16px;
    font-size: 20px;
    color: #303133;
  }

  p {
    margin-top: 8px;
    font-size: 14px;
    color: #909399;
  }

  .el-button {
    margin-top: 20px;
  }
}

.status-info {
  margin-top: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
  text-align: left;

  p {
    margin: 4px 0;
    font-size: 14px;
    color: #606266;
  }
}

/* 申请表单卡片 */
.apply-form-card {
  width: 100%;
  max-width: 700px;
  padding: 30px;
  background: #fff;
  border-radius: 8px;
}

.form-tip {
  margin-bottom: 24px;
  font-size: 14px;
  color: #909399;
}
</style>
