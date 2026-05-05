<template>
  <div class="import-page">
    <h2>样本批量导入</h2>

    <div class="action-row">
      <el-button type="success" @click="downloadTemplate">下载Excel模板</el-button>
      <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          accept=".xlsx,.xls"
          name="file"
          :show-file-list="false"
          :with-credentials="false"
          :on-success="handleSuccess"
          :on-error="handleError"
          :before-upload="beforeUpload"
      >
        <el-button type="primary">上传Excel文件</el-button>
      </el-upload>
    </div>

    <el-alert
        title="模板已经调整为与新增单条样本表单一致：样本名称可不填；样本类型可填写中文名称；存储位置可填写新位置，系统会自动创建存储记录。"
        type="info"
        show-icon
        :closable="false"
        style="margin: 18px 0"
    />

    <div class="tips">
      <p>1. 先下载模板，按模板表头填写，请勿修改表头。</p>
      <p>2. 样本来源/志愿者、样本队列、采集时间、样本类型为建议填写项；样本名称为空时系统会自动生成。</p>
      <p>3. 样本类型可以填写名称，例如“血液”；也可以填写已有样本类型ID。</p>
      <p>4. 存储位置可以填写已有位置，也可以填写新位置；如果数据库不存在该位置，导入时会自动创建存储记录。</p>
      <p>5. 样本状态支持：待处理、正常、异常；不填默认待处理。</p>
      <p>6. 支持格式：xlsx、xls。</p>
    </div>
  </div>
</template>

<script setup>
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getToken } from '@/utils/auth'

const router = useRouter()
const uploadUrl = `${import.meta.env.VITE_BASE_URL}/biomedSample/import`
const uploadHeaders = { Authorization: `Bearer ${getToken()}` }

const downloadTemplate = () => {
  const data = [
    ['样本编号', '样本名称', '样本来源/志愿者', '样本队列', '采集时间', '样本类型', '存储位置', '样本状态'],
    ['', '血液样本A', '志愿者一', '冷库一', '2026-04-07 15:20:17', '血液', '冷库A-01', '待处理'],
    ['', '', '志愿者二', '肿瘤队列', '2026-04-08 09:30:00', '组织', '液氮罐B-02', '正常']
  ]
  const ws = XLSX.utils.aoa_to_sheet(data)
  ws['!cols'] = [
    { wch: 22 }, { wch: 18 }, { wch: 18 }, { wch: 16 },
    { wch: 22 }, { wch: 14 }, { wch: 18 }, { wch: 12 }
  ]
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '样本导入模板')
  XLSX.writeFile(wb, '样本批量导入模板.xlsx')
  ElMessage.success('模板下载成功')
}

const beforeUpload = (file) => {
  const name = file.name || ''
  const isExcel = name.endsWith('.xlsx') || name.endsWith('.xls')
  if (!isExcel) {
    ElMessage.error('只能上传Excel文件')
    return false
  }
  return true
}

const handleSuccess = (res) => {
  if (res.code === '200') {
    ElMessage.success(res.data || res.msg || '导入成功')
    router.push('/sample')
  } else {
    ElMessage.error(res.msg || '导入失败')
  }
}

const handleError = (err) => {
  console.error('上传错误详情：', err)
  ElMessage.error('导入失败，请检查登录状态、后端服务或Excel内容')
}
</script>

<style scoped>
.import-page {
  padding: 30px;
  background: #fff;
  min-height: 100vh;
}
.action-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 18px;
}
.tips {
  margin-top: 20px;
  color: #606266;
  line-height: 1.9;
  font-size: 15px;
}
</style>
