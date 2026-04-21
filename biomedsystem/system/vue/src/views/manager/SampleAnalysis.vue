<template>
  <div class="analysis-page">
    <div class="page-title">样本数据分析</div>
    <div class="core-container">
      <!-- 左侧筛选区 -->
      <div class="filter-card">
        <el-card title="分析条件筛选" shadow="hover">
          <el-form :model="filterForm" label-width="100px">
            <el-form-item label="分析时间范围">
              <el-date-picker
                  v-model="filterForm.timeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 100%"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="实验队列">
              <el-select v-model="filterForm.queueName" placeholder="请选择队列" clearable style="width: 100%">
                <el-option
                    v-for="item in queueList"
                    :key="item"
                    :label="item"
                    :value="item"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="样本类型">
              <el-select v-model="filterForm.sampleTypeId" placeholder="请选择类型" clearable style="width: 100%">
                <el-option
                    v-for="item in sampleTypeList"
                    :key="item.id"
                    :label="item.typeName"
                    :value="item.id"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="queryAnalysisData" style="width: 100%">执行分析</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 右侧图表区 -->
      <div class="chart-card">
        <el-tabs v-model="activeTab" type="card" @tab-change="switchChart">
          <el-tab-pane label="队列样本占比分析" name="queueRatio"></el-tab-pane>
          <el-tab-pane label="采集时间分布分析" name="timeDist"></el-tab-pane>
          <el-tab-pane label="样本类型差异分析" name="typeDiff"></el-tab-pane>
        </el-tabs>

        <div class="echarts-container" ref="echartsRef" style="width: 100%; height: 400px; margin-top: 20px;"></div>

        <div class="operate-bar" style="margin-top: 20px; text-align: right;">
          <el-button type="info" icon="Download" @click="downloadChart">下载图表</el-button>
          <el-button type="primary" icon="Document" @click="generateReport">生成分析报告</el-button>
        </div>

        <el-dialog title="样本分析报告" v-model="reportVisible" width="60%">
          <el-input v-model="analysisReport" type="textarea" rows="10" readonly style="width: 100%"></el-input>
          <template #footer>
            <el-button @click="copyReport">复制报告</el-button>
            <el-button @click="reportVisible = false">关闭</el-button>
          </template>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { ref, reactive, onMounted, onUnmounted } from "vue";
import * as echarts from "echarts";
import { ElMessage } from "element-plus";

const echartsRef = ref(null);
let myChart = null;
const activeTab = ref("queueRatio");

// ===================== 筛选表单（动态） =====================
const filterForm = reactive({
  timeRange: [],
  queueName: null,    // 队列名称
  sampleTypeId: null  // 样本类型ID
});

// ===================== 动态下拉数据（从数据库取） =====================
const queueList = ref([]);       // 队列列表
const sampleTypeList = ref([]);  // 样本类型列表

const analysisReport = ref("");
const reportVisible = ref(false);

// ===================== 工具方法 =====================
const formatDate = (date) => {
  if (!date) return null;
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

// ===================== 初始化图表 =====================
const initECharts = () => {
  if (!echartsRef.value) return;
  myChart = echarts.init(echartsRef.value);
  window.addEventListener("resize", () => myChart && myChart.resize());
  myChart.setOption({
    title: { text: '加载中...', left: 'center', top: 'middle' },
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', data: [] }]
  });
};

// ===================== 加载下拉框数据（动态！） =====================
const loadDictData = async () => {
  try {
    // 1. 加载所有样本类型
    let typeRes = await request.get("/sampleType/selectAll");
    if (typeRes.code === "200") {
      sampleTypeList.value = typeRes.data || [];
    }

    // 2. 加载所有队列（从样本表提取不重复队列）
    let sampleRes = await request.get("/biomedSample/selectAll");
    if (sampleRes.code === "200") {
      let samples = sampleRes.data || [];
      let queues = [...new Set(samples.map(s => s.queue).filter(q => q))];
      queueList.value = queues;
    }
  } catch (e) {
    ElMessage.error("加载下拉数据失败");
  }
};

// ===================== 执行分析（带筛选条件！） =====================
const queryAnalysisData = () => {
  if (!myChart) {
    ElMessage.error("图表未初始化");
    return;
  }

  myChart.showLoading();

  // 接口映射
  const apiMap = {
    queueRatio: "/api/sample/analysis/queueRatio",
    timeDist: "/api/sample/analysis/collectTimeDist",
    typeDiff: "/api/sample/analysis/sampleTypeCount"
  };
  const api = apiMap[activeTab.value];

  // 筛选参数
  const params = {};
  if (filterForm.timeRange.length === 2) {
    params.startDate = formatDate(filterForm.timeRange[0]);
    params.endDate = formatDate(filterForm.timeRange[1]);
  }
  if (filterForm.queueName) params.queueName = filterForm.queueName;
  if (filterForm.sampleTypeId) params.sampleTypeId = filterForm.sampleTypeId;

  // 请求后端
  request.get(api, { params }).then(res => {
    if (res.code !== "200") {
      ElMessage.error(res.msg || "分析失败");
      return;
    }

    let { xAxis, series } = res.data;
    if (!xAxis) xAxis = [];
    if (!series) series = [];

    // 图表配置
    let option = {};
    if (activeTab.value === "queueRatio") {
      option = {
        title: { text: '队列样本占比分析', left: 'center' },
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie', radius: ['40%', '70%'],
          data: xAxis.map((name, i) => ({ name, value: series[i] || 0 })),
          label: { formatter: '{b}: {c} ({d}%)' }
        }]
      };
    } else if (activeTab.value === "timeDist") {
      option = {
        title: { text: '采集时间分布', left: 'center' },
        xAxis: { type: 'category', data: xAxis },
        yAxis: { type: 'value' },
        series: [
          { type: 'bar', data: series },
          { type: 'line', data: series, smooth: true }
        ]
      };
    } else {
      option = {
        title: { text: '样本类型统计', left: 'center' },
        xAxis: { type: 'category', data: xAxis },
        yAxis: { type: 'value' },
        series: [{ type: 'bar', data: series }]
      };
    }

    myChart.setOption(option);
    ElMessage.success("分析完成");

  }).finally(() => {
    myChart.hideLoading();
  });
};

// ===================== 切换图表 / 下载 / 报告 =====================
const switchChart = () => queryAnalysisData();
const downloadChart = () => {
  const a = document.createElement('a');
  a.href = myChart.getDataURL({ type: 'png', backgroundColor: '#fff' });
  a.download = '分析图表.png';
  a.click();
  ElMessage.success('下载成功');
};
const generateReport = () => { reportVisible.value = true; };
const copyReport = () => {
  navigator.clipboard.writeText(analysisReport.value);
  ElMessage.success('复制成功');
};

// ===================== 生命周期 =====================
onMounted(async () => {
  initECharts();
  await loadDictData();     // 加载下拉框
  queryAnalysisData();      // 默认加载一次分析
});

onUnmounted(() => {
  myChart?.dispose();
  myChart = null;
});
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
.core-container {
  display: flex;
  gap: 20px;
  width: 100%;
  flex-wrap: wrap;
}
.filter-card {
  width: 300px;
  flex-shrink: 0;
}
.chart-card {
  flex: 1;
  min-width: 600px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}
@media (max-width: 992px) {
  .core-container {
    flex-direction: column;
  }
  .filter-card {
    width: 100%;
  }
  .chart-card {
    min-width: 100%;
  }
}
</style>