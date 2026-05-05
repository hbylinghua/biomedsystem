<template>
  <div>
    <div
        style="
        height: 60px;
        background-color: #fff;
        display: flex;
        align-items: center;
        border-bottom: 1px solid #ddd;
      "
    >
      <div style="flex: 1">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px" />
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px">
            样本管理系统
          </div>
        </div>
      </div>

      <div style="padding-right: 20px">
        <div style="display: flex; align-items: center">
          <img
              style="width: 40px; height: 40px; border-radius: 50%"
              :src="getRealAvatar()"
              alt=""
          />
          <span style="margin-left: 5px">{{ data.user.username }}</span>
        </div>
      </div>
    </div>

    <div style="display: flex">
      <div
          style="
          width: 200px;
          border-right: 1px solid #ddd;
          min-height: calc(100vh - 60px);
        "
      >
        <el-menu
            router
            style="border: none"
            :default-active="route.path"
            :default-openeds="['1', '2']"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>系统首页</span>
          </el-menu-item>

          <el-menu-item index="/sample" v-if="isResearcher">
            <el-icon><Goods /></el-icon>
            <span>样本信息管理</span>
          </el-menu-item>

          <el-menu-item index="/sampleAnalysis" v-if="isResearcher">
            <el-icon><Tickets /></el-icon>
            <span>样本数据分析</span>
          </el-menu-item>

          <el-menu-item index="/sampleStorage" v-if="isResearcher">
            <el-icon><SoldOut /></el-icon>
            <span>样本存储管理</span>
          </el-menu-item>

          <el-menu-item index="/biomedOperLog" v-if="isAdmin">
            <el-icon><Tickets /></el-icon>
            <span>样本日志管理</span>
          </el-menu-item>

          <el-sub-menu index="1" v-if="isAdmin">
            <template #title>
              <el-icon><Menu /></el-icon>
              <span>样本管理</span>
            </template>

            <el-menu-item index="/sampleType">
              <el-icon><Menu /></el-icon>
              <span>样本类别管理</span>
            </el-menu-item>

            <el-menu-item index="/sample">
              <el-icon><Goods /></el-icon>
              <span>样本信息管理</span>
            </el-menu-item>

            <el-menu-item index="/sampleStorage">
              <el-icon><SoldOut /></el-icon>
              <span>样本存储管理</span>
            </el-menu-item>

            <el-menu-item index="/sampleAnalysis">
              <el-icon><Tickets /></el-icon>
              <span>样本数据分析</span>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/notice" v-if="isAdmin">
            <el-icon><Bell /></el-icon>
            <span>系统公告管理</span>
          </el-menu-item>

          <el-menu-item index="/sysUser" v-if="isAdmin">
            <el-icon><User /></el-icon>
            <span>用户信息</span>
          </el-menu-item>

          <el-menu-item index="/person">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>

          <el-menu-item index="/password">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </el-menu-item>

          <el-menu-item index="/login" @click="logout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出系统</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div
          style="
          flex: 1;
          width: 0;
          background-color: #f8f8ff;
          padding: 10px;
        "
      >
        <router-view @updateUser="updateUser" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUser, getToken, clearAuth } from '@/utils/auth'

const router = useRouter()
const route = useRoute()

const data = reactive({
  user: {
    userId: null,
    username: '',
    role: '',
    avatar: ''
  }
})

const isAdmin = computed(() => data.user.role === 'admin' || data.user.role === 'super_admin')
const isResearcher = computed(() => data.user.role === 'researcher')

const loadUser = () => {
  const user = getUser()
  const token = getToken()

  if (!token || !user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  data.user = {
    ...data.user,
    ...user
  }
}

// 实时获取最新头像
const getRealAvatar = () => {
  const user = JSON.parse(localStorage.getItem('system-user') || '{}')
  return user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
}

const updateUser = () => {
  loadUser()
}

const logout = () => {
  ElMessageBox.confirm('确定退出登录吗？', '退出确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
      .then(() => {
        clearAuth()
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
}

onMounted(() => {
  loadUser()
  // 👇 加这一段 —— 监听个人资料更新
  window.addEventListener('refreshUser', () => {
    loadUser()
  })

})
</script>

<style scoped>
.el-menu-item.is-active {
  background-color: #e0edfd !important;
}

.el-menu-item:hover {
  color: #1967e3;
}
</style>
