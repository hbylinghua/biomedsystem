<template>
  <div class="detail-page">
    <!-- 顶部返回 -->
    <div class="back-bar">
      <el-button @click="$router.go(-1)">← 返回列表</el-button>
    </div>

    <!-- 样本信息卡片 -->
    <div class="card-wrapper">
      <div class="card-title">📋 样本基础信息</div>
      <div class="info-row">
        <div class="label">样本编号：</div>
        <div class="value">{{ detail.sampleNo }}</div>
      </div>
      <div class="info-row">
        <div class="label">样本名称：</div>
        <div class="value">{{ detail.source }}</div>
      </div>
      <div class="info-row">
        <div class="label">样本类型：</div>
        <div class="value">{{ getTypeName(detail.sampleTypeId) }}</div>
      </div>
      <div class="info-row">
        <div class="label">存储位置：</div>
        <div class="value">{{ detail.queue }}</div>
      </div>
      <div class="info-row">
        <div class="label">采集时间：</div>
        <div class="value">{{ formatTime(detail.collectTime) }}</div>
      </div>
      <div class="info-row">
        <div class="label">存储ID：</div>
        <div class="value">{{ detail.storageId || '无' }}</div>
      </div>
      <div class="info-row">
        <div class="label">创建时间：</div>
        <div class="value">{{ formatTime(detail.createTime) }}</div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-group">
      <el-button type="primary" @click="toEdit">编辑样本</el-button>
      <el-button type="danger" @click="deleteSample">删除样本</el-button>
      <el-button @click="$router.go(-1)">取消返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const detail = ref({})

// 样本类型
const typeList = ref([])
const getTypeList = () => {
  request.get('/sampleType/selectAll').then(res => {
    if (res.code === '200') typeList.value = res.data
  })
}

// 获取详情
const getDetail = () => {
  request.get('/biomedSample/selectById/' + id).then(res => {
    if (res.code === '200') detail.value = res.data
  })
}

// 类型名称
const getTypeName = (id) => {
  const item = typeList.value.find(i => i.id === id)
  return item ? item.typeName : '未知'
}

// 时间格式化
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '无'
}

// 编辑
const toEdit = () => {
  router.push(`/sampleEdit/${id}`)
}

// 删除
const deleteSample = async () => {
  await ElMessageBox.confirm('确认删除该样本？', '提示', { type: 'warning' })
  request.delete('/biomedSample/delete/' + id).then(res => {
    if (res.code === 200) {
      ElMessage.success('删除成功')
      router.push('/sample')
    }
  })
}

onMounted(() => {
  getTypeList()
  getDetail()
})
</script>

<style scoped>
.detail-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}
.back-bar {
  margin-bottom: 15px;
}
.card-wrapper {
  background: #fff;
  border-radius: 10px;
  padding: 25px;
  box-shadow: 0 2px 10px #00000008;
}
.card-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}
.info-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.label {
  width: 120px;
  font-weight: 600;
  color: #666;
}
.value {
  color: #333;
}
.action-group {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}
</style>