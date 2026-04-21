<template>
  <div class="card" style="width: 500px; padding: 20px">
    <div style="font-size: 20px; font-weight: bold; margin-bottom: 20px">修改密码</div>

    <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" show-password placeholder="请输入旧密码" />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" show-password placeholder="请输入新密码" />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" show-password placeholder="请再次输入新密码" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="updatePassword">确认修改</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { clearAuth } from '@/utils/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref()

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive({
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

const updatePassword = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await request.put('/updatePassword', {
        oldPassword: form.oldPassword,
        newPassword: form.newPassword,
        confirmPassword: form.confirmPassword
      })

      ElMessage.success('密码修改成功，请重新登录')
      clearAuth()
      router.push('/login')
    } catch (e) {
      console.error(e)
    }
  })
}

const resetForm = () => {
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
  formRef.value?.clearValidate()
}
</script>
