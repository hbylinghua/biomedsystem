<template>
  <div class="oper-log-page">
    <!-- 页面标题：操作日志管理 -->
    <div class="page-title">
      <span>样本操作日志查看</span>
      <el-button type="primary" size="small" @click="handleAdd" style="margin-left: 20px">
        新增日志
      </el-button>
    </div>

    <!-- 搜索区：多条件筛选 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input
          v-model="data.sampleId"
          style="width: 200px; margin-right: 10px"
          placeholder="请输入样本ID查询"
          clearable
          type="number"
      ></el-input>
      <el-select
          v-model="data.operType"
          style="width: 150px; margin-right: 10px"
          placeholder="请选择操作类型"
          clearable
      >
        <el-option label="录入" value="录入"></el-option>
        <el-option label="修改" value="修改"></el-option>
        <el-option label="删除" value="删除"></el-option>
        <el-option label="查询" value="查询"></el-option>
      </el-select>
      <el-input
          v-model="data.operBy"
          style="width: 200px; margin-right: 10px"
          placeholder="请输入操作人ID查询"
          clearable
          type="number"
      ></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <!-- 日志列表区 -->
    <div class="card" style="margin-bottom: 5px">
      <!-- 加载状态 -->
      <el-table
          :data="data.tableData"
          stripe
          v-loading="loading"
          border
          empty-text="暂无操作日志数据"
      >
        <!-- 序号列 -->
        <el-table-column
            label="序号"
            type="index"
            width="60"
            :index="(index) => (data.pageNum - 1) * data.pageSize + index + 1"
        ></el-table-column>
        <el-table-column label="日志ID" prop="id" width="100"></el-table-column>
        <el-table-column label="样本ID" prop="sampleId" width="100"></el-table-column>
        <el-table-column label="操作类型" prop="operType" width="100">
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.operType)">{{ scope.row.operType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作人ID" prop="operBy" width="100"></el-table-column>
        <el-table-column label="操作时间" prop="operTime" width="200">
          <template #default="scope">
            {{ formatTime(scope.row.operTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作内容" prop="content" min-width="300"></el-table-column>
        <!-- 预留删除操作（日志一般不删，可根据需求开启） -->
        <el-table-column label="操作" header-align="center" width="80" v-if="false">
          <template #default="scope">
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页区 -->
    <div class="card" v-if="data.total">
      <el-pagination
          background
          layout="prev, pager, next, jumper, ->, total"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @size-change="load"
          @current-change="load"
      />
    </div>

    <!-- 新增日志对话框 -->
    <el-dialog
        v-model="dialogVisible"
        title="新增操作日志"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="formRef"
          :model="logForm"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item label="样本ID" prop="sampleId">
          <el-input
              v-model="logForm.sampleId"
              type="number"
              placeholder="请输入样本ID"
              clearable
          />
        </el-form-item>
        <el-form-item label="操作类型" prop="operType">
          <el-select
              v-model="logForm.operType"
              placeholder="请选择操作类型"
              style="width: 100%"
          >
            <el-option label="录入" value="录入"></el-option>
            <el-option label="修改" value="修改"></el-option>
            <el-option label="删除" value="删除"></el-option>
            <el-option label="查询" value="查询"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作人ID" prop="operBy">
          <el-input
              v-model="logForm.operBy"
              type="number"
              placeholder="请输入操作人ID"
              clearable
          />
        </el-form-item>
        <el-form-item label="操作内容" prop="content">
          <el-input
              v-model="logForm.content"
              type="textarea"
              :rows="3"
              placeholder="请输入操作内容，如：录入样本BIO20250001"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLog" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import dayjs from "dayjs";

// 加载状态
const loading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const formRef = ref(null);

// 页面数据
const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  // 筛选条件
  sampleId: null,
  operType: null,
  operBy: null,
});

// 新增日志表单
const logForm = reactive({
  sampleId: null,
  operType: null,
  operBy: null,
  content: '',
});

// 表单校验规则
const rules = {
  sampleId: [
    { required: true, message: "请输入样本ID", trigger: "blur" }
  ],
  operType: [
    { required: true, message: "请选择操作类型", trigger: "change" }
  ],
  operBy: [
    { required: true, message: "请输入操作人ID", trigger: "blur" }
  ],
  content: [
    { required: true, message: "请输入操作内容", trigger: "blur" }
  ]
};

// 操作类型标签颜色映射
const getTagType = (operType) => {
  switch (operType) {
    case "录入":
      return "success";
    case "修改":
      return "warning";
    case "删除":
      return "danger";
    case "查询":
      return "info";
    default:
      return "default";
  }
};

// 时间格式化
const formatTime = (time) => {
  if (!time) return "-";
  return dayjs(time).format("YYYY-MM-DD HH:mm:ss");
};

// 分页查询日志 - 修复数据获取路径
const load = () => {
  loading.value = true;
  const queryParams = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    sampleId: data.sampleId,
    operType: data.operType,
    operBy: data.operBy,
  };
  request
      .get("/biomedOperLog/selectPage", {
        params: queryParams,
      })
      .then((res) => {
        // 修复：后端返回 Result.success(page)，其中 page 是 PageInfo 对象
        // res = Result 对象
        // res.data = PageInfo 对象 (page)
        // res.data.list = 数据列表
        // res.data.total = 总数
        const pageData = res.data || {};
        data.tableData = pageData.list || [];
        data.total = pageData.total || 0;
      })
      .catch((err) => {
        ElMessage.error("查询操作日志失败：" + (err.msg || err.message));
        console.error("查询失败：", err);
      })
      .finally(() => {
        loading.value = false;
      });
};

