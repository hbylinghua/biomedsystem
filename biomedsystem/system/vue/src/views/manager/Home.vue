<template>
  <div class="dashboard-page">
    <div class="dashboard-header">
      <div class="title-wrap">
        <h2>生物医学样本可视化驾驶舱</h2>
        <p>样本全局态势、预警监控与趋势分析</p>
      </div>
      <div class="actions">
        <el-button @click="loadAll">刷新</el-button>
        <el-button type="primary" @click="$router.push('/sampleAnalysis')">进入详细分析</el-button>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="时间范围">
          <el-date-picker
              v-model="filterForm.timeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
          />
        </el-form-item>

        <el-form-item label="队列">
          <el-select v-model="filterForm.queueName" clearable placeholder="全部队列" style="width: 180px">
            <el-option v-for="item in queueList" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="样本类型">
          <el-select v-model="filterForm.sampleTypeId" clearable placeholder="全部类型" style="width: 180px">
            <el-option
                v-for="item in sampleTypeList"
                :key="item.id"
                :label="item.typeName"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadAll">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="metric-grid">
      <el-card class="metric-item">
        <div class="metric-title">样本总量</div>
        <div class="metric-value">{{ overview.totalCount || 0 }}</div>
      </el-card>
      <el-card class="metric-item">
        <div class="metric-title">今日新增</div>
        <div class="metric-value">{{ overview.todayAddCount || 0 }}</div>
      </el-card>
      <el-card class="metric-item">
        <div class="metric-title">在库样本</div>
        <div class="metric-value">{{ overview.inStorageCount || 0 }}</div>
      </el-card>
      <el-card class="metric-item">
        <div class="metric-title">待处理样本</div>
        <div class="metric-value">{{ overview.pendingCount || 0 }}</div>
      </el-card>
      <el-card class="metric-item">
        <div class="metric-title">异常样本</div>
        <div class="metric-value danger">{{ overview.abnormalCount || 0 }}</div>
      </el-card>
      <el-card class="metric-item">
        <div class="metric-title">存储利用率</div>
        <div class="metric-value">{{ overview.storageUsageRate || 0 }}%</div>
      </el-card>
    </div>

    <div class="chart-grid">
      <el-card class="chart-card">
        <template #header>样本采集趋势</template>
        <div ref="trendRef" class="chart-box"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>样本来源分布</template>
        <div ref="sourceRef" class="chart-box"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>状态分布</template>
        <div ref="statusRef" class="chart-box"></div>
      </el-card>
    </div>

    <div class="bottom-grid">
      <el-card>
        <template #header>样本预警</template>
        <el-table :data="warnings" size="small" height="300">
          <el-table-column prop="sampleNo" label="样本编号" />
          <el-table-column prop="sampleName" label="样本名称" />
          <el-table-column prop="queue" label="队列" />
          <el-table-column prop="warningText" label="预警信息" />
        </el-table>
      </el-card>

      <el-card>
        <template #header>系统公告</template>
        <el-timeline>
          <el-timeline-item
              v-for="item in noticeList"
              :key="item.id"
              :timestamp="formatTime(item.time)"
          >
            <el-card>
              <h4>{{ item.title }}</h4>
              <p>{{ item.content }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, onBeforeUnmount } from 'vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import request from '@/utils/request'
import {
  getDashboardOverview,
  getDashboardTrend,
  getDashboardStatus,
  getDashboardSource,
  getDashboardWarnings
} from '@/api/dashboard'

const trendRef = ref(null)
const sourceRef = ref(null)
const statusRef = ref(null)

let trendChart = null
let sourceChart = null
let statusChart = null
let timer = null

const overview = reactive({})
const warnings = ref([])
const noticeList = ref([])
const queueList = ref([])
const sampleTypeList = ref([])

const filterForm = reactive({
  timeRange: [],
  queueName: null,
  sampleTypeId: null,
})

const formatTime = (time) => time ? dayjs(time).format('YYYY/MM/DD') : '-'

const formatDate = (date) => {
  if (!date) return null
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const buildParams = () => {
  const params = {}
  if (filterForm.timeRange?.length === 2) {
    params.startDate = formatDate(filterForm.timeRange[0])
    params.endDate = formatDate(filterForm.timeRange[1])
  }
  if (filterForm.queueName) params.queueName = filterForm.queueName
  if (filterForm.sampleTypeId) params.sampleTypeId = filterForm.sampleTypeId
  return params
}

const initCharts = async () => {
  await nextTick()
  trendChart = echarts.init(trendRef.value)
  sourceChart = echarts.init(sourceRef.value)
  statusChart = echarts.init(statusRef.value)
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  trendChart?.resize()
  sourceChart?.resize()
  statusChart?.resize()
}

const renderTrend = (data) => {
  trendChart?.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.xAxis || [] },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, areaStyle: {}, data: data.series || [] }]
  })
}

const renderSource = (data) => {
  sourceChart?.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      data: (data.xAxis || []).map((name, i) => ({ name, value: data.series?.[i] || 0 }))
    }]
  })
}

const renderStatus = (data) => {
  statusChart?.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.xAxis || [] },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: data.series || [] }]
  })
}

const loadDictData = async () => {
  const [typeRes, sampleRes] = await Promise.all([
    request.get('/sampleType/selectAll'),
    request.get('/biomedSample/selectAll')
  ])
  sampleTypeList.value = typeRes.data || []
  const samples = sampleRes.data || []
  queueList.value = [...new Set(samples.map(s => s.queue).filter(Boolean))]
}

const loadAll = async () => {
  const params = buildParams()

  const [overviewRes, trendRes, sourceRes, statusRes, warningRes, noticeRes] = await Promise.all([
    getDashboardOverview(params),
    getDashboardTrend(params),
    getDashboardSource(params),
    getDashboardStatus(params),
    getDashboardWarnings(params),
    request.get('/notice/selectAll')
  ])

  Object.assign(overview, overviewRes.data || {})
  warnings.value = warningRes.data || []
  noticeList.value = noticeRes.data || []

  renderTrend(trendRes.data || {})
  renderSource(sourceRes.data || {})
  renderStatus(statusRes.data || {})
}

const resetFilter = () => {
  filterForm.timeRange = []
  filterForm.queueName = null
  filterForm.sampleTypeId = null
  loadAll()
}

onMounted(async () => {
  await initCharts()
  await loadDictData()
  await loadAll()
  timer = setInterval(loadAll, 60000)
})

onBeforeUnmount(() => {
  clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  sourceChart?.dispose()
  statusChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-page {
  padding: 16px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.title-wrap h2 {
  margin: 0;
  font-size: 24px;
}
.title-wrap p {
  margin: 6px 0 0;
  color: #909399;
}
.filter-card, .metric-grid, .chart-grid, .bottom-grid {
  margin-bottom: 16px;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}
.metric-item .metric-title {
  color: #909399;
  font-size: 14px;
}
.metric-item .metric-value {
  font-size: 28px;
  font-weight: bold;
  margin-top: 8px;
}
.metric-item .metric-value.danger {
  color: #f56c6c;
}
.chart-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 16px;
}
.chart-card .chart-box {
  height: 320px;
}
.bottom-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}
</style>