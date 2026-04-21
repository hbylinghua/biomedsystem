import {createRouter, createWebHistory} from 'vue-router'
import { getToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/views/Manager.vue'),
      redirect: '/home',
      children: [
        { path: 'sampleImport', component: () => import('@/views/manager/SampleImport.vue')},
        { path: 'person', component: () => import('@/views/manager/Person.vue')},
        { path: 'password', component: () => import('@/views/manager/Password.vue')},
        { path: 'home', component: () => import('@/views/manager/Home.vue')},
        { path: 'notice', component: () => import('@/views/manager/Notice.vue')},
        // 核心修改：把旧路径改成新路径，和菜单index匹配
        { path: 'sampleType', component: () => import('@/views/manager/SampleType.vue')},
        { path: 'sample', component: () => import('@/views/manager/Sample.vue')},
        { path: 'sampleStorage', component: () => import('@/views/manager/SampleStorage.vue')},
        { path: 'sysUser', component: () => import('@/views/manager/User.vue')},
        { path: 'sampleAnalysis', component: () => import('@/views/manager/SampleAnalysis.vue')},
        { path: 'biomedOperLog', component: () => import('@/views/manager/biomedOperLog.vue')},
        { path: 'sampleDetail/:id', component: () => import('@/views/manager/SampleDetail.vue') },
      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue')},
    { path: '/register', component: () => import('@/views/Register.vue')},
  ]
})

const whiteList = ['/login', '/register']
router.beforeEach((to, from, next) => {
  const token = getToken()

  if (token) {
    if (to.path === '/login') {
      next('/')
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next('/login')
    }
  }
})



export default router