<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>样本操作日志</span>
          <el-tag type="info">日志由样本录入、修改、删除、导入等业务自动生成，不支持手动添加</el-tag>
        </div>
      </template>

      <el-form :inline="true">
        <el-form-item>
          <el-input v-model="data.queryForm.operType" placeholder="请输入操作类型查询" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-input-number v-model="data.queryForm.sampleId" placeholder="样本ID" :min="1" controls-position="right" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="data.tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="sampleId" label="样本ID" width="100" />
        <el-table-column prop="operType" label="操作类型" min-width="120" />
        <el-table-column prop="operBy" label="操作人ID" min-width="120" />
        <el-table-column prop="content" label="操作内容" min-width="260" />
        <el-table-column prop="operTime" label="操作时间" min-width="180" />
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
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  queryForm: { operType: '', sampleId: null }
})

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/biomedOperLog/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        operType: data.queryForm.operType,
        sampleId: data.queryForm.sampleId
      }
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
const resetSearch = () => { data.queryForm.operType = ''; data.queryForm.sampleId = null; data.pageNum = 1; load() }
const handleSizeChange = (val) => { data.pageSize = val; data.pageNum = 1; load() }
const handleCurrentChange = (val) => { data.pageNum = val; load() }

onMounted(load)
</script>

<style scoped>
.page-container { padding: 15px; }
.header-row { display: flex; align-items: center; justify-content: space-between; }
</style>
