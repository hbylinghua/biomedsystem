import { ElMessage } from 'element-plus'
import router from '../router'
import axios from 'axios'
import { getToken, clearAuth } from './auth'

const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL,
    timeout: 30000,
    withCredentials: true
})

request.interceptors.request.use(
    config => {
        config.headers['Content-Type'] = 'application/json;charset=utf-8'
        const token = getToken()
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => {
        ElMessage.error('请求发送失败：' + error.message)
        return Promise.reject(error)
    }
)

request.interceptors.response.use(
    response => {
        if (response.config.responseType === 'blob') {
            return response.data
        }

        let res = response.data
        if (typeof res === 'string') {
            try {
                res = res ? JSON.parse(res) : res
            } catch (e) {
                return res
            }
        }

        if (res?.code === '401' || res?.code === 401) {
            ElMessage.error(res.msg || '登录状态失效，请重新登录')
            clearAuth()
            router.push('/login')
            return Promise.reject(res)
        }

        if (res?.code !== '200' && res?.code !== 200) {
            ElMessage.error(res.msg || '操作失败')
            return Promise.reject(res)
        }

        return res
    },
    error => {
        if (error.message?.includes('Network Error')) {
            ElMessage.error('网络异常，请检查后端是否启动')
        } else if (error.response?.status === 404) {
            ElMessage.error('接口不存在：' + error.config.url)
        } else {
            ElMessage.error('请求失败：' + error.message)
        }
        return Promise.reject(error)
    }
)

export default request
