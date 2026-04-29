<template>
  <div class="store-manage">
    <div class="page-header">
      <h2>门店管理</h2>
      <el-button type="primary" @click="showAddDialog">新增门店</el-button>
    </div>

    <el-table :data="stores" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="门店名称" width="150" />
      <el-table-column prop="address" label="地址" min-width="200" />
      <el-table-column prop="phone" label="电话" width="120" />
      <el-table-column label="坐标" width="150">
        <template #default="scope">
          {{ scope.row.latitude ? `${scope.row.latitude}, ${scope.row.longitude}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '营业中' : '已关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" :type="scope.row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '关闭' : '营业' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入门店地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="form.latitude" :precision="6" :step="0.000001" :min="-90" :max="90" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="form.longitude" :precision="6" :step="0.000001" :min="-180" :max="180" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">营业中</el-radio>
            <el-radio :label="0">已关闭</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, onBeforeRouteUpdate } from 'vue-router'
import { adminApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()

const loading = ref(false)
const stores = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  address: '',
  phone: '',
  latitude: null,
  longitude: null,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入门店地址', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑门店' : '新增门店')

const loadStores = async () => {
  loading.value = true
  try {
    const data = await adminApi.getStores({ page: 1, size: 100 }) // 加载所有门店
    stores.value = data?.records || data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('加载门店列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    address: '',
    phone: '',
    latitude: null,
    longitude: null,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    address: row.address,
    phone: row.phone,
    latitude: row.latitude,
    longitude: row.longitude,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await adminApi.updateStore(form.id, form)
      ElMessage.success('门店更新成功')
    } else {
      await adminApi.addStore(form)
      ElMessage.success('门店添加成功')
    }
    dialogVisible.value = false
    loadStores()
  } catch (error) {
    console.error(error)
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '关闭' : '开启'
  try {
    await ElMessageBox.confirm(`确定要${action}门店"${row.name}"吗？`, '提示', { type: 'warning' })
    await adminApi.updateStore(row.id, { status: row.status === 1 ? 0 : 1 })
    ElMessage.success(`门店已${action}`)
    loadStores()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadStores()
})

// 使用导航守卫 - 当路由更新但组件复用时触发
onBeforeRouteUpdate((to, from, next) => {
  if (to.path === '/admin/stores') {
    loadStores()
  }
  next()
})
</script>

<style scoped>
.store-manage {
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
</style>
