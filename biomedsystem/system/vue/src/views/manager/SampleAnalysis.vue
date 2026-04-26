<template>
  <div class="analysis-page">
    <div class="page-title">样本详细分析</div>

    <div class="top-grid">
      <el-card class="filter-card" shadow="hover">
        <template #header>分析条件筛选</template>
        <el-form :model="filterForm" label-width="90px">
          <el-form-item label="时间范围">
            <el-date-picker
                v-model="filterForm.timeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="样本队列">
            <el-select v-model="filterForm.queueName" placeholder="请选择队列" clearable style="width: 100%">
              <el-option v-for="item in queueList" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="样本类型">
            <el-select v-model="filterForm.sampleTypeId" placeholder="请选择类型" clearable style="width: 100%">
              <el-option v-for="item in sampleTypeList" :key="item.id" :label="item.typeName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="queryAnalysisData" style="width: 100%">执行分析</el-button>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetFilter" style="width: 100%">重置条件</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <div class="metric-grid">
        <el-card><div class="metric-title">样本总量</div><div class="metric-value">{{ metrics.total }}</div></el-card>
        <el-card><div class="metric-title">待处理</div><div class="metric-value warn">{{ metrics.pending }}</div></el-card>
        <el-card><div class="metric-title">正常</div><div class="metric-value success">{{ metrics.normal }}</div></el-card>
        <el-card><div class="metric-title">异常</div><div class="metric-value danger">{{ metrics.abnormal }}</div></el-card>
      </div>
    </div>

    <el-card class="chart-card" shadow="hover">
      <template #header>
        <div class="chart-header">
          <span>详细分析图表（点击图表可钻取明细）</span>
          <div>
            <el-button type="info" @click="downloadChart">下载图表</el-button>
            <el-button type="primary" @click="generateReport">生成分析报告</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="card" @tab-change="switchChart">
        <el-tab-pane label="队列样本占比分析" name="queueRatio" />
        <el-tab-pane label="采集时间分布分析" name="timeDist" />
        <el-tab-pane label="样本类型差异分析" name="typeDiff" />
      </el-tabs>

      <div ref="echartsRef" class="echarts-container"></div>

      <div class="insight-box">
        <div class="insight-title">分析结论摘要</div>
        <ul>
          <li>{{ insights.line1 }}</li>
          <li>{{ insights.line2 }}</li>
          <li>{{ insights.line3 }}</li>
        </ul>
      </div>
    </el-card>

    <div class="content-grid">
      <el-card shadow="hover">
        <template #header>筛选后的明细数据</template>
        <el-table :data="filteredSamples" border height="420">
          <el-table-column prop="sampleNo" label="样本编号" min-width="120" />
          <el-table-column prop="sampleName" label="样本名称" min-width="140" />
          <el-table-column prop="source" label="来源" min-width="120" />
          <el-table-column label="样本类型" min-width="120">
            <template #default="scope">{{ getTypeName(scope.row.sampleTypeId) }}</template>
          </el-table-column>
          <el-table-column prop="queue" label="队列" min-width="120" />
          <el-table-column label="状态" width="100">
            <template #default="scope">{{ getStatusText(scope.row.status) }}</template>
          </el-table-column>
          <el-table-column prop="collectTime" label="采集时间" min-width="160">
            <template #default="scope">{{ formatTime(scope.row.collectTime) }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <SampleAssistantCard />
    </div>

    <el-dialog title="样本分析报告" v-model="reportVisible" width="60%">
      <el-input v-model="analysisReport" type="textarea" rows="14" readonly style="width: 100%" />
      <template #footer>
        <el-button @click="copyReport">复制报告</el-button>
        <el-button @click="reportVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from '@/utils/request'
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import SampleAssistantCard from './SampleAssistantCard.vue'

const echartsRef = ref(null)
let myChart = null
const activeTab = ref('queueRatio')
const reportVisible = ref(false)
const analysisReport = ref('')

const filterForm = reactive({
  timeRange: [],
  queueName: null,
  sampleTypeId: null
})

const queueList = ref([])
const sampleTypeList = ref([])
const allSamples = ref([])
const chartData = ref({ xAxis: [], series: [] })

