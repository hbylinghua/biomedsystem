<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item>
          <el-input v-model="data.queryForm.operType" placeholder="请输入操作类型查询" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="handleAdd">新增日志</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="data.tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="sampleId" label="样本ID" width="100" />
        <el-table-column prop="operType" label="操作类型" min-width="120" />
        <el-table-column prop="operBy" label="操作人ID" min-width="120" />
        <el-table-column prop="content" label="操作内容" min-width="220" />
        <el-table-column prop="operTime" label="操作时间" min-width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button link type="danger" @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 16px; text-align: right">
        <el-pagination
            v-model:current-page="data.pageNum"
            v-model:page-size="data.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="data.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增日志" width="600px">
      <el-form ref="formRef" :model="logForm" :rules="rules" label-width="90px">
        <el-form-item label="样本ID" prop="sampleId">
          <el-input-number v-model="logForm.sampleId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="操作类型" prop="operType">
          <el-input v-model="logForm.operType" />
        </el-form-item>
        <el-form-item label="操作人ID" prop="operBy">
          <el-input-number v-model="logForm.operBy" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="操作内容" prop="content">
          <el-input v-model="logForm.content" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLog">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  queryForm: { operType: '' }
})

const logForm = reactive({ sampleId: null, operType: '', operBy: null, content: '' })
const rules = {
  sampleId: [{ required: true, message: '请输入样本ID', trigger: 'blur' }],
  operType: [{ required: true, message: '请输入操作类型', trigger: 'blur' }],
  operBy: [{ required: true, message: '请输入操作人ID', trigger: 'blur' }],
  content: [{ required: true, message: '请输入操作内容', trigger: 'blur' }]
}

const resetLogForm = () => { logForm.sampleId = null; logForm.operType = ''; logForm.operBy = null; logForm.content = '' }

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/biomedOperLog/selectPage', {
      params: { pageNum: data.pageNum, pageSize: data.pageSize, operType: data.queryForm.operType }
    })
    const pageData = res.data || {}
    data.tableData = pageData.list || []
    data.total = pageData.total || 0
  } catch (err) {
    data.tableData = []
    data.total = 0
    ElMessage.error('日志查询失败')
  } finally {
    loading.value = false
  }
}
const handleSearch = () => { data.pageNum = 1; load() }
const resetSearch = () => { data.queryForm.operType = ''; data.pageNum = 1; load() }
const handleAdd = () => { resetLogForm(); dialogVisible.value = true }
const submitLog = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await request.post('/biomedOperLog/add', logForm)
      ElMessage.success('日志新增成功')
      dialogVisible.value = false
      load()
    } catch {
      ElMessage.error('新增失败')
    }
  })
}
const del = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这条日志吗？', '提示', { type: 'warning' })
    await request.delete(`/biomedOperLog/delete/${id}`)
    ElMessage.success('日志删除成功')
    if (data.tableData.length === 1 && data.pageNum > 1) data.pageNum -= 1
    load()
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') ElMessage.error('删除失败')
  }
}
const handleSizeChange = (val) => { data.pageSize = val; data.pageNum = 1; load() }
const handleCurrentChange = (val) => { data.pageNum = val; load() }

onMounted(load)
</script>
