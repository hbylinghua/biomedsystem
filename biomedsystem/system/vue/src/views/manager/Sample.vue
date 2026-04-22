<template>
  <div class="sample-page">
    <!-- 页面标题：生物样本信息管理 -->
    <div class="page-title">生物样本信息管理</div>

    <!-- 搜索区：样本名称查询 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input
          v-model="data.name"
          style="width: 300px; margin-right: 10px"
          placeholder="请输入样本名称查询"
          clearable
      ></el-input>
      <!-- 在你的操作按钮区域，比如和“新增样本”按钮放一起 -->
      <el-button type="primary" @click="goToImport">批量导入</el-button>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
      <el-button type="success" @click="exportSample">批量导出</el-button>
    </div>

    <!-- 表格区：样本信息列表 -->

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增样本</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>

      </div>
      <el-table
          :data="data.tableData"
          stripe
          v-loading="loading"
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="样本名称" prop="source">
          <template #default="scope">
            <el-link type="primary" @click="goDetail(scope.row.id)">
              {{ scope.row.source }}
            </el-link>
          </template>
        </el-table-column>
        <!-- 🌟 新增：状态列 -->
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag
                :type="scope.row.status === 0 ? 'success' : scope.row.status === 1 ? 'info' : 'danger'"
            >
              {{ scope.row.status === 0 ? '正常' : scope.row.status === 1 ? '已使用' : '废弃' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="样本类型" prop="sampleTypeId" width="100">
          <template #default="scope">
            {{ getTypeName(scope.row.sampleTypeId) }}
          </template>
        </el-table-column>
        <el-table-column label="样本编号" prop="sampleNo" width="120"></el-table-column>
        <el-table-column label="存储位置" prop="queue" width="120"></el-table-column>
        <el-table-column label="采集时间" prop="collectTime" width="100">
          <template #default="scope">
            {{ formatTime(scope.row.collectTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" header-align="center" width="160">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="handleEdit(scope.row)"
            >编辑</el-button>
            <el-button
                type="danger"
                size="small"
                @click="handleDelete(scope.row.id)"
            >删除</el-button>
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
          style="text-align: right; padding: 10px 0"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        title="生物样本信息"
        width="50%"
        v-model="data.formVisible"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-form
          :model="data.form"
          label-width="120px"
          style="padding-right: 20px"
          :rules="formRules"
          ref="formRef"
      >
        <el-form-item label="样本名称" prop="source">
          <el-input
              v-model="data.form.source"
              autocomplete="off"
              placeholder="请输入样本名称"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="样本编号" prop="sampleNo">
          <el-input
              v-model="data.form.sampleNo"
              disabled
          />
        </el-form-item>
        <el-form-item label="存储位置" prop="queue">
          <el-input
              v-model="data.form.queue"
              autocomplete="off"
              placeholder="请输入存储位置（如：冷库A-01柜）"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="采集时间" prop="collectTime">
          <el-date-picker
              v-model="data.form.collectTime"
              type="date"
              placeholder="请选择采集时间"
              style="width: 100%"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="样本类型" prop="sampleTypeId">
          <el-select
              v-model="data.form.sampleTypeId"
              placeholder="请选择样本类型"
              style="width: 100%"
          >
            <el-option
                v-for="item in categoryList"
                :key="item.id"
                :label="item.typeName"
                :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 🌟 新增：状态下拉框 -->
        <el-form-item label="样本状态" prop="status">
          <el-select v-model="data.form.status" placeholder="请选择状态">
            <el-option label="正常" :value="0"></el-option>
            <el-option label="已使用" :value="1"></el-option>
            <el-option label="废弃" :value="2"></el-option>
          </el-select>
        </el-form-item>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref, watch } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import dayjs from "dayjs";
import { useRouter } from 'vue-router'
import { getToken } from '@/utils/auth'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import StorageRecommendDialog from './StorageRecommendDialog.vue'

// 文件上传地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload';
// 加载状态
const loading = ref(false);
// 表单校验引用
const formRef = ref(null);
// 上传文件列表
const fileList = ref([]);

// 核心响应式：样本类型列表
const categoryList = ref([]);

// 核心数据
const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null
});

// 表单校验规则
const formRules = {
  source: [{ required: true, message: '请输入样本名称', trigger: 'blur' }],
  queue: [{ required: true, message: '请输入存储位置', trigger: 'blur' }],
  sampleTypeId: [{ required: true, message: '请选择样本类型', trigger: 'change' }],
  collectTime: [{ required: true, message: '请选择采集时间', trigger: 'change' }]
};

// 时间格式化
const formatTime = (time) => {
  if (!time) return "暂无时间";
  return dayjs(time).format("YYYY-MM-DD");
};

// 获取样本类型名称
const getTypeName = (sampleTypeId) => {
  if (!sampleTypeId) return "未分类";
  const typeItem = categoryList.value.find(item => item.id == sampleTypeId);
  return typeItem ? typeItem.typeName : "未分类";
};

// 获取样本类型列表
const getCategoryList = () => {
  request.get('/sampleType/selectAll')
      .then(res => {
        if (res.code === '200' && Array.isArray(res.data)) {
          categoryList.value = res.data;
        } else {
          categoryList.value = [];
        }
      })
      .catch(err => {
        ElMessage.error('获取样本类型列表失败：' + (err.msg || '网络异常'));
        categoryList.value = [];
      });
};

// 分页查询样本数据
const load = () => {
  loading.value = true;
  const queryParams = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    source: data.name
  };
  request.get('/biomedSample/selectPage', { params: queryParams })
      .then(res => {
        const resData = res.data || res;
        if (res.code === '200') {
          data.tableData = resData.list || [];
          data.total = resData.total || 0;
        } else {
          ElMessage.warning(res.msg || '查询数据为空');
          data.tableData = [];
          data.total = 0;
        }
      })
      .catch(err => {
        ElMessage.error('查询样本数据失败：' + (err.msg || '网络异常'));
        data.tableData = [];
        data.total = 0;
      })
      .finally(() => {
        loading.value = false;
      });
};

// 新增样本
const handleAdd = () => {
  data.form = {}; // 清空表单
  data.formVisible = true;
  fileList.value = [];
  getCategoryList();
};

const router = useRouter()

//导入
const goToImport = () => {
  router.push('/sampleImport')
}

//详情
const goDetail = (id) => {
  router.push(`/sampleDetail/${id}`)
}

//导出
const exportSample = () => {
  request({
    url: "/biomedSample/export",
    method: "get",
    responseType: "blob",
    headers: {
      Authorization: `Bearer ${getToken() || ''}`
    }
  }).then(res => {
    const blob = new Blob([res], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `样本列表_${new Date().toLocaleDateString()}.xlsx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    ElMessage.success("导出成功！");
  }).catch(err => {
    // 把错误JSON转成文本提示
    const reader = new FileReader();
    reader.onload = () => {
      ElMessage.error("导出失败：" + reader.result);
    };
    reader.readAsText(err.response?.data || res);
  });
};

// 编辑样本
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  if (data.form.collectTime) {
    data.form.collectTime = dayjs(data.form.collectTime).format("YYYY-MM-DD");
  }
  data.formVisible = true;
  fileList.value = data.form.img ? [{ url: data.form.img }] : [];
  getCategoryList();
};

// 格式化时间
const formatCollectTime = (dateStr) => {
  if (!dateStr) return null;
  if (dateStr.indexOf(' ') === -1) {
    return `${dateStr} 00:00:00`;
  }
  return dateStr;
};

// 保存逻辑
const save = () => {
  formRef.value.validate((valid) => {
    if (!valid) {
      return ElMessage.warning('请完善必填字段！');
    }

    const submitData = {
      id: data.form.id || null,
      source: data.form.source,
      sampleNo: data.form.sampleNo || '',
      queue: data.form.queue,
      collectTime: formatCollectTime(data.form.collectTime),
      sampleTypeId: data.form.sampleTypeId,
      storageId: data.form.storageId || 0,
      status: data.form.status ?? 0 // 🌟 保存状态
    };

    const requestPromise = data.form.id
        ? request.put('/biomedSample/update', submitData)
        : request.post('/biomedSample/add', submitData);

    requestPromise.then(res => {
      if (res.code === '200') {
        ElMessage.success('保存成功');
        data.formVisible = false;
        load();
      } else {
        ElMessage.error('操作失败');
      }
    }).catch(err => {
      ElMessage.error('操作异常');
    });
  });
};

// 删除样本
const handleDelete = (id) => {
  if (!id) return ElMessage.warning('无效的样本ID');

  ElMessageBox.confirm(
      '删除后样本数据无法恢复，您确定删除吗?',
      '样本删除确认',
      { type: 'warning' }
  ).then(() => {
    request.delete(`/biomedSample/delete/${id}`)
        .then(res => {
          if (res.code === '200') {
            load();
            ElMessage.success('删除成功');
          }
        })
  }).catch(() => {
    ElMessage.info('已取消');
  });
};

//批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的样本类型！')
    return
  }
  ElMessageBox.confirm(
      `确定要删除选中的${selectedIds.value.length}个样本类型吗？删除后无法恢复！`,
      '批量删除确认',
      { type: 'warning' }
  ).then(() => {
    // 注：如果后端未实现批量删除，可循环调用单删接口
    Promise.all(
        selectedIds.value.map(id => request.delete(`/biomedSample/delete/${id}`))
    ).then(() => {
      load()
      ElMessage.success('批量删除样本类型成功')
      selectedIds.value = []
    }).catch(err => {
      ElMessage.error('批量删除失败：' + (err.msg || err.message))
    })
  }).catch(() => {})
}

const selectedIds = ref([]);  // 👈 就加这一行！
// 多选事件
const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
}


const recommendDialogVisible = ref(false)

const openRecommendDialog = () => {
  if (!form.sampleTypeId) {
    ElMessage.warning('请先选择样本类型')
    return
  }
  recommendDialogVisible.value = true
}

const handleChooseRecommendation = (row) => {
  form.storageId = row.storageId
  ElMessage.success(`已自动回填推荐位置：${row.position}`)
}



// 重置查询条件
const reset = () => {
  data.name = null;
  data.pageNum = 1;
  load();
};

// 初始化加载
const init = () => {
  getCategoryList();
  load();
};

init();
</script>

<style scoped>
.sample-page {
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

.card {
  background: #fff;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.dialog-footer {
  text-align: right;
}

.el-table .el-table__cell {
  padding: 10px 0;
}

.storage-select-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>