const metrics = reactive({
  total: 0,
  pending: 0,
  normal: 0,
  abnormal: 0
})

const insights = reactive({
  line1: '当前暂无分析结论',
  line2: '请调整筛选条件后重新分析',
  line3: '点击图表可进一步钻取明细'
})

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const getTypeName = (sampleTypeId) => {
  const found = sampleTypeList.value.find(item => item.id === sampleTypeId)
  return found ? found.typeName : '未分类'
}

const getStatusText = (status) => {
  if (status === 0) return '待处理'
  if (status === 1) return '正常'
  if (status === 2) return '异常'
  return '其他'
}

const filteredSamples = computed(() => {
  let list = [...allSamples.value]
  if (filterForm.queueName) {
    list = list.filter(item => item.queue === filterForm.queueName)
  }
  if (filterForm.sampleTypeId) {
    list = list.filter(item => item.sampleTypeId === filterForm.sampleTypeId)
  }
  if (filterForm.timeRange && filterForm.timeRange.length === 2) {
    const start = dayjs(filterForm.timeRange[0]).startOf('day')
    const end = dayjs(filterForm.timeRange[1]).endOf('day')
    list = list.filter(item => {
      if (!item.collectTime) return false
      const current = dayjs(item.collectTime)
      return (current.isAfter(start) || current.isSame(start)) && (current.isBefore(end) || current.isSame(end))
    })
  }
  return list
})

const buildInsights = () => {
  const list = filteredSamples.value
  metrics.total = list.length
  metrics.pending = list.filter(item => item.status === 0).length
  metrics.normal = list.filter(item => item.status === 1).length
  metrics.abnormal = list.filter(item => item.status === 2).length

  const topQueue = countTop(list.map(item => item.queue).filter(Boolean))
  const topSource = countTop(list.map(item => item.source).filter(Boolean))
  const topTypeId = countTop(list.map(item => item.sampleTypeId).filter(Boolean))
  const topTypeName = topTypeId ? getTypeName(Number(topTypeId)) : '暂无'

  insights.line1 = `当前筛选结果共 ${metrics.total} 条样本，其中待处理 ${metrics.pending} 条、正常 ${metrics.normal} 条、异常 ${metrics.abnormal} 条。`
  insights.line2 = `主要样本队列：${topQueue || '暂无'}；主要来源：${topSource || '暂无'}。`
  insights.line3 = `当前数量最多的样本类型：${topTypeName}。`
}

const countTop = (arr) => {
  if (!arr.length) return null
  const counter = {}
  arr.forEach(item => { counter[item] = (counter[item] || 0) + 1 })
  return Object.keys(counter).sort((a, b) => counter[b] - counter[a])[0]
}

const initECharts = async () => {
  await nextTick()
  if (!echartsRef.value) return
  myChart = echarts.init(echartsRef.value)
  myChart.on('click', handleChartClick)
  window.addEventListener('resize', resizeChart)
}

const resizeChart = () => {
  myChart?.resize()
}

const loadDictData = async () => {
  const [typeRes, sampleRes] = await Promise.all([
    request.get('/sampleType/selectAll'),
    request.get('/biomedSample/selectAll')
  ])
  sampleTypeList.value = typeRes.data || []
  allSamples.value = sampleRes.data || []
  queueList.value = [...new Set(allSamples.value.map(s => s.queue).filter(Boolean))]
}

const buildParams = () => {
  const params = {}
  if (filterForm.timeRange.length === 2) {
    params.startDate = filterForm.timeRange[0]
    params.endDate = filterForm.timeRange[1]
  }
  if (filterForm.queueName) params.queueName = filterForm.queueName
  if (filterForm.sampleTypeId) params.sampleTypeId = filterForm.sampleTypeId
  return params
}

const queryAnalysisData = async () => {
  if (!myChart) return
  myChart.showLoading()

  const apiMap = {
    queueRatio: '/api/sample/analysis/queueRatio',
    timeDist: '/api/sample/analysis/collectTimeDist',
    typeDiff: '/api/sample/analysis/sampleTypeCount'
  }

  try {
    const res = await request.get(apiMap[activeTab.value], { params: buildParams() })
    chartData.value = res.data || { xAxis: [], series: [] }
    renderChart()
    buildInsights()
  } catch (e) {
    ElMessage.error('分析失败')
  } finally {
    myChart.hideLoading()
  }
}

