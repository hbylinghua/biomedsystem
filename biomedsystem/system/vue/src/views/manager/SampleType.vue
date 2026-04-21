<template>
  <div class="sample-type-page">
    <!-- 页面标题：样本类型管理 -->
    <div class="page-title">生物样本类型管理</div>

    <!-- 搜索区：按类型名称查询 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input
          v-model="data.name"
          style="width: 300px; margin-right: 10px"
          placeholder="请输入样本类型名称查询"
          clearable
      ></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <!-- 操作&表格区：样本类型列表 -->
    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px; display: flex; justify-content: space-between;">
        <el-button type="primary" @click="handleAdd">新增样本类型</el-button>
        <!-- 批量删除：毕设加分项 -->
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
      </div>

      <!-- 加载状态：提升用户体验 -->
      <el-table
          :data="data.tableData"
          stripe
          v-loading="loading"
          @selection-change="handleSelectionChange"
      >
        <!-- 多选框：批量操作 -->
        <el-table-column type="selection" width="55"></el-table-column>
        <!-- 序号列 -->
        <el-table-column
            label="序号"
            type="index"
            width="60"
            :index="(index) => (data.pageNum - 1) * data.pageSize + index + 1"
        ></el-table-column>
        <el-table-column label="类型名称" prop="name"></el-table-column>
        <el-table-column label="创建时间" prop="createTime"></el-table-column>
        <el-table-column label="操作" header-align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog title="样本类型信息" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入样本类型名称" />
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

// 加载状态
const loading = ref(false)
// 批量删除选中的ID列表
const selectedIds = ref([])

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null  // 类型名称查询条件
})

// 分页查询：对接后端SampleTypeController的selectPage接口
const load = () => {
  loading.value = true
  const queryParams = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    name: data.name
  }
  request.get('/sampleType/selectPage', { params: queryParams }).then(res => {
    // 关键：后端返回的是 typeName，映射为前端的 name
    data.tableData = (res.data.list || []).map(item => ({
      id: item.id,
      name: item.typeName, // 注意：这里是 typeName（实体类字段），不是之前的 typename
      description: item.description,
      createTime: item.createTime
    }))
    data.total = res.data.total || 0
  }).catch(err => {
    ElMessage.error('查询样本类型失败：' + (err.msg || err.message))
  }).finally(() => {
    loading.value = false
  })
}

// 新增样本类型
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// 编辑样本类型
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存：对接后端sampleType/add接口
const add = () => {
  // 简单校验：类型名称不能为空
  if (!data.form.name) {
    ElMessage.warning('样本类型名称不能为空！')
    return
  }
  const submitData = {
    typeName: data.form.name, // 前端name → 后端typeName
    description: data.form.description || '' // 空值处理，避免undefined
  }
  request.post('/sampleType/add', submitData).then(res => {
    if (res.code === '200') {
      data.name = null // 清空搜索框
      data.pageNum = 1 // 回到第一页
      load()
      ElMessage.success('样本类型新增成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg || '新增失败')
    }
  }).catch(err => {
    ElMessage.error('新增样本类型失败：' + (err.msg || err.message))
  })
}

// 编辑保存：对接后端sampleType/update接口
const update = () => {
  // 简单校验
  if (!data.form.name) {
    ElMessage.warning('样本类型名称不能为空！')
    return
  }
  // 核心修改：编辑时保留id，同时映射name→typeName
  const submitData = {
    id: data.form.id, // 编辑必须传id
    typeName: data.form.name, // 前端name → 后端typeName
    description: data.form.description || '' // 空值处理
  }
  request.put('/sampleType/update', submitData).then(res => {
    if (res.code === '200') {
      data.name = null // 清空搜索框
      data.pageNum = 1 // 回到第一页
      load()
      ElMessage.success('样本类型编辑成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg || '编辑失败')
    }
  }).catch(err => {
    ElMessage.error('编辑样本类型失败：' + (err.msg || err.message))
  })
}

// 弹窗保存逻辑
const save = () => {
  data.form.id ? update() : add()
}

// 单条删除：对接后端sampleType/delete/{id}接口
const handleDelete = (id) => {
  ElMessageBox.confirm(
      '删除后样本类型数据无法恢复，您确定删除吗?',
      '样本类型删除确认',
      { type: 'warning' }
  ).then(() => {
    request.delete(`/sampleType/delete/${id}`).then(res => {
      const responseData = res.data || res;
      if (responseData.code === '200') {
        load()
        ElMessage.success('样本类型删除成功')
      } else {
        ElMessage.error(responseData.msg || '删除失败')
      }
    }).catch(err => {
      const errorMsg = err.response?.data?.msg || err.message || '网络请求异常'
      ElMessage.error('删除样本类型失败：' + errorMsg)
    })
  }).catch(() => {})
}

// 批量删除（毕设加分项）
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
        selectedIds.value.map(id => request.delete(`/sampleType/delete/${id}`))
    ).then(() => {
      load()
      ElMessage.success('批量删除样本类型成功')
      selectedIds.value = []
    }).catch(err => {
      ElMessage.error('批量删除失败：' + (err.msg || err.message))
    })
  }).catch(() => {})
}

// 监听表格选中事件
const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
}

// 重置查询条件
const reset = () => {
  data.name = null
  data.pageNum = 1
  load()
}

// 初始化加载数据
load()
</script>

<style scoped>
.sample-type-page {
  padding: 10px;
}
.page-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #1989fa;
}
.el-table {
  --el-table-header-text-color: #333;
  --el-table-row-hover-bg-color: #e0edfd;
}
</style>