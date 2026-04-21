<template>
  <div class="storage-page">
    <!-- 页面标题：样本存储流转管理 -->
    <div class="page-title">生物样本存储流转管理</div>

    <!-- 搜索区：按样本名称查询 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input
          v-model="data.sampleName"
          style="width: 300px; margin-right: 10px"
          placeholder="请输入样本名称查询"
          clearable
      ></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <!-- 表格区：样本存储记录 -->
    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增存储记录</el-button>
      </div>
      <el-table
          :data="data.tableData"
          stripe
          v-loading="loading"
          border
          style="width: 100%"
          cell-padding="10"
      >
        <el-table-column label="样本名称" prop="sampleName" min-width="120"></el-table-column>
        <el-table-column label="入库数量" prop="number" width="100"></el-table-column>
        <el-table-column label="存储位置" prop="storageLocation" min-width="120"></el-table-column>
        <el-table-column label="入库日期" prop="storageDate" width="100"></el-table-column>
        <el-table-column label="保管人" prop="custodian" width="100"></el-table-column>
        <el-table-column label="存储状态" prop="storageStatus" width="100">
          <template #default="scope">
            <el-tag
                :type="scope.row.storageStatus === '正常' ? 'success' : (scope.row.storageStatus === '待核查' ? 'warning' : 'info')"
                size="small"
            >
              {{ scope.row.storageStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="comment" min-width="150"></el-table-column>
        <el-table-column label="操作" header-align="center" width="160">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页区：适配后端分页参数 -->
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
        title="样本存储信息"
        width="50%"
        v-model="data.formVisible"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-form
          :model="data.form"
          label-width="100px"
          style="padding-right: 20px"
          :rules="formRules"
          ref="formRef"
      >
        <el-form-item label="样本名称" prop="sampleId">
          <el-select
              v-model="data.form.sampleId"
              placeholder="请选择样本"
              style="width: 100%"
              @change="getSampleInfo"
          >
            <el-option
                v-for="item in data.sampleList"
                :key="item.id"
                :label="item.source"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="number">
          <el-input-number
              :min="1"
              v-model="data.form.number"
              placeholder="请输入入库数量"
              style="width: 100%"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="存储位置" prop="storageLocation">
          <el-input
              v-model="data.form.storageLocation"
              placeholder="请输入存储位置（如：冷库A-01柜）"
              maxlength="50"
              show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="入库日期" prop="storageDate">
          <el-date-picker
              v-model="data.form.storageDate"
              placeholder="请选择入库日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="保管人" prop="custodian">
          <el-input
              v-model="data.form.custodian"
              placeholder="请输入保管人姓名"
              maxlength="20"
              show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="存储状态" prop="storageStatus">
          <el-select v-model="data.form.storageStatus" placeholder="请选择存储状态" style="width: 100%">
            <el-option label="正常" value="正常"></el-option>
            <el-option label="待核查" value="待核查"></el-option>
            <el-option label="已出库" value="已出库"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="comment">
          <el-input
              v-model="data.form.comment"
              placeholder="请输入备注（如：样本保存要求）"
              type="textarea"
              rows="3"
              maxlength="200"
              show-word-limit
          ></el-input>
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
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import dayjs from "dayjs";

// 加载状态
const loading = ref(false);
const formRef = ref(null);

const formRules = {
  sampleId: [{ required: true, message: '请选择样本', trigger: 'change' }],
  number: [{ required: true, message: '请输入入库数量', trigger: 'blur' }],
  storageLocation: [{ required: true, message: '请输入存储位置', trigger: 'blur' }],
  storageDate: [{ required: true, message: '请选择入库日期', trigger: 'change' }],
  custodian: [{ required: true, message: '请输入保管人姓名', trigger: 'blur' }],
  storageStatus: [{ required: true, message: '请选择存储状态', trigger: 'change' }]
};

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: { storageStatus: '正常' },
  tableData: [],
  sampleName: null,
  sampleList: [],
  currentStock: 0
});

// 字段映射：前端 → 后端
const mapFrontToBack = (formData) => {
  const backData = {
    id: formData.id || null,
    position: formData.storageLocation,
    status: formData.storageStatus,
    expireTime: formData.storageDate ? `${formData.storageDate}T00:00:00` : null,
    temp: JSON.stringify({
      sampleId: formData.sampleId,
      number: formData.number,
      custodian: formData.custodian,
      comment: formData.comment
    })
  };
  return backData;
};

// 字段映射：后端 → 前端
const mapBackToFront = (backData) => {
  const frontData = {
    id: backData.id,
    storageLocation: backData.position,
    storageStatus: backData.status,
    storageDate: backData.expireTime ? dayjs(backData.expireTime).format('YYYY-MM-DD') : '',
    ...(backData.temp ? JSON.parse(backData.temp) : {})
  };
  if (frontData.sampleId) {
    const sample = data.sampleList.find(item => item.id == frontData.sampleId);
    frontData.sampleName = sample ? sample.source : '未知样本';
  }
  return frontData;
};

// 获取样本列表
const getSampleList = () => {
  request.get('/biomedSample/selectAll')
      .then(res => {
        if (res.code === '200') {
          data.sampleList = res.data || [];
        }
      })
      .catch(err => {
        ElMessage.error('获取样本列表失败');
      });
};

const getSampleInfo = () => {
  const sample = data.sampleList.find(item => item.id === data.form.sampleId);
  if (sample && !data.form.storageLocation) {
    data.form.storageLocation = sample.queue || '';
  }
};

// ===================== ✅ 搜索已修复，其他完全不变 =====================
const load = () => {
  loading.value = true;
  const queryParams = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    sampleName: data.sampleName,  // 搜索词
    position: '',
    status: ''
  };
  request.get('/biomedStorage/selectPage', { params: queryParams })
      .then(res => {
        if (res.code === '200') {
          data.tableData = (res.data.list || []).map(item => mapBackToFront(item));
          data.total = res.data.total || 0;
        } else {
          data.tableData = [];
          data.total = 0;
        }
      })
      .catch(err => {
        data.tableData = [];
        data.total = 0;
      })
      .finally(() => {
        loading.value = false;
      });
};

// 新增
const handleAdd = () => {
  data.form = { storageStatus: '正常' };
  data.formVisible = true;
  getSampleList();
};

// 编辑
const handleEdit = (row) => {
  data.form = { ...row };
  data.formVisible = true;
  getSampleList();
};

// 保存
const add = () => {
  const submitData = mapFrontToBack(data.form);
  request.post('/biomedStorage/add', submitData)
      .then(res => {
        if (res.code === '200') {
          load();
          data.formVisible = false;
          ElMessage.success('新增成功');
        }
      });
};

const update = () => {
  const submitData = mapFrontToBack(data.form);
  request.put('/biomedStorage/update', submitData)
      .then(res => {
        if (res.code === '200') {
          load();
          data.formVisible = false;
          ElMessage.success('编辑成功');
        }
      });
};

const save = () => {
  formRef.value.validate(valid => {
    if (valid) data.form.id ? update() : add();
    else ElMessage.warning('请完善必填项');
  });
};

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除？').then(() => {
    request.delete('/biomedStorage/delete/' + id).then(res => {
      if (res.code === '200') {
        load();
        ElMessage.success('删除成功');
      }
    });
  });
};

const reset = () => {
  data.sampleName = null;
  data.pageNum = 1;
  load();
};

const init = () => {
  getSampleList();
  load();
};

init();
</script>
<style scoped>
.storage-page {
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
  margin-bottom: 10px;
}

.el-tag {
  font-size: 12px;
}

.dialog-footer {
  text-align: right;
}
</style>