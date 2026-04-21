<template>
  <div class="admin-page">
    <!-- 页面标题 -->
    <div class="page-title">信息管理</div>

    <div class="card" style="margin-bottom: 5px;">
      <el-input
          v-model="data.name"
          style="width: 300px; margin-right: 10px"
          placeholder="请输入名称查询"
          clearable
      ></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增人员</el-button>
      </div>
      <!-- 增加loading状态，接口请求时显示加载中 -->
      <el-table :data="data.tableData" stripe v-loading="loading" border>
        <el-table-column label="用户名" prop="username" min-width="70"></el-table-column>
        <el-table-column label="头像" width="120">
          <template #default="scope">
            <!-- 头像兜底：有值显示图片，无值显示默认图标 -->
            <div v-if="scope.row.avatar" style="width: 40px; height: 40px; border-radius: 50%; overflow: hidden;">
              <el-image
                  :src="scope.row.avatar"
                  style="width: 100%; height: 100%"
                  fit="cover"
                  :error="() => scope.row.avatar = ''"
              ></el-image>
            </div>
            <el-icon v-else style="width: 40px; height: 40px; line-height: 40px; text-align: center; color: #999; background: #f5f5f5; border-radius: 50%">
              <User />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column label="角色" prop="role" width="100">
          <template #default="scope">
            <!-- 匹配数据库角色值，增加样式和兜底 -->
            <el-tag type="danger" size="small" v-if="scope.row.role === 'admin'">管理员</el-tag>
            <el-tag type="success" size="small" v-else-if="scope.row.role === 'researcher'">研究员</el-tag>
            <el-tag type="info" size="small" v-else>未知角色</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件：适配全量查询的total -->
    <div class="card" v-if="data.total > 0">
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

    <el-dialog title="管理员信息" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="头像" prop="avatar">
          <el-upload
              :action="uploadUrl"
              list-type="picture"
              :on-success="handleImgSuccess"
              :show-file-list="false"
          >
            <!-- 预览已上传的头像 -->
            <div v-if="data.form.avatar">
              <el-image :src="data.form.avatar" style="width: 80px; height: 80px; margin-bottom: 10px" fit="cover"></el-image>
            </div>
            <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model="data.form.username" autocomplete="off" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="data.form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="admin"></el-option>
            <el-option label="研究员" value="researcher"></el-option>
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
import { reactive, ref, onMounted, computed } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import { User } from '@element-plus/icons-vue';

const loading = ref(false);
// 删掉头像上传地址：const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload';

const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
const currentUserRole = currentUser.role || 'researcher';
const currentUserId = currentUser.id || '';

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null // 保留但不用，或直接删掉
});

const tableDataToShow = computed(() => {
  if (['super_admin', 'admin'].includes(currentUserRole)) {
    return data.tableData;
  }
  return data.tableData.filter(item => item.id === currentUserId);
});

const load = () => {
  loading.value = true;
  // 删掉name参数：const queryParams = { name: data.name };
  const queryParams = {}; // 无搜索条件
  if (currentUserRole === 'researcher') {
    queryParams.id = currentUserId; // 普通用户仅查自己
  }

  request.get('/user/selectAll', { params: queryParams })
      .then(res => {
        if (res.code === '200') {
          data.tableData = res.data || [];
          data.total = res.data ? res.data.length : 0;
        } else {
          ElMessage.warning(res.msg || '暂无数据');
          data.tableData = [];
          data.total = 0;
        }
      })
      .catch(err => {
        ElMessage.error('查询失败：' + (err.msg || err.message || '网络异常'));
        data.tableData = [];
        data.total = 0;
      })
      .finally(() => {
        loading.value = false;
      });
};

const canEdit = (row) => {
  if (row.id === currentUserId) return false;
  if (currentUserRole === 'super_admin') {
    return row.role !== 'super_admin';
  }
  if (currentUserRole === 'admin') {
    return row.role === 'researcher';
  }
  return false;
};

const canDelete = (row) => {
  if (row.id === currentUserId) return false;
  if (currentUserRole === 'super_admin') {
    return row.role !== 'super_admin';
  }
  if (currentUserRole === 'admin') {
    return row.role === 'researcher';
  }
  return false;
};

const canChangeRole = () => {
  return ['super_admin', 'admin'].includes(currentUserRole);
};

const handleAdd = () => {
  const defaultRole = currentUserRole === 'super_admin' ? 'admin' : 'researcher';
  data.form = { role: defaultRole };
  data.formVisible = true;
};

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  data.formVisible = true;
};

const add = () => {
  if (!data.form.username) return ElMessage.warning('请输入账号！');
  if (!data.form.role) return ElMessage.warning('请选择角色！');

  if (currentUserRole === 'admin' && data.form.role !== 'researcher') {
    return ElMessage.error('普通管理员仅可新增普通用户！');
  }

  request.post('/user/add', data.form)
      .then(res => {
        if (res.code === '200') {
          load();
          ElMessage.success('新增成功');
          data.formVisible = false;
        } else {
          ElMessage.error(res.msg || '新增失败');
        }
      })
      .catch(err => ElMessage.error('新增失败：' + (err.msg || err.message)));
};

const update = () => {
  if (!data.form.username) return ElMessage.warning('请输入账号！');

  if (currentUserRole === 'admin' && data.form.role !== 'researcher') {
    return ElMessage.error('普通管理员仅可编辑普通用户！');
  }

  request.put('/user/update', data.form)
      .then(res => {
        if (res.code === '200') {
          load();
          ElMessage.success('编辑成功');
          data.formVisible = false;
        } else {
          ElMessage.error(res.msg || '编辑失败');
        }
      })
      .catch(err => ElMessage.error('编辑失败：' + (err.msg || err.message)));
};

const save = () => {
  data.form.id ? update() : add();
};

const handleDelete = (id) => {
  if (!id) return ElMessage.warning('无效的ID！');

  ElMessageBox.confirm(
      '删除后数据无法恢复，您确定删除吗?',
      '删除确认',
      { type: 'warning' }
  ).then(() => {
    request.delete(`/user/delete/${id}`)
        .then(res => {
          if (res.code === '200') {
            load();
            ElMessage.success('删除成功');
          } else {
            ElMessage.error(res.msg || '删除失败');
          }
        })
        .catch(err => ElMessage.error('删除失败：' + (err.msg || err.message)));
  }).catch(() => ElMessage.info('已取消删除'));
};

const reset = () => {
  data.name = null;
  data.pageNum = 1;
  load();
};

// 删掉头像上传方法：const handleImgSuccess = (res) => { ... };

onMounted(() => load());
</script>

<style scoped>
.admin-page {
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

.dialog-footer {
  text-align: right;
}

.el-tag {
  font-size: 12px;
}
</style>