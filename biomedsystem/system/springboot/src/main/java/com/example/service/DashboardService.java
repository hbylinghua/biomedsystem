package com.example.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    Map<String, Object> getOverview(
            String startDate, String endDate, String queueName, Long sampleTypeId,
            Long userId, String role
    );

    Map<String, Object> getSampleTrend(
            String startDate, String endDate, String queueName, Long sampleTypeId,
            Long userId, String role
    );

    Map<String, Object> getStatusDistribution(
            String startDate, String endDate, String queueName, Long sampleTypeId,
            Long userId, String role
    );

    Map<String, Object> getSourceDistribution(
            String startDate, String endDate, String queueName, Long sampleTypeId,
            Long userId, String role
    );

    List<Map<String, Object>> getWarnings(
            String startDate, String endDate, String queueName, Long sampleTypeId,
            Long userId, String role
    );
}