// 打开新增日志对话框
const handleAdd = () => {
  // 重置表单
  logForm.sampleId = null;
  logForm.operType = null;
  logForm.operBy = null;
  logForm.content = '';
  dialogVisible.value = true;
};

// 提交新增日志
const submitLog = () => {
  formRef.value?.validate((valid) => {
    if (valid) {
      submitLoading.value = true;
      request
          .post("/biomedOperLog/add", logForm)
          .then((res) => {
            console.log("响应数据：", res);
            // 修复：后端返回 Result 对象
            // res = AxiosResponse { data: Result对象 }
            // res.data = Result对象 { code, msg, data }
            const result = res.data;
            if (result && (result.code === "200" || result.code === 200)) {
              ElMessage.success("日志新增成功");
              dialogVisible.value = false;
              load(); // 刷新列表
            } else {
              ElMessage.error(result?.msg || "新增失败");
            }
          })
          .catch((err) => {
            console.error("请求错误：", err);
            ElMessage.error("新增日志失败：" + (err.response?.data?.msg || err.message || "请求失败"));
          })
          .finally(() => {
            submitLoading.value = false;
          });
    }
  });
};

// 删除日志（预留，默认隐藏）
const handleDelete = (id) => {
  ElMessageBox.confirm(
      "确定删除该操作日志吗？删除后无法恢复！",
      "删除确认",
      { type: "warning" }
  ).then(() => {
    request
        .delete(`/biomedOperLog/delete/${id}`)
        .then((res) => {
          const responseData = res.data || res;
          if (responseData.code === "200") {
            load();
            ElMessage.success("日志删除成功");
          } else {
            ElMessage.error(responseData.msg || "删除失败");
          }
        })
        .catch((err) => {
          ElMessage.error("删除日志失败：" + (err.msg || err.message));
        });
  }).catch(() => {});
};

// 重置筛选条件
const reset = () => {
  data.sampleId = null;
  data.operType = null;
  data.operBy = null;
  data.pageNum = 1;
  load();
};

// 初始化加载
load();
</script>

<style scoped>
.oper-log-page {
  padding: 10px;
}
.page-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #1989fa;
  display: flex;
  align-items: center;
}
.el-table {
  --el-table-header-text-color: #333;
  --el-table-row-hover-bg-color: #e0edfd;
}
</style>