const renderChart = () => {
  const { xAxis = [], series = [] } = chartData.value
  let option = {}

  if (activeTab.value === 'queueRatio') {
    option = {
      title: { text: '队列样本占比分析', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: xAxis.map((name, i) => ({ name, value: series[i] || 0 })),
        label: { formatter: '{b}: {c} ({d}%)' }
      }]
    }
  } else if (activeTab.value === 'timeDist') {
    option = {
      title: { text: '采集时间分布', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: xAxis },
      yAxis: { type: 'value' },
      series: [
        { type: 'bar', data: series },
        { type: 'line', data: series, smooth: true }
      ]
    }
  } else {
    option = {
      title: { text: '样本类型差异分析', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: xAxis },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: series }]
    }
  }

  myChart.setOption(option, true)
}

const handleChartClick = (params) => {
  if (!params?.name) return
  if (activeTab.value === 'queueRatio') {
    filterForm.queueName = params.name
  } else if (activeTab.value === 'typeDiff') {
    const found = sampleTypeList.value.find(item => item.typeName === params.name)
    if (found) filterForm.sampleTypeId = found.id
  } else if (activeTab.value === 'timeDist') {
    filterForm.timeRange = [params.name, params.name]
  }
  queryAnalysisData()
}

const switchChart = () => {
  queryAnalysisData()
}

const resetFilter = async () => {
  filterForm.timeRange = []
  filterForm.queueName = null
  filterForm.sampleTypeId = null
  await queryAnalysisData()
}

const downloadChart = () => {
  const a = document.createElement('a')
  a.href = myChart.getDataURL({ type: 'png', backgroundColor: '#fff' })
  a.download = '详细分析图表.png'
  a.click()
  ElMessage.success('下载成功')
}

const generateReport = () => {
  const queueText = filterForm.queueName || '全部队列'
  const typeText = filterForm.sampleTypeId ? getTypeName(filterForm.sampleTypeId) : '全部类型'
  const timeText = filterForm.timeRange.length === 2 ? `${filterForm.timeRange[0]} 至 ${filterForm.timeRange[1]}` : '全部时间'

  analysisReport.value = [
    '【样本详细分析报告】',
    '',
    `筛选条件：`,
    `- 时间范围：${timeText}`,
    `- 队列：${queueText}`,
    `- 样本类型：${typeText}`,
    '',
    `统计结果：`,
    `- 样本总量：${metrics.total}`,
    `- 待处理：${metrics.pending}`,
    `- 正常：${metrics.normal}`,
    `- 异常：${metrics.abnormal}`,
    '',
    `分析结论：`,
    `1. ${insights.line1}`,
    `2. ${insights.line2}`,
    `3. ${insights.line3}`,
    '',
    '说明：图表支持点击钻取，点击后可直接联动下方明细数据表。'
  ].join('\n')

  reportVisible.value = true
}

const copyReport = async () => {
  await navigator.clipboard.writeText(analysisReport.value)
  ElMessage.success('复制成功')
}

onMounted(async () => {
  await loadDictData()
  await initECharts()
  await queryAnalysisData()
})

onUnmounted(() => {
  if (myChart) {
    myChart.off('click', handleChartClick)
    myChart.dispose()
  }
  window.removeEventListener('resize', resizeChart)
  myChart = null
})
</script>

<style scoped>
.analysis-page {
  padding: 15px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 20px);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 15px;
  color: #1989fa;
}

.top-grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.metric-title {
  color: #909399;
  font-size: 14px;
}

.metric-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: bold;
}

.metric-value.warn { color: #e6a23c; }
.metric-value.success { color: #67c23a; }
.metric-value.danger { color: #f56c6c; }

.chart-card {
  margin-bottom: 16px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.echarts-container {
  width: 100%;
  height: 420px;
  margin-top: 20px;
}

.insight-box {
  margin-top: 16px;
  background: #f7f8fa;
  border-radius: 8px;
  padding: 12px 16px;
}

.insight-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}

@media (max-width: 1200px) {
  .top-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
