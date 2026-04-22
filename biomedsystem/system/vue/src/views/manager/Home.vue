<template>
  <div class="dashboard-page">
    <div class="dashboard-header">
      <div class="title-wrap">
        <h2>生物医学样本可视化驾驶舱</h2>
        <p>样本全局态势、预警监控与趋势分析</p>
      </div>
      <div class="actions">
        <el-button @click="loadAll">刷新</el-button>
        <el-button type="primary" @click="$router.push('/sampleAnalysis')">
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
        <div class="metric-value">{{ overview.totalCount }}</div>
      </el-card>

      <el-card class="metric-item">
        <div class="metric-title">今日新增</div>
        <div class="metric-value">{{ overview.todayAddCount }}</div>
      </el-card>

      <el-card class="metric-item">
        <div class="metric-title">已入库存</div>
        <div class="metric-value">{{ overview.inStorageCount }}</div>
      </el-card>

      <el-card class="metric-item">
        <div class="metric-title">待处理样本</div>
        <div class="metric-value">{{ overview.pendingCount }}</div>
      </el-card>

      <el-card class="metric-item">
        <div class="metric-title">异常样本</div>
        <div class="metric-value danger">{{ overview.abnormalCount }}</div>
      </el-card>

      <el-card class="metric-item">
        <div class="metric-title">入库率</div>
        <div class="metric-value">{{ overview.storageUsageRate }}%</div>
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
          <el-table-column prop="queue" label="队列" min-width="100" />
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
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import request from '@/utils/request'

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
const allSamples = ref([])

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

const isSuccess = (res) => {
  return !!res && (res.code === 200 || res.code === '200')
}

const formatTime = (value) => {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

const normalizeStatus = (status) => {
  if (status === 0 || status === '待处理') return '待处理'
  if (status === 1 || status === '正常') return '正常'
  if (status === 2 || status === '异常') return '异常'
  return '其他'
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

const applyFilters = (samples) => {
  let list = [...samples]

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
      const t = dayjs(item.collectTime)
      return (t.isAfter(start) || t.isSame(start)) && (t.isBefore(end) || t.isSame(end))
    })
  }

  return list
}

const calcOverview = (samples) => {
  const today = dayjs().format('YYYY-MM-DD')

  overview.totalCount = samples.length
  overview.todayAddCount = samples.filter(item => {
    return item.createTime && dayjs(item.createTime).format('YYYY-MM-DD') === today
  }).length

  overview.inStorageCount = samples.filter(item => !!item.storageId).length
  overview.pendingCount = samples.filter(item => normalizeStatus(item.status) === '待处理').length
  overview.abnormalCount = samples.filter(item => normalizeStatus(item.status) === '异常').length
  overview.storageUsageRate = samples.length
      ? Number(((overview.inStorageCount / samples.length) * 100).toFixed(2))
      : 0
}

const buildDistribution = (samples, getter) => {
  const counter = {}
  samples.forEach(item => {
    const key = getter(item) || '未知'
    counter[key] = (counter[key] || 0) + 1
  })

  return {
    xAxis: Object.keys(counter),
    series: Object.values(counter)
  }
}

const calcWarnings = (samples) => {
  warnings.value = samples
      .filter(item => normalizeStatus(item.status) === '异常' || !item.storageId)
      .map(item => ({
        sampleNo: item.sampleNo,
        sampleName: item.sampleName,
        queue: item.queue,
        warningText: normalizeStatus(item.status) === '异常' ? '样本异常' : '未入库'
      }))
      .slice(0, 10)
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

const loadAll = async () => {
  try {
    const params = buildParams()

    const [typeRes, sampleRes, trendRes, noticeRes] = await Promise.allSettled([
      request.get('/sampleType/selectAll'),
      request.get('/biomedSample/selectAll'),
      request.get('/api/sample/analysis/collectTimeDist', { params }),
      request.get('/notice/selectAll')
    ])

    if (typeRes.status === 'fulfilled' && isSuccess(typeRes.value)) {
      sampleTypeList.value = typeRes.value.data || []
    } else {
      sampleTypeList.value = []
    }

    if (sampleRes.status === 'fulfilled' && isSuccess(sampleRes.value)) {
      allSamples.value = sampleRes.value.data || []
      queueList.value = [...new Set(allSamples.value.map(item => item.queue).filter(Boolean))]
    } else {
      allSamples.value = []
      queueList.value = []
    }

    const filteredSamples = applyFilters(allSamples.value)
    calcOverview(filteredSamples)
    calcWarnings(filteredSamples)

    const sourceData = buildDistribution(filteredSamples, item => item.source)
    const statusData = buildDistribution(filteredSamples, item => normalizeStatus(item.status))

    renderSource(sourceData)
    renderStatus(statusData)

    if (trendRes.status === 'fulfilled' && isSuccess(trendRes.value)) {
      renderTrend(trendRes.value.data || { xAxis: [], series: [] })
    } else {
      renderTrend({ xAxis: [], series: [] })
    }

    if (noticeRes.status === 'fulfilled' && isSuccess(noticeRes.value)) {
      noticeList.value = noticeRes.value.data || []
    } else {
      noticeList.value = []
    }
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