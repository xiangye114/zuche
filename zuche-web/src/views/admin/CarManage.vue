<template>
  <div class="car-manage">
    <div class="page-header">
      <h2>车辆管理</h2>
      <div class="header-buttons">
        <el-button type="warning" @click="handleSyncStatus" :loading="syncLoading">
          <el-icon><Refresh /></el-icon>同步状态
        </el-button>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>添加车辆
        </el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-bar">
      <el-input
        v-model="filter.keyword"
        placeholder="搜索车辆名称"
        clearable
        style="width: 200px"
        @change="handleFilterChange"
      />
      <el-select
        v-model="filter.storeId"
        placeholder="选择门店"
        clearable
        style="width: 180px"
        @change="handleFilterChange"
      >
        <el-option
          v-for="store in stores"
          :key="store.id"
          :label="store.name"
          :value="store.id"
        />
      </el-select>
      <el-select
        v-model="filter.brandId"
        placeholder="选择品牌"
        clearable
        style="width: 150px"
        @change="handleFilterChange"
      >
        <el-option
          v-for="brand in brands"
          :key="brand.id"
          :label="brand.name"
          :value="brand.id"
        />
      </el-select>
      <el-select
        v-model="filter.status"
        placeholder="车辆状态"
        clearable
        style="width: 130px"
        @change="handleFilterChange"
      >
        <el-option label="维修中" :value="0" />
        <el-option label="可租" :value="1" />
        <el-option label="已租" :value="2" />
      </el-select>
    </div>

    <!-- 批量操作栏 -->
    <div class="batch-operation-bar" v-if="selectedCars.length > 0">
      <span class="selected-count">已选择 {{ selectedCars.length }} 项</span>
      <el-button type="success" size="small" @click="handleBatchSetAvailable" :loading="batchLoading">
        批量设为可租
      </el-button>
      <el-button type="warning" size="small" @click="handleBatchSetRepair" :loading="batchLoading">
        批量设为维修
      </el-button>
      <el-button type="danger" size="small" @click="handleBatchDelete" :loading="batchLoading" v-if="hasDeletePermission()">
        批量删除
      </el-button>
      <el-button size="small" @click="clearSelection">取消选择</el-button>
    </div>

    <!-- 车辆列表 -->
    <el-table 
      :data="cars" 
      style="width: 100%" 
      v-loading="loading"
      @selection-change="handleSelectionChange"
      ref="tableRef"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="车辆ID" width="80" align="center">
        <template #default="scope">
          <span class="car-id">{{ scope.row.car?.id || scope.row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column label="车辆信息" min-width="280">
        <template #default="scope">
          <div class="car-info-cell">
            <el-image
              :src="(scope.row.car?.image || scope.row.image) || defaultCarImage"
              fit="cover"
              style="width: 80px; height: 60px; border-radius: 8px"
            />
            <div class="car-basic-info">
              <div class="car-name">{{ scope.row.car?.name || scope.row.name }}</div>
              <div class="car-tags">
                <el-tag size="small" type="info">{{ getTransmissionText(scope.row.car?.transmission || scope.row.transmission) }}</el-tag>
                <el-tag size="small" type="info">{{ scope.row.car?.seats || scope.row.seats }}座</el-tag>
                <el-tag size="small" type="info">{{ scope.row.car?.fuelType || scope.row.fuelType }}</el-tag>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="所属门店" min-width="180">
        <template #default="scope">
          <div class="store-info">
            <el-icon><OfficeBuilding /></el-icon>
            <span>{{ getStoreName(scope.row.car?.storeId || scope.row.storeId) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="品牌/车型" width="150">
        <template #default="scope">
          <div>{{ getBrandName(scope.row.car?.brandId || scope.row.brandId) }}</div>
          <div class="text-gray">{{ getCategoryName(scope.row.car?.categoryId || scope.row.categoryId) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="150">
        <template #default="scope">
          <div class="price-info">
            <div class="day-price">¥{{ scope.row.car?.pricePerDay || scope.row.pricePerDay }}<span class="unit">/天</span></div>
            <div class="deposit" v-if="scope.row.car?.deposit || scope.row.deposit">押金 ¥{{ scope.row.car?.deposit || scope.row.deposit }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="车辆状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.car?.status || scope.row.status)">
            {{ getStatusText(scope.row.car?.status || scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button 
            size="small" 
            type="primary" 
            @click="handleEdit(scope.row)"
            :disabled="isCarRented(scope.row)"
          >编辑</el-button>
          <el-button 
            size="small" 
            type="danger" 
            @click="handleDelete(scope.row)" 
            v-if="hasDeletePermission()"
            :disabled="isCarRented(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper"
        @current-change="loadCars"
      />
    </div>

    <!-- 添加/编辑车辆对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑车辆' : '添加车辆'"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="car-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="车辆名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入车辆名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属门店" prop="storeId">
              <el-select v-model="form.storeId" placeholder="选择所属门店" style="width: 100%">
                <el-option
                  v-for="store in stores"
                  :key="store.id"
                  :label="store.name"
                  :value="store.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brandId">
              <el-select v-model="form.brandId" placeholder="选择品牌" style="width: 100%">
                <el-option
                  v-for="brand in brands"
                  :key="brand.id"
                  :label="brand.name"
                  :value="brand.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="车型" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择车型" style="width: 100%">
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="日租金" prop="pricePerDay">
              <el-input-number v-model="form.pricePerDay" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="押金" prop="deposit">
              <el-input-number v-model="form.deposit" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="颜色" prop="color">
              <el-input v-model="form.color" placeholder="如：白色" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="座位数" prop="seats">
              <el-input-number v-model="form.seats" :min="1" :max="50" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="变速箱" prop="transmission">
              <el-select v-model="form.transmission" placeholder="选择变速箱" style="width: 100%">
                <el-option label="手动" value="手动" />
                <el-option label="自动" value="自动" />
                <el-option label="CVT" value="CVT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="燃油类型" prop="fuelType">
              <el-select v-model="form.fuelType" placeholder="选择燃油类型" style="width: 100%">
                <el-option label="汽油" value="汽油" />
                <el-option label="柴油" value="柴油" />
                <el-option label="电动" value="电动" />
                <el-option label="混动" value="混动" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="车辆状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">维修中</el-radio>
            <el-radio :label="1">可租</el-radio>
            <el-radio :label="2">已租</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="车辆图片" prop="image">
          <el-input v-model="form.image" placeholder="请输入图片URL" />
        </el-form-item>

        <el-form-item label="车辆描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入车辆描述"
          />
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
import { Plus, OfficeBuilding, Refresh } from '@element-plus/icons-vue'
import { hasDeletePermission } from '@/utils/permission'

const route = useRoute()

const loading = ref(false)
const submitLoading = ref(false)
const batchLoading = ref(false)
const syncLoading = ref(false)
const cars = ref([])
const stores = ref([])
const brands = ref([])
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const defaultCarImage = 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=400&h=300&fit=crop'
const selectedCars = ref([])
const tableRef = ref(null)

const filter = reactive({
  keyword: '',
  storeId: null,
  brandId: null,
  status: null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  storeId: null,
  brandId: null,
  categoryId: null,
  color: '',
  pricePerDay: 0,
  deposit: 0,
  seats: 5,
  transmission: '',
  fuelType: '',
  image: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入车辆名称', trigger: 'blur' }],
  storeId: [{ required: true, message: '请选择所属门店', trigger: 'change' }],
  brandId: [{ required: true, message: '请选择品牌', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择车型', trigger: 'change' }],
  pricePerDay: [{ required: true, message: '请输入日租金', trigger: 'blur' }],
  transmission: [{ required: true, message: '请选择变速箱', trigger: 'change' }],
  fuelType: [{ required: true, message: '请选择燃油类型', trigger: 'change' }]
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '维修中', 1: '可租', 2: '已租' }
  return texts[status] || '未知'
}

const isCarRented = (row) => {
  const status = row.car?.status ?? row.status
  return status === 2
}

const getTransmissionText = (transmission) => {
  return transmission || '自动'
}

const getStoreName = (storeId) => {
  const store = stores.value.find(s => s.id === storeId)
  return store ? store.name : '未知门店'
}

const getBrandName = (brandId) => {
  const brand = brands.value.find(b => b.id === brandId)
  return brand ? brand.name : '未知品牌'
}

const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : '未知车型'
}

const handleSyncStatus = async () => {
  syncLoading.value = true
  try {
    const result = await adminApi.syncCarStatus()
    ElMessage.success(`同步完成：${result.updatedToRented || 0} 辆已设为已租，${result.updatedToAvailable || 0} 辆已设为可租`)
    loadCars()
  } catch (error) {
    console.error('同步失败:', error)
    ElMessage.error('同步失败')
  } finally {
    syncLoading.value = false
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadCars()
}

const loadCars = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: filter.keyword || undefined,
      storeId: filter.storeId || undefined,
      brandId: filter.brandId || undefined,
      status: filter.status !== null ? filter.status : undefined
    }
    const data = await adminApi.getCars(params)
    cars.value = data?.records || data || []
    total.value = data?.total || 0
  } catch (error) {
    console.error('加载车辆列表失败:', error)
    ElMessage.error('加载车辆列表失败')
  } finally {
    loading.value = false
  }
}

const loadStores = async () => {
  try {
    const data = await adminApi.getStores()
    stores.value = data?.records || data || []
  } catch (error) {
    console.error('加载门店列表失败:', error)
  }
}

const loadBrands = async () => {
  try {
    const data = await adminApi.getBrands()
    brands.value = data || []
  } catch (error) {
    console.error('加载品牌列表失败:', error)
  }
}

const loadCategories = async () => {
  try {
    const data = await adminApi.getCategories()
    categories.value = data || []
  } catch (error) {
    console.error('加载车型列表失败:', error)
  }
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.storeId = null
  form.brandId = null
  form.categoryId = null
  form.color = ''
  form.pricePerDay = 0
  form.deposit = 0
  form.seats = 5
  form.transmission = ''
  form.fuelType = ''
  form.image = ''
  form.description = ''
  form.status = 1
}

const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  const carData = row.car || row
  Object.assign(form, carData)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await adminApi.updateCar(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await adminApi.addCar(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadCars()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  const carData = row.car || row
  try {
    await ElMessageBox.confirm(`确定要删除车辆"${carData.name}"吗？`, '提示', { type: 'warning' })
    await adminApi.deleteCar(carData.id)
    ElMessage.success('删除成功')
    loadCars()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedCars.value = selection
}

// 清除选择
const clearSelection = () => {
  tableRef.value?.clearSelection()
  selectedCars.value = []
}

// 批量设为可租
const handleBatchSetAvailable = async () => {
  await handleBatchUpdateStatus(1, '设为可租')
}

// 批量设为维修
const handleBatchSetRepair = async () => {
  await handleBatchUpdateStatus(0, '设为维修')
}

// 批量更新状态
const handleBatchUpdateStatus = async (status, actionName) => {
  if (selectedCars.value.length === 0) {
    ElMessage.warning('请先选择车辆')
    return
  }
  
  const hasRentedCar = selectedCars.value.some(row => {
    const carStatus = row.car?.status ?? row.status
    return carStatus === 2
  })
  
  if (hasRentedCar) {
    ElMessage.warning('部分车辆已被租用，无法批量操作')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要将选中的 ${selectedCars.value.length} 辆车${actionName}吗？`,
      '提示',
      { type: 'warning' }
    )
    
    batchLoading.value = true
    const ids = selectedCars.value.map(row => {
      const car = row.car || row
      return car.id
    })
    await adminApi.batchUpdateCarStatus({ ids, status })
    ElMessage.success(`批量${actionName}成功`)
    clearSelection()
    loadCars()
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
  if (selectedCars.value.length === 0) {
    ElMessage.warning('请先选择车辆')
    return
  }
  
  const hasRentedCar = selectedCars.value.some(row => {
    const carStatus = row.car?.status ?? row.status
    return carStatus === 2
  })
  
  if (hasRentedCar) {
    ElMessage.warning('部分车辆已被租用，无法批量删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCars.value.length} 辆车吗？此操作不可恢复！`,
      '警告',
      { type: 'error' }
    )
    
    batchLoading.value = true
    const ids = selectedCars.value.map(row => {
      const car = row.car || row
      return car.id
    })
    await adminApi.batchDeleteCars({ ids })
    ElMessage.success('批量删除成功')
    clearSelection()
    loadCars()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    batchLoading.value = false
  }
}

const loadAllData = () => {
  loadCars()
  loadStores()
  loadBrands()
  loadCategories()
}

onMounted(() => {
  loadAllData()
})

onBeforeRouteUpdate((to, from, next) => {
  if (to.path === '/admin/cars') {
    loadAllData()
  }
  next()
})
</script>

<style scoped>
.car-manage {
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

.header-buttons {
  display: flex;
  gap: 10px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
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

.car-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.car-basic-info {
  flex: 1;
}

.car-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
}

.car-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.store-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}

.store-info .el-icon {
  color: #409eff;
}

.text-gray {
  color: #909399;
  font-size: 13px;
}

.car-id {
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
}

.price-info {
  text-align: center;
}

.day-price {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.day-price .unit {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.deposit {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.car-form {
  padding: 10px 0;
}
</style>
