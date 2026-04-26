<template>
  <el-card class="pending-recommend-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>智能推荐待处理样本</span>
        <el-button type="primary" link @click="loadData">刷新</el-button>
      </div>
    </template>

    <el-table
        :data="tableData"
        v-loading="loading"
        size="small"
        border
        height="320"
        empty-text="暂无推荐数据"
    >
      <el-table-column prop="sampleNo" label="样本编号" min-width="130" />
      <el-table-column prop="sampleName" label="样本名称" min-width="140" />
      <el-table-column prop="priorityScore" label="优先级" width="90" />
      <el-table-column prop="recommendAction" label="建议动作" width="120" />
      <el-table-column prop="reason" label="推荐原因" min-width="240" />
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingTaskRecommendation } from '@/api/recommendation'

const tableData = ref([])
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPendingTaskRecommendation({ limit: 5 })
    tableData.value = res.data || []
  } catch (err) {
    console.error('获取待处理样本推荐失败：', err)
    tableData.value = []
    ElMessage.error('获取推荐任务失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.pending-recommend-card {
  width: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
