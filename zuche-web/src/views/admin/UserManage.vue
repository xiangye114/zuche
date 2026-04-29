<template>
  <div class="user-manage">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showAddDialog" v-if="hasDeletePermission()">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </div>

    <!-- 操作栏 -->
    <div class="operation-bar">
      <el-button type="success" size="small" @click="handleExport" :loading="exportLoading">
        <el-icon><Download /></el-icon>导出Excel
      </el-button>
    </div>

    <!-- 批量操作栏 -->
    <div class="batch-operation-bar" v-if="selectedUsers.length > 0">
      <span class="selected-count">已选择 {{ selectedUsers.length }} 项</span>
      <el-button type="primary" size="small" @click="handleBatchEnable" :loading="batchLoading">
        批量启用
      </el-button>
      <el-button type="warning" size="small" @click="handleBatchDisable" :loading="batchLoading">
        批量禁用
      </el-button>
      <el-button type="danger" size="small" @click="handleBatchDelete" :loading="batchLoading" v-if="hasDeletePermission()">
        批量删除
      </el-button>
      <el-button size="small" @click="clearSelection">取消选择</el-button>
    </div>

    <el-table
      :data="users"
      style="width: 100%"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      ref="tableRef"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="用户ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="角色" width="120">
        <template #default="scope">
          <el-tag :type="getRoleType(scope.row.role)">
            {{ getRoleName(scope.row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" :type="scope.row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 用户编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>

        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          <div class="form-tip" v-if="isEdit">留空则不修改密码</div>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" :value="0" />
            <el-option label="管理员" :value="1" />
            <el-option label="超级管理员" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, onBeforeRouteUpdate } from 'vue-router'
import { adminApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hasDeletePermission, RoleNameMap, UserRole } from '@/utils/permission'
import { exportExcel } from '@/utils/export'
import { Download, Plus } from '@element-plus/icons-vue'

const route = useRoute()

const loading = ref(false)
const batchLoading = ref(false)
const exportLoading = ref(false)
const submitLoading = ref(false)
const users = ref([])
const selectedUsers = ref([])
const tableRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  username: '',
  password: '',
  phone: '',
  email: '',
  role: 0
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取角色名称
const getRoleName = (role) => {
  return RoleNameMap[role] || '普通用户'
}

// 获取角色标签类型
const getRoleType = (role) => {
  switch (role) {
    case UserRole.SUPER_ADMIN:
      return 'danger'
    case UserRole.STORE_ADMIN:
      return 'warning'
    default:
      return 'info'
  }
}

const loadUsers = async () => {
  loading.value = true
  try {
    const data = await adminApi.getUsers({ page: 1, size: 100 })
    users.value = data?.records || data || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 清除选择
const clearSelection = () => {
  tableRef.value?.clearSelection()
  selectedUsers.value = []
}

// 批量启用
const handleBatchEnable = async () => {
  await handleBatchUpdateStatus(1, '启用')
}

// 批量禁用
const handleBatchDisable = async () => {
  await handleBatchUpdateStatus(0, '禁用')
}

// 批量更新状态
const handleBatchUpdateStatus = async (status, actionName) => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请先选择用户')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要${actionName}选中的 ${selectedUsers.value.length} 个用户吗？`,
      '提示',
      { type: 'warning' }
    )

    batchLoading.value = true
    const ids = selectedUsers.value.map(user => user.id)
    await adminApi.batchUpdateUserStatus({ ids, status })
    ElMessage.success(`批量${actionName}成功`)
    clearSelection()
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量操作失败:', error)
      ElMessage.error('批量操作失败')
    }
  } finally {
    batchLoading.value = false
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请先选择用户')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？此操作不可恢复！`,
      '警告',
      { type: 'error' }
    )

    batchLoading.value = true
    const ids = selectedUsers.value.map(user => user.id)
    await adminApi.batchDeleteUsers({ ids })
    ElMessage.success('批量删除成功')
    clearSelection()
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    batchLoading.value = false
  }
}

// 显示新增对话框
const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.password = ''
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.role = row.role || 0
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.username = ''
  form.password = ''
  form.phone = ''
  form.email = ''
  form.role = 0
  formRef.value?.clearValidate()
}

// 关闭对话框
const handleDialogClose = () => {
  resetForm()
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = {
      username: form.username,
      phone: form.phone || null,
      email: form.email || null,
      role: form.role
    }

    if (!isEdit.value) {
      submitData.password = form.password
    } else if (form.password) {
      submitData.password = form.password
    }

    if (isEdit.value) {
      await adminApi.updateUser(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await adminApi.addUser(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadUsers()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户"${row.username}"吗？`, '提示', { type: 'warning' })
    await adminApi.updateUserStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(`用户已${action}`)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 导出Excel
const handleExport = async () => {
  exportLoading.value = true
  try {
    await exportExcel(
      adminApi.exportUsers({ keyword: '', status: null }),
      `用户列表_${new Date().toLocaleDateString()}.xlsx`
    )
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  loadUsers()
})

onBeforeRouteUpdate((to, from, next) => {
  if (to.path === '/admin/users') {
    loadUsers()
  }
  next()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
  color: #333;
}

.operation-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.batch-operation-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.selected-count {
  color: #606266;
  font-size: 14px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
