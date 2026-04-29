<template>
  <div class="knowledge-manage">
    <div class="page-header">
      <h2>知识库管理</h2>
      <el-button type="primary" @click="showAddDialog">新增知识</el-button>
    </div>

    <el-table :data="knowledgeItems" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="question" label="问题" min-width="200" />
      <el-table-column prop="answer" label="答案" min-width="300" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="问题" prop="question">
          <el-input v-model="form.question" placeholder="请输入问题" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input v-model="form.answer" placeholder="请输入答案" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
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
const knowledgeItems = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = reactive({
  id: null,
  question: '',
  answer: '',
  category: '',
  priority: 0,
  status: 1
})

const rules = {
  question: [{ required: true, message: '请输入问题', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入答案', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑知识' : '新增知识')

const loadKnowledge = async () => {
  loading.value = true
  try {
    const data = await adminApi.getKnowledge({ page: 1, size: 100 })
    knowledgeItems.value = data?.records || data || []
  } catch (error) {
    console.error('加载知识库失败:', error)
    ElMessage.error('加载知识库失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    question: '',
    answer: '',
    category: '',
    priority: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    question: row.question,
    answer: row.answer,
    category: row.category,
    priority: row.priority,
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
      await adminApi.updateKnowledge(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await adminApi.addKnowledge(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadKnowledge()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除该知识条目吗？`, '提示', { type: 'warning' })
    await adminApi.deleteKnowledge(row.id)
    ElMessage.success('删除成功')
    loadKnowledge()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadKnowledge()
})

// 使用导航守卫 - 当路由更新但组件复用时触发
onBeforeRouteUpdate((to, from, next) => {
  if (to.path === '/admin/knowledge') {
    loadKnowledge()
  }
  next()
})
</script>

<style scoped>
.knowledge-manage {
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
