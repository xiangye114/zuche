<template>
  <div class="brand-manage">
    <div class="page-header">
      <h2>品牌管理</h2>
      <el-button type="primary" @click="showAddDialog">新增品牌</el-button>
    </div>

    <el-table :data="brands" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="品牌名称" />
      <el-table-column prop="logo" label="Logo">
        <template #default="scope">
          <el-image v-if="scope.row.logo" :src="scope.row.logo" style="width: 50px; height: 50px; object-fit: contain;" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="品牌名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入品牌名称" />
        </el-form-item>
        <el-form-item label="Logo">
          <el-input v-model="form.logo" placeholder="请输入Logo URL" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入品牌描述" type="textarea" :rows="3" />
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
const brands = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = reactive({
  id: null,
  name: '',
  logo: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入品牌名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑品牌' : '新增品牌')

const loadBrands = async () => {
  loading.value = true
  try {
    const data = await adminApi.getBrands()
    brands.value = data || []
  } catch (error) {
    console.error('加载品牌列表失败:', error)
    ElMessage.error('加载品牌列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    logo: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    logo: row.logo,
    description: row.description
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await adminApi.updateBrand(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await adminApi.addBrand(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadBrands()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除品牌"${row.name}"吗？`, '提示', { type: 'warning' })
    await adminApi.deleteBrand(row.id)
    ElMessage.success('删除成功')
    loadBrands()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadBrands()
})

// 使用导航守卫 - 当路由更新但组件复用时触发
onBeforeRouteUpdate((to, from, next) => {
  if (to.path === '/admin/brands') {
    loadBrands()
  }
  next()
})
</script>

<style scoped>
.brand-manage {
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
