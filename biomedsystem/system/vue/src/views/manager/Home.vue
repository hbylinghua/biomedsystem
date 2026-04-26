<template>
  <div class="dashboard-page">
    <div class="dashboard-header">
      <div class="title-wrap">
        <h2>生物医学样本可视化驾驶舱</h2>
        <p>样本全局态势、预警监控与趋势分析</p>
      </div>
      <div class="actions">
        <el-button @click="loadAll">刷新</el-button>
        <el-button type="primary" @click="router.push('/sampleAnalysis')">
          进入详细分析
        </el-button>
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
              value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="队列">
          <el-select
              v-model="filterForm.queueName"
              clearable
              placeholder="全部队列"
              style="width: 180px"
          >
            <el-option
                v-for="item in queueList"
                :key="item"
                :label="item"
                :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="样本类型">
          <el-select
              v-model="filterForm.sampleTypeId"
              clearable
              placeholder="全部类型"
              style="width: 180px"
          >
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
        <div class="metric-title">已入库样本</div>
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
        <div class="metric-title">已入库占比</div>
        <div class="metric-value">{{ overview.storageUsageRate || 0 }}%</div>
      </el-card>
    </div>

    <div class="chart-grid">
      <el-card class="chart-card">
        <template #header>采集时间趋势</template>
        <div ref="trendRef" class="chart-box"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>样本来源分布</template>
        <div ref="sourceRef" class="chart-box"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>样本状态分布</template>
        <div ref="statusRef" class="chart-box"></div>
      </el-card>
    </div>

    <div class="bottom-grid">
      <el-card>
        <template #header>预警样本</template>
        <el-table :data="warnings" size="small" height="320" border>
          <el-table-column prop="sampleNo" label="样本编号" min-width="130" />
          <el-table-column prop="sampleName" label="样本名称" min-width="140" />
          <el-table-column prop="queue" label="样本队列" min-width="120" />
          <el-table-column prop="warningText" label="预警信息" min-width="160" />
        </el-table>
      </el-card>

      <el-card>
        <template #header>系统公告</template>
        <el-timeline>
          <el-timeline-item
              v-for="item in noticeList"
              :key="item.id"
              :timestamp="formatTime(item.time)"
              placement="top"
          >
            <el-card shadow="never">
              <h4 class="notice-title">{{ item.title }}</h4>
              <p class="notice-content">{{ item.content }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>

    <div style="margin-top: 16px;">
      <PendingRecommendCard />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import request from '@/utils/request'
import PendingRecommendCard from './PendingRecommendCard.vue'
import {
  getDashboardOverview,
  getDashboardTrend,
  getDashboardSource,
  getDashboardStatus,
  getDashboardWarnings
} from '@/api/dashboard'

const router = useRouter()

const trendRef = ref(null)
const sourceRef = ref(null)
const statusRef = ref(null)

let trendChart = null
let sourceChart = null
let statusChart = null
let timer = null

const sampleTypeList = ref([])
const queueList = ref([])
const noticeList = ref([])
const warnings = ref([])

const filterForm = reactive({
  timeRange: [],
  queueName: null,
  sampleTypeId: null
})

const overview = reactive({
  totalCount: 0,
  todayAddCount: 0,
  inStorageCount: 0,
  pendingCount: 0,
  abnormalCount: 0,
  storageUsageRate: 0
})

const formatTime = (value) => {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

const buildParams = () => {
  const params = {}
  if (filterForm.timeRange && filterForm.timeRange.length === 2) {
    params.startDate = filterForm.timeRange[0]
    params.endDate = filterForm.timeRange[1]
  }
  if (filterForm.queueName) {
    params.queueName = filterForm.queueName
  }
  if (filterForm.sampleTypeId) {
    params.sampleTypeId = filterForm.sampleTypeId
  }
  return params
}

const initCharts = async () => {
  await nextTick()

  if (trendRef.value && !trendChart) {
    trendChart = echarts.init(trendRef.value)
  }
  if (sourceRef.value && !sourceChart) {
    sourceChart = echarts.init(sourceRef.value)
  }
  if (statusRef.value && !statusChart) {
    statusChart = echarts.init(statusRef.value)
  }

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
    grid: { left: 40, right: 20, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data?.xAxis || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        type: 'line',
        smooth: true,
        areaStyle: {},
        data: data?.series || []
      }
    ]
  })
}

const renderSource = (data) => {
  sourceChart?.setOption({
    tooltip: { trigger: 'item' },
    legend: {
      bottom: 0
    },
    series: [
      {
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['50%', '45%'],
        data: (data?.xAxis || []).map((name, index) => ({
          name,
          value: data?.series?.[index] || 0
        }))
      }
    ]
  })
}

const renderStatus = (data) => {
  statusChart?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data?.xAxis || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        type: 'bar',
        barWidth: 40,
        data: data?.series || []
      }
    ]
  })
}

const loadDictData = async () => {
  try {
    const [typeRes, sampleRes] = await Promise.all([
      request.get('/sampleType/selectAll'),
      request.get('/biomedSample/selectAll')
    ])
    sampleTypeList.value = typeRes.data || []
    const samples = sampleRes.data || []
    queueList.value = [...new Set(samples.map(s => s.queue).filter(Boolean))]
  } catch (e) {
    sampleTypeList.value = []
    queueList.value = []
  }
}

const loadAll = async () => {
  try {
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
  } catch (err) {
    console.error('首页数据加载失败：', err)
    ElMessage.error('首页数据加载失败')
  }
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
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  sourceChart?.dispose()
  statusChart?.dispose()
  trendChart = null
  sourceChart = null
  statusChart = null
})
</script>

<style scoped lang="scss">
.dashboard-page {
  min-height: calc(100vh - 60px);
  padding: 16px;
  background: #f5f7fa;
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
  color: #303133;
}

.title-wrap p {
  margin: 6px 0 0;
  color: #909399;
}

.filter-card,
.metric-grid,
.chart-grid,
.bottom-grid {
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
  margin-top: 8px;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
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
  grid-template-columns: 1.3fr 1fr;
  gap: 16px;
}

.notice-title {
  margin: 0 0 8px 0;
  font-size: 15px;
}

.notice-content {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 1400px) {
  .metric-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .chart-grid,
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}
</style>
