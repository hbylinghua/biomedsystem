<template>
  <div class="detail-page">
    <div class="back-bar">
      <el-button @click="$router.go(-1)">← 返回列表</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :span="16">
        <div class="card-wrapper">
          <div class="card-title">样本详细信息</div>

          <div class="info-grid">
            <div class="info-row"><div class="label">样本编号：</div><div class="value">{{ detail.sampleNo || '-' }}</div></div>
            <div class="info-row"><div class="label">样本名称：</div><div class="value">{{ detail.sampleName || fallbackName }}</div></div>
            <div class="info-row"><div class="label">样本来源/志愿者：</div><div class="value">{{ detail.source || '-' }}</div></div>
            <div class="info-row"><div class="label">样本类型：</div><div class="value">{{ getTypeName(detail.sampleTypeId) }}</div></div>
            <div class="info-row"><div class="label">样本状态：</div><div class="value">{{ getStatusText(detail.status) }}</div></div>
            <div class="info-row"><div class="label">样本队列：</div><div class="value">{{ detail.queue || '-' }}</div></div>
            <div class="info-row"><div class="label">存储位置：</div><div class="value">{{ storageDetail.position || '未入库' }}</div></div>
            <div class="info-row"><div class="label">存储温度：</div><div class="value">{{ storageDetail.temp || '-' }}</div></div>
            <div class="info-row"><div class="label">存储状态：</div><div class="value">{{ storageDetail.status || '-' }}</div></div>
            <div class="info-row"><div class="label">存储ID：</div><div class="value">{{ detail.storageId || '无' }}</div></div>
            <div class="info-row"><div class="label">采集时间：</div><div class="value">{{ formatTime(detail.collectTime) }}</div></div>
            <div class="info-row"><div class="label">创建时间：</div><div class="value">{{ formatTime(detail.createTime) }}</div></div>
            <div class="info-row"><div class="label">更新时间：</div><div class="value">{{ formatTime(detail.updateTime) }}</div></div>
            <div class="info-row"><div class="label">创建人ID：</div><div class="value">{{ detail.createBy || '-' }}</div></div>
          </div>
        </div>

        <div class="card-wrapper" style="margin-top: 16px;">
          <div class="card-title">操作日志</div>
          <el-table :data="logList" border size="small">
            <el-table-column prop="operType" label="操作类型" width="120" />
            <el-table-column prop="operBy" label="操作人ID" width="100" />
            <el-table-column prop="content" label="操作内容" min-width="220" />
            <el-table-column prop="operTime" label="操作时间" min-width="180">
              <template #default="scope">{{ formatTime(scope.row.operTime) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="card-wrapper">
          <div class="card-title">操作</div>
          <div class="action-group vertical">
            <el-button type="primary" @click="toEdit">编辑样本</el-button>
            <el-button type="danger" @click="deleteSample">删除样本</el-button>
            <el-button @click="$router.go(-1)">取消返回</el-button>
          </div>
        </div>

        <div class="card-wrapper" style="margin-top: 16px;">
          <div class="card-title">当前判断</div>
          <el-alert
              :title="detail.storageId ? '该样本已绑定存储记录' : '该样本尚未入库，给它绑定 storageId 后首页“未入库”预警会消失'"
              :type="detail.storageId ? 'success' : 'warning'"
              :closable="false"
              show-icon
          />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const detail = ref({})
const storageDetail = ref({})
const typeList = ref([])
const logList = ref([])
const fallbackName = computed(() => detail.value?.sampleNo ? `样本-${detail.value.sampleNo}` : '-')

const getTypeList = async () => {
  const res = await request.get('/sampleType/selectAll')
  typeList.value = res.data || []
}

const getDetail = async () => {
  const res = await request.get('/biomedSample/selectById/' + id)
  detail.value = res.data || {}
  if (detail.value.storageId) {
    try {
      const storageRes = await request.get('/biomedStorage/selectById/' + detail.value.storageId)
      storageDetail.value = storageRes.data || {}
    } catch {
      storageDetail.value = {}
    }
  } else {
    storageDetail.value = {}
  }
}

const getLogs = async () => {
  try {
    const res = await request.get('/biomedOperLog/selectPage', {
      params: { pageNum: 1, pageSize: 20, sampleId: id }
    })
    logList.value = res.data?.list || []
  } catch {
    logList.value = []
  }
}

const getTypeName = (typeId) => {
  const item = typeList.value.find(i => i.id === typeId)
  return item ? item.typeName : '未知'
}
const getStatusText = (status) => status === 0 ? '待处理' : status === 1 ? '正常' : status === 2 ? '异常' : '其他'
const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '无'
const toEdit = () => router.push({ path: '/sample', query: { editId: id } })

const deleteSample = async () => {
  await ElMessageBox.confirm('确认删除该样本？', '提示', { type: 'warning' })
  await request.delete('/biomedSample/delete/' + id)
  ElMessage.success('删除成功')
  router.push('/sample')
}

onMounted(async () => {
  await getTypeList()
  await getDetail()
  await getLogs()
})
</script>

<style scoped>
.detail-page { padding: 20px; background: #f5f7fa; min-height: 100vh; }
.back-bar { margin-bottom: 15px; }
.card-wrapper { background: #fff; border-radius: 10px; padding: 25px; box-shadow: 0 2px 10px #00000008; }
.card-title { font-size: 16px; font-weight: bold; margin-bottom: 20px; color: #333; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 24px; }
.info-row { display: flex; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.label { width: 120px; font-weight: 600; color: #666; }
.value { color: #333; flex: 1; }
.action-group { display: flex; gap: 10px; }
.action-group.vertical { flex-direction: column; }
</style>
