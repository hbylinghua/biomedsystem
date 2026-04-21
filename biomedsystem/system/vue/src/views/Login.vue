<template>
  <div class="login-container">
    <div class="login-box">
      <div
          style="
          font-weight: bold;
          font-size: 24px;
          text-align: center;
          margin-bottom: 30px;
          color: #1450aa;
        "
      >
        欢 迎 登 录
      </div>

      <el-form :model="form" ref="formRef" :rules="rules">
        <el-form-item prop="username">
          <el-input
              :prefix-icon="User"
              size="large"
              v-model="form.username"
              placeholder="请输入账号"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              :prefix-icon="Lock"
              size="large"
              v-model="form.password"
              placeholder="请输入密码"
              show-password
          />
        </el-form-item>

        <el-form-item prop="role">
          <el-select size="large" style="width: 100%" v-model="form.role">
            <el-option value="admin" label="管理员" />
            <el-option value="researcher" label="用户" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
              size="large"
              type="primary"
              style="width: 100%"
              @click="login"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div style="text-align: right">
        还没有账号？请 <router-link to="/register">注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { setToken, setUser } from '@/utils/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref()

const form = reactive({
  username: '',
  password: '',
  role: 'admin'
})

const rules = reactive({
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
})

const login = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const res = await request.post('/login', form)
      const loginData = res.data

      setToken(loginData.token)
      setUser({
        userId: loginData.userId,
        username: loginData.username,
        role: loginData.role
      })

      router.push('/')
    } catch (error) {
      console.error(error)
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('@/assets/imgs/sea.jpg');
  background-size: cover;
}

.login-box {
  width: 350px;
  padding: 50px 30px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.5);
}
</style>
