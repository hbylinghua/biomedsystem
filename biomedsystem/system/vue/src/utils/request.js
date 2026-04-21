import { ElMessage } from 'element-plus' // 保留你原有的Element Plus提示
import router from '../router'
import axios from "axios";
import { getToken, clearAuth } from './auth'

const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL, // 变量名匹配你的环境变量
    timeout: 30000,
    withCredentials: true // 新增：解决跨域携带凭证问题（关键！）
})

// request 拦截器
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    const token = getToken()
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
    }

    return config
}, error => {
    ElMessage.error('请求发送失败：' + error.message) // 新增：请求发送失败提示
    return Promise.reject(error)
});

// response 拦截器（核心修改）
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件，直接返回
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }

        // 1. 权限失效处理（保留你原有逻辑）
        if (res.code === '401') {
            ElMessage.error(res.msg || '登录状态失效，请重新登录');
            clearAuth()
            router.push("/login")
            return Promise.reject(res)
        }

        // 2. 新增：业务失败提示（如密码错误）
        if (res.code !== '200') {
            ElMessage.error(res.msg || '操作失败');
            return Promise.reject(res)
        }

        // 3. 新增：业务成功提示（可选，登录页可单独处理）
        if (res.code === '200' && res.msg) {
            ElMessage.success(res.msg);
        }

        return res;
    },
    error => {
        // 新增：统一捕获网络/跨域/404错误
        if (error.message.includes('Network Error')) {
            ElMessage.error('网络异常，请检查后端是否启动！');
        } else if (error.response?.status === 404) {
            ElMessage.error('接口不存在：' + error.config.url);
        } else {
            ElMessage.error('请求失败：' + error.message);
        }
        console.log('err' + error)
        return Promise.reject(error)
    }
)

export default request