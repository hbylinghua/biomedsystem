package com.example.service;

import java.util.Map;

public interface SampleAnalysisService {
    // 队列样本占比分析（增加筛选参数）
    Map<String, Object> queueRatioAnalysis(
            String queueName,
            Long sampleTypeId,
            String startDate,
            String endDate,
            Long userId,
            String role
    );

    // 采集时间分布分析（增加筛选参数）
    Map<String, Object> collectTimeDistAnalysis(
            String startDate,
            String endDate,
            String queueName,
            Long sampleTypeId,
            Long userId,
            String role
    );

    // 样本类型数量分析（增加筛选参数）
    Map<String, Object> sampleTypeCountAnalysis(
            String queueName,
            Long sampleTypeId,
            String startDate,
            String endDate,
            Long userId,
            String role
    );
}