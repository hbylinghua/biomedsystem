<template>
  <div class="sample-page">
    <div class="page-title">生物样本信息管理</div>

    <div class="card" style="margin-bottom: 5px;">
      <el-input
          v-model="data.name"
          style="width: 300px; margin-right: 10px"
          placeholder="请输入样本名称查询"
          clearable
          @keyup.enter="load"
      />
      <el-button type="primary" @click="goToImport">批量导入</el-button>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
      <el-button type="success" @click="exportSample">批量导出</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增样本</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
      </div>

      <el-table
          :data="data.tableData"
          stripe
          v-loading="loading"
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="样本名称" prop="sampleName" min-width="180">
          <template #default="scope">
            <el-link type="primary" @click="goDetail(scope.row.id)">
              {{ scope.row.sampleName || `样本-${scope.row.sampleNo || scope.row.id}` }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="样本来源/志愿者" prop="source" min-width="160" />
        <el-table-column label="样本队列" prop="queue" min-width="120" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="样本类型" min-width="120">
          <template #default="scope">{{ getTypeName(scope.row.sampleTypeId) }}</template>
        </el-table-column>
        <el-table-column label="存储位置" min-width="160">
          <template #default="scope">{{ getStoragePosition(scope.row.storageId) }}</template>
        </el-table-column>
        <el-table-column label="样本编号" prop="sampleNo" width="150" />
        <el-table-column label="采集时间" prop="collectTime" width="180">
          <template #default="scope">{{ formatTime(scope.row.collectTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" header-align="center" width="160">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <el-pagination
          background
          layout="prev, pager, next, jumper, ->, total"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @size-change="load"
          @current-change="load"
          style="text-align: right; padding: 10px 0"
      />
    </div>

    <el-dialog
        title="生物样本信息"
        width="55%"
        v-model="data.formVisible"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-form
          :model="data.form"
          label-width="120px"
          style="padding-right: 20px"
          :rules="formRules"
          ref="formRef"
      >
        <el-form-item label="样本名称" prop="sampleName">
          <el-input v-model="data.form.sampleName" autocomplete="off" placeholder="请输入样本名称" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="样本来源/志愿者" prop="source">
          <el-input v-model="data.form.source" autocomplete="off" placeholder="请输入样本来源或志愿者姓名" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="样本编号" prop="sampleNo">
          <el-input v-model="data.form.sampleNo" disabled placeholder="保存后自动生成" />
        </el-form-item>

        <el-form-item label="样本队列" prop="queue">
          <el-input v-model="data.form.queue" autocomplete="off" placeholder="请输入样本队列（如：肿瘤队列）" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="采集时间" prop="collectTime">
          <el-date-picker
              v-model="data.form.collectTime"
              type="datetime"
              placeholder="请选择采集时间"
              style="width: 100%"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="样本类型" prop="sampleTypeId">
          <el-select v-model="data.form.sampleTypeId" placeholder="请选择样本类型" style="width: 100%">
            <el-option v-for="item in categoryList" :key="item.id" :label="item.typeName" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="存储记录" prop="storageId">
          <div class="storage-select-row">
            <el-select v-model="data.form.storageId" placeholder="请选择存储记录" clearable style="width: 100%">
              <el-option v-for="item in storageList" :key="item.id" :label="`${item.position}（${item.temp || '未知温度'}）`" :value="item.id" />
            </el-select>
            <el-button type="primary" plain @click="openRecommendDialog">智能推荐</el-button>
          </div>
          <div class="storage-tip">
            当前选择：{{ selectedStoragePosition }}
          </div>
        </el-form-item>

        <el-form-item label="样本状态" prop="status">
          <el-select v-model="data.form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待处理" :value="0" />
            <el-option label="正常" :value="1" />
            <el-option label="异常" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <StorageRecommendDialog
        v-model="recommendDialogVisible"
        :sample-type-id="data.form.sampleTypeId"
        :queue-name="data.form.queue"
        :source="data.form.source"
        @choose="handleChooseRecommendation"
    />
  </div>
</template>

<script setup>
import request from '@/utils/request'
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { useRouter, useRoute } from 'vue-router'
import { getToken } from '@/utils/auth'
import StorageRecommendDialog from './StorageRecommendDialog.vue'

const loading = ref(false)
const formRef = ref(null)
const categoryList = ref([])
const storageList = ref([])
const selectedIds = ref([])
const recommendDialogVisible = ref(false)

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {
    id: null,
    sampleName: '',
    source: '',
    sampleNo: '',
    queue: '',
    collectTime: '',
    sampleTypeId: null,
    storageId: null,
    status: 0
  },
  tableData: [],
  name: null
})

const formRules = {
  sampleName: [{ required: true, message: '请输入样本名称', trigger: 'blur' }],
  source: [{ required: true, message: '请输入样本来源/志愿者', trigger: 'blur' }],
  queue: [{ required: true, message: '请输入样本队列', trigger: 'blur' }],
  sampleTypeId: [{ required: true, message: '请选择样本类型', trigger: 'change' }],
  collectTime: [{ required: true, message: '请选择采集时间', trigger: 'change' }]
}

const router = useRouter()
const route = useRoute()

const selectedStoragePosition = computed(() => {
  if (!data.form.storageId) return '未入库'
  const item = storageList.value.find(s => s.id === data.form.storageId)
  return item ? `${item.position}（${item.temp || '未知温度'}）` : `存储记录#${data.form.storageId}`
})

