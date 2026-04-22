<template>
  <el-dialog
      :model-value="modelValue"
      title="智能存储推荐"
      width="760px"
      @close="handleClose"
  >
    <div class="recommend-top-tip">
      <el-alert
          title="系统会根据样本类型、队列、来源和历史使用情况推荐更合适的存储位置"
          type="info"
          :closable="false"
          show-icon
      />
    </div>

    <div class="recommend-query-info">
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="样本类型ID">
          {{ sampleTypeId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="队列">
          {{ queueName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="来源">
          {{ source || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="recommend-action-bar">
      <el-button type="primary" @click="loadData" :loading="loading">
        刷新推荐
      </el-button>
    </div>

    <el-table
        :data="recommendList"
        v-loading="loading"
        border
        stripe
        empty-text="暂无推荐结果"
        style="width: 100%"
    >
      <el-table-column prop="position" label="推荐位置" min-width="180" />
      <el-table-column prop="temp" label="推荐温度" width="120" />
      <el-table-column prop="score" label="推荐分数" width="100" />

      <el-table-column label="推荐理由" min-width="260">
        <template #default="scope">
          <div class="reason-tags">
            <el-tag
                v-for="(item, index) in scope.row.reasons || []"
                :key="index"
                style="margin-right: 6px; margin-bottom: 6px"
            >
              {{ item }}
            </el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="100" fixed="right">
        <template #default="scope">
          <el-button type="success" link @click="handleChoose(scope.row)">
            采用
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getStorageRecommendation } from '@/api/recommendation'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  sampleTypeId: {
    type: [Number, String],
    default: null
  },
  queueName: {
    type: String,
    default: ''
  },
  source: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'choose'])

const loading = ref(false)
const recommendList = ref([])

const isSuccess = (res) => {
  return !!res && (res.code === 200 || res.code === '200')
}

const loadData = async () => {
  if (!props.sampleTypeId) {
    ElMessage.warning('请先选择样本类型后再进行智能推荐')
    recommendList.value = []
    return
  }

  loading.value = true
  try {
    const res = await getStorageRecommendation({
      sampleTypeId: props.sampleTypeId,
      queueName: props.queueName,
      source: props.source
    })

    if (isSuccess(res)) {
      recommendList.value = res.data || []
    } else {
      recommendList.value = []
      ElMessage.error(res.msg || '获取推荐失败')
    }
  } catch (err) {
    console.error('获取智能推荐失败：', err)
    recommendList.value = []
    ElMessage.error('获取推荐失败')
  } finally {
    loading.value = false
  }
}

const handleChoose = (row) => {
  emit('choose', row)
  emit('update:modelValue', false)
  ElMessage.success(`已采用推荐位置：${row.position}`)
}

const handleClose = () => {
  emit('update:modelValue', false)
}

watch(
    () => props.modelValue,
    (val) => {
      if (val) {
        loadData()
      }
    }
)
</script>

<style scoped>
.recommend-top-tip {
  margin-bottom: 16px;
}

.recommend-query-info {
  margin-bottom: 16px;
}

.recommend-action-bar {
  margin-bottom: 16px;
  text-align: right;
}

.reason-tags {
  display: flex;
  flex-wrap: wrap;
}
</style>