<template>
  <el-card shadow="hover" class="assistant-card">
    <template #header>
      <div class="assistant-header">
        <span>样本智能助手</span>
        <el-button type="primary" link @click="askQuestion">发送</el-button>
      </div>
    </template>

    <el-input
        v-model="question"
        type="textarea"
        :rows="3"
        placeholder="例如：最近7天新增了多少样本？未入库样本有哪些？异常样本有多少？哪个样本类型最多？"
        @keyup.enter.exact.prevent="askQuestion"
    />

    <div class="question-tags">
      <el-tag
          v-for="item in quickQuestions"
          :key="item"
          style="cursor: pointer; margin-right: 8px; margin-top: 8px"
          @click="useQuestion(item)"
      >
        {{ item }}
      </el-tag>
    </div>

    <el-skeleton :loading="loading" animated style="margin-top: 16px">
      <template #template>
        <el-skeleton-item variant="p" style="width: 100%; height: 60px" />
      </template>

      <template #default>
        <div v-if="answer" class="answer-box">
          <div class="answer-title">回答</div>
          <div class="answer-content">{{ answer }}</div>
        </div>

        <el-table
            v-if="relatedSamples.length"
            :data="relatedSamples"
            size="small"
            border
            style="margin-top: 16px"
            max-height="280"
        >
          <el-table-column prop="sampleNo" label="样本编号" min-width="120" />
          <el-table-column prop="sampleName" label="样本名称" min-width="140" />
          <el-table-column prop="source" label="样本来源" min-width="120" />
          <el-table-column prop="queue" label="样本队列" min-width="120" />
          <el-table-column prop="statusText" label="状态" width="100" />
        </el-table>
      </template>
    </el-skeleton>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { askSampleAssistant } from '@/api/recommendation'

const question = ref('')
const answer = ref('')
const relatedSamples = ref([])
const loading = ref(false)

const quickQuestions = [
  '最近7天新增了多少样本？',
  '未入库样本有哪些？',
  '异常样本有多少？',
  '哪个样本类型最多？',
  '哪个来源最多？',
  '哪个队列样本最多？'
]

const useQuestion = (q) => {
  question.value = q
  askQuestion()
}

const askQuestion = async () => {
  if (!question.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  loading.value = true
  try {
    const res = await askSampleAssistant(question.value.trim())
    answer.value = res.data?.answer || ''
    relatedSamples.value = (res.data?.relatedSamples || []).map(item => ({
      ...item,
      sampleName: item.sampleName || `样本-${item.sampleNo || item.id}`,
      statusText: item.status === 0 ? '待处理' : item.status === 1 ? '正常' : '异常'
    }))
  } catch (e) {
    answer.value = ''
    relatedSamples.value = []
    ElMessage.error('智能问答失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.assistant-card { width: 100%; }
.assistant-header { display: flex; justify-content: space-between; align-items: center; }
.question-tags { margin-top: 8px; }
.answer-box { margin-top: 16px; background: #f7f8fa; padding: 12px; border-radius: 8px; }
.answer-title { font-weight: 600; margin-bottom: 8px; }
.answer-content { color: #333; line-height: 1.7; white-space: pre-wrap; }
</style>
