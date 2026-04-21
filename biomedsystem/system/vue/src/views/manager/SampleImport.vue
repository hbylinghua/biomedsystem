<template>
  <div style="padding: 30px; background: #fff; min-height: 100vh;">
    <h2>样本批量导入</h2>

    <el-button
        type="success"
        @click="downloadTemplate"
        style="margin-right: 10px;"
    >
      下载Excel模板
    </el-button>

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

    <div style="margin-top: 20px; color: #666;">
      <p>1. 先下载模板，按格式填写</p >
      <p>2. 请勿修改表头</p >
      <p>3. 支持格式：xlsx、xls</p >
      <p>4. 样本类型ID、存储ID必须填写数据库中已存在的真实ID</p >
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
const uploadHeaders = {
  Authorization: `Bearer ${getToken()}`
}

// 下载模板
// 下载模板（带下拉选择框版本）
// 下载模板（带样本状态下拉选择框）
const downloadTemplate = () => {
  const data = [
    ['样本编号', '样本名称', '样本类型', '存储位置', '存储ID', '样本状态'],
    ['BIO20250001', '志愿者四', 12, '冷库二', 9, '正常']
  ]
  const ws = XLSX.utils.aoa_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '模板')

  // ===================== 给「样本状态」列添加下拉选择框 =====================
  // F 列（样本状态）从第 2 行到第 1000 行都可以下拉选择
  const statusRange = 'F2:F1000'
  const statusValidation = {
    type: 'list',
    formula1: '"正常,已使用,废弃"', // 下拉选项
    showDropDown: true,
    allowBlank: false
  }

  // 把下拉规则应用到工作表
  ws['!dataValidation'] = [{
    sqref: statusRange,
    ...statusValidation
  }]

  // 下载文件
  XLSX.writeFile(wb, '样本批量导入模板.xlsx')
  ElMessage.success('模板下载成功！')
}

const beforeUpload = (file) => {
  const isExcel =
      file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
      file.type === 'application/vnd.ms-excel'

  if (!isExcel) {
    ElMessage.error('只能上传Excel文件！')
    return false
  }
  return true
}

const handleSuccess = (res) => {
  if (res.code === '200') {
    ElMessage.success(res.msg || '导入成功！')
    router.push('/sample')
  } else {
    ElMessage.error(res.msg || '导入失败！')
  }
}

const handleError = (err) => {
  console.error('上传错误详情：', err)
  ElMessage.error('导入失败，请检查登录状态、后端服务或Excel内容')
}
</script>