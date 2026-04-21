import request from '@/utils/request'

export const getDashboardOverview = (params) =>
    request.get('/api/dashboard/overview', { params })

export const getDashboardTrend = (params) =>
    request.get('/api/dashboard/sampleTrend', { params })

export const getDashboardStatus = (params) =>
    request.get('/api/dashboard/statusDistribution', { params })

export const getDashboardSource = (params) =>
    request.get('/api/dashboard/sourceDistribution', { params })

export const getDashboardWarnings = (params) =>
    request.get('/api/dashboard/warnings', { params })