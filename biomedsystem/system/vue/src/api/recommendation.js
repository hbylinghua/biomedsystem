import request from '@/utils/request'

export const getStorageRecommendation = (params) => {
    return request.get('/api/recommend/storageForSample', { params })
}

export const getPendingTaskRecommendation = (params = { limit: 5 }) => {
    return request.get('/api/recommend/pendingTasks', { params })
}

export const askSampleAssistant = (question) => {
    return request.get('/api/recommend/assistantAsk', {
        params: { question }
    })
}
