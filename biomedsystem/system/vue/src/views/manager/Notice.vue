<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input
          v-model="data.title"
          style="width: 300px; margin-right: 10px"
          placeholder="请输入标题查询"
          clearable
      ></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <!-- 新增：加载状态 + 空数据提示 -->
      <el-table
          :data="data.tableData"
          stripe
          v-loading="loading"
          empty-text="暂无公告数据"
      >
        <el-table-column label="标题" prop="title"></el-table-column>
        <el-table-column label="内容" prop="content"></el-table-column>
        <!-- 核心修改：格式化展示发布时间 -->
        <el-table-column label="发布时间" width="200">
          <template #default="scope">
            {{ formatTime(scope.row.time) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" header-align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <!-- 修复：绑定分页事件，切换页码/条数时重新查询 -->
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

    <el-dialog
        title="系统公告信息"
        width="40%"
        v-model="data.formVisible"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="data.form.title" autocomplete="off" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" v-model="data.form.content" autocomplete="off" />
        </el-form-item>
        <!-- 无需添加时间输入框，由后端自动填充 -->
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
import { reactive, ref } from "vue";  // 新增：引入ref
import { ElMessageBox, ElMessage } from "element-plus";
// 核心新增：引入dayjs处理时间格式化
import dayjs from "dayjs";

// 新增：加载状态
const loading = ref(false);

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  title: null,
  noticeList: []
})

// 核心新增：时间格式化函数（兼容字符串/时间对象）
const formatTime = (time) => {
  if (!time) return "暂无时间"; // 空值兜底
  // 格式化规则：年-月-日 时:分:秒
  return dayjs(time).format("YYYY-MM-DD HH:mm:ss");
}

// 修复：分页查询接口改为selectPage，补充错误处理
const load = () => {
  loading.value = true;
  // 调用查询所有数据的接口 selectAll
  request.get('/notice/selectAll', {
    params: {
      title: data.title // 只传查询条件，不分页
    }
  }).then(res => {
    // selectAll 返回的就是数组，直接赋值
    data.tableData = res.data || [];
    data.total = data.tableData.length; // 手动计算总数
  }).catch(err => {
    ElMessage.error('查询公告失败：' + (err.msg || err.message));
  }).finally(() => {
    loading.value = false;
  });
}

// 新增
const handleAdd = () => {
  data.form = {};
  data.formVisible = true;
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  data.formVisible = true;
}

// 新增保存：补充空值校验 + 重置查询条件
const add = () => {
  // 新增：标题非空校验
  if (!data.form.title) {
    ElMessage.warning('公告标题不能为空！');
    return;
  }
  // 新增：内容非空校验
  if (!data.form.content) {
    ElMessage.warning('公告内容不能为空！');
    return;
  }
  // 前端无需传time字段，由后端自动填充当前时间
  request.post('/notice/add', data.form).then(res => {
    if (res.code === '200') {
      data.pageNum = 1;  // 新增：回到第一页
      load();
      ElMessage.success('公告新增成功');
      data.formVisible = false;
    } else {
      ElMessage.error(res.msg || '新增失败');
    }
  }).catch(err => {
    ElMessage.error('新增公告失败：' + (err.msg || err.message));
  })
}

// 编辑保存：补充空值校验
const update = () => {
  if (!data.form.title) {
    ElMessage.warning('公告标题不能为空！');
    return;
  }
  if (!data.form.content) {
    ElMessage.warning('公告内容不能为空！');
    return;
  }
  // 编辑时也无需传time，如需更新时间则在后端处理
  request.put('/notice/update', data.form).then(res => {
    if (res.code === '200') {
      load();
      ElMessage.success('公告编辑成功');
      data.formVisible = false;
    } else {
      ElMessage.error(res.msg || '编辑失败');
    }
  }).catch(err => {
    ElMessage.error('编辑公告失败：' + (err.msg || err.message));
  })
}

// 弹窗保存
const save = () => {
  data.form.id ? update() : add();
}

// 删除：优化错误处理
const handleDelete = (id) => {
  ElMessageBox.confirm(
      '删除后数据无法恢复，您确定删除吗?',
      '删除确认',
      { type: 'warning' }
  ).then(() => {
    request.delete('/notice/delete/' + id).then(res => {
      const responseData = res.data || res;
      if (responseData.code === '200') {
        load();
        ElMessage.success('公告删除成功');
      } else {
        ElMessage.error(responseData.msg || '删除失败');
      }
    }).catch(err => {
      ElMessage.error('删除公告失败：' + (err.msg || err.message));
    })
  }).catch(() => {})
}

// 修复：重置时重置页码到第1页
const reset = () => {
  data.title = null;
  data.pageNum = 1;  // 关键：回到第一页
  load();
}

// 初始化加载
load();
</script>

<style scoped>
/* 新增：统一样式，和其他页面保持一致 */
.card {
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.el-table {
  --el-table-header-text-color: #333;
  --el-table-row-hover-bg-color: #e0edfd;
}
</style>