const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
const getStatusText = (status) => status === 0 ? '待处理' : status === 1 ? '正常' : status === 2 ? '异常' : '其他'
const getStatusTag = (status) => status === 0 ? 'warning' : status === 1 ? 'success' : status === 2 ? 'danger' : 'info'
const getTypeName = (sampleTypeId) => {
  if (!sampleTypeId) return '未分类'
  const typeItem = categoryList.value.find(item => item.id === sampleTypeId)
  return typeItem ? typeItem.typeName : '未分类'
}
const getStoragePosition = (storageId) => {
  if (!storageId) return '未入库'
  const storageItem = storageList.value.find(item => item.id === storageId)
  return storageItem ? storageItem.position : `存储记录#${storageId}`
}

const getCategoryList = async () => {
  try {
    const res = await request.get('/sampleType/selectAll')
    categoryList.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    categoryList.value = []
  }
}

const getStorageList = async () => {
  try {
    const res = await request.get('/biomedStorage/selectAll')
    storageList.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    storageList.value = []
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/biomedSample/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        sampleName: data.name
      }
    })
    const pageData = res.data || {}
    data.tableData = pageData.list || []
    data.total = pageData.total || 0
  } catch (err) {
    data.tableData = []
    data.total = 0
  } finally {
    loading.value = false
  }
}

const resetFormData = () => {
  data.form = {
    id: null,
    sampleName: '',
    source: '',
    sampleNo: '',
    queue: '',
    collectTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    sampleTypeId: null,
    storageId: null,
    status: 0
  }
}

const handleAdd = async () => {
  resetFormData()
  await Promise.all([getCategoryList(), getStorageList()])
  data.formVisible = true
}

const goToImport = () => router.push('/sampleImport')
const goDetail = (id) => router.push(`/sampleDetail/${id}`)

const exportSample = async () => {
  try {
    const blobData = await request({
      url: '/biomedSample/export',
      method: 'get',
      responseType: 'blob',
      headers: { Authorization: `Bearer ${getToken() || ''}` }
    })
    const blob = new Blob([blobData], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `样本列表_${dayjs().format('YYYY-MM-DD')}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

const fillForm = (row) => {
  data.form = {
    id: row.id,
    sampleName: row.sampleName || '',
    source: row.source || '',
    sampleNo: row.sampleNo || '',
    queue: row.queue || '',
    collectTime: row.collectTime ? dayjs(row.collectTime).format('YYYY-MM-DD HH:mm:ss') : '',
    sampleTypeId: row.sampleTypeId || null,
    storageId: row.storageId || null,
    status: row.status ?? 0
  }
}

const handleEdit = async (row) => {
  await Promise.all([getCategoryList(), getStorageList()])
  fillForm(row)
  data.formVisible = true
}

const save = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善必填字段')
      return
    }
    const submitData = {
      id: data.form.id || null,
      sampleName: data.form.sampleName || '',
      source: data.form.source || '',
      sampleNo: data.form.sampleNo || '',
      queue: data.form.queue || '',
      collectTime: data.form.collectTime,
      sampleTypeId: data.form.sampleTypeId,
      storageId: data.form.storageId || null,
      status: data.form.status ?? 0
    }
    try {
      if (data.form.id) {
        await request.put('/biomedSample/update', submitData)
      } else {
        await request.post('/biomedSample/add', submitData)
      }
      ElMessage.success('保存成功')
      data.formVisible = false
      load()
    } catch {
      ElMessage.error('保存失败')
    }
  })
}

const handleDelete = (id) => {
  if (!id) return
  ElMessageBox.confirm('删除后样本数据无法恢复，您确定删除吗？', '样本删除确认', { type: 'warning' })
      .then(async () => {
        await request.delete(`/biomedSample/delete/${id}`)
        ElMessage.success('删除成功')
        load()
      })
      .catch(() => {})
}

const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的样本')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个样本吗？删除后无法恢复！`, '批量删除确认', { type: 'warning' })
      .then(async () => {
        await Promise.all(selectedIds.value.map(id => request.delete(`/biomedSample/delete/${id}`)))
        ElMessage.success('批量删除成功')
        selectedIds.value = []
        load()
      })
      .catch(() => {})
}

const handleSelectionChange = (val) => { selectedIds.value = val.map(item => item.id) }
const openRecommendDialog = () => {
  if (!data.form.sampleTypeId) {
    ElMessage.warning('请先选择样本类型')
    return
  }
  recommendDialogVisible.value = true
}
const handleChooseRecommendation = (row) => {
  data.form.storageId = row.storageId
  ElMessage.success(`已自动回填存储记录：${row.position}`)
}
const reset = () => { data.name = null; data.pageNum = 1; load() }

onMounted(async () => {
  await Promise.all([getCategoryList(), getStorageList(), load()])
  if (route.query.editId) {
    try {
      const res = await request.get(`/biomedSample/selectById/${route.query.editId}`)
      fillForm(res.data || {})
      data.formVisible = true
      router.replace('/sample')
    } catch (e) {
      console.error(e)
    }
  }
})
</script>

<style scoped>
.sample-page { padding: 15px; background-color: #f5f7fa; min-height: calc(100vh - 20px); }
.page-title { font-size: 18px; font-weight: 600; margin-bottom: 15px; color: #1989fa; }
.card { background: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.dialog-footer { text-align: right; }
.el-table .el-table__cell { padding: 10px 0; }
.storage-select-row { display: flex; align-items: center; gap: 12px; }
.storage-select-row :deep(.el-select) { flex: 1; }
.storage-tip { color: #909399; font-size: 12px; margin-top: 6px; }
</style>
