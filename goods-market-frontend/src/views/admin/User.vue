<template>
  <div class="user-page">
    <h2 class="page-title">用户管理</h2>
    <div class="filter-bar">
      <el-input v-model="filters.phone" placeholder="手机号搜索" clearable prefix-icon="Search" style="width: 200px" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-select v-model="filters.role" placeholder="角色" clearable style="width: 120px" @change="handleSearch">
        <el-option label="用户" :value="1" /><el-option label="商家" :value="2" /><el-option label="管理员" :value="3" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
        <el-option label="正常" :value="1" /><el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
    </div>
    <el-table v-loading="loading" :data="userList" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="nickname" label="昵称" min-width="120"><template #default="{ row }">{{ row.nickname || '-' }}</template></el-table-column>
      <el-table-column label="角色" width="100" align="center"><template #default="{ row }"><el-tag :type="roleTagType(row.role)" size="small">{{ roleName(row.role) }}</el-tag></template></el-table-column>
      <el-table-column label="状态" width="100" align="center"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button :type="row.status === 1 ? 'warning' : 'success'" link size="small" @click="handleToggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button type="primary" link size="small" @click="openRoleDialog(row)">修改角色</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next, jumper, ->, total" background @current-change="fetchUsers" />
    </div>
    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="400px">
      <p style="margin-bottom: 16px; color: #606266">用户：<strong>{{ editingUser?.phone }}</strong>（当前角色：{{ roleName(editingUser?.role) }}）</p>
      <el-radio-group v-model="newRole">
        <el-radio :value="1">用户</el-radio><el-radio :value="2">商家</el-radio><el-radio :value="3">管理员</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitting" @click="handleRoleSubmit">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getUserList, updateUserStatus, updateUserRole } from '@/api/admin'

const userList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filters = reactive({ phone: '', role: undefined, status: undefined })

function roleName(role) { return { 1: '用户', 2: '商家', 3: '管理员' }[role] ?? '未知' }
function roleTagType(role) { return { 1: '', 2: 'warning', 3: 'danger' }[role] ?? 'info' }

async function fetchUsers() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filters.phone) params.phone = filters.phone
    if (filters.role !== undefined) params.role = filters.role
    if (filters.status !== undefined) params.status = filters.status
    const res = await getUserList(params)
    const page = res.data || {}
    userList.value = page.records || []
    total.value = page.total || 0
  } catch { userList.value = [] } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchUsers() }

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try { await ElMessageBox.confirm(`确定${action}用户 ${row.phone}？`, `${action}确认`, { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }) } catch { return }
  try { await updateUserStatus(row.id, newStatus); ElMessage.success(`已${action}`); fetchUsers() } catch {}
}

const roleDialogVisible = ref(false)
const editingUser = ref(null)
const newRole = ref(1)
const roleSubmitting = ref(false)

function openRoleDialog(row) { editingUser.value = row; newRole.value = row.role; roleDialogVisible.value = true }

async function handleRoleSubmit() {
  if (newRole.value === editingUser.value.role) { roleDialogVisible.value = false; return }
  roleSubmitting.value = true
  try { await updateUserRole({ userId: editingUser.value.id, role: newRole.value }); ElMessage.success('角色已更新'); roleDialogVisible.value = false; fetchUsers() }
  catch {} finally { roleSubmitting.value = false }
}

onMounted(() => fetchUsers())
</script>

<style lang="scss" scoped>
.user-page { padding: 0; }
.page-title { margin-bottom: 20px; font-size: 20px; font-weight: 600; color: #303133; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination-wrap { display: flex; justify-content: center; padding: 24px 0; }
</style>
