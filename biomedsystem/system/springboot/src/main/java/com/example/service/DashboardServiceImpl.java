package com.example.service;

import com.example.mapper.DashboardMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private DashboardMapper dashboardMapper;

    @Override
    public Map<String, Object> getOverview(String startDate, String endDate, String queueName,
                                           Long sampleTypeId, Long userId, String role) {
        return dashboardMapper.selectOverview(startDate, endDate, queueName, sampleTypeId, userId, role);
    }

    @Override
    public Map<String, Object> getSampleTrend(String startDate, String endDate, String queueName,
                                              Long sampleTypeId, Long userId, String role) {
        List<Map<String, Object>> list = dashboardMapper.selectSampleTrend(
                startDate, endDate, queueName, sampleTypeId, userId, role
        );
        return convertToChartData(list);
    }

    @Override
    public Map<String, Object> getStatusDistribution(String startDate, String endDate, String queueName,
                                                     Long sampleTypeId, Long userId, String role) {
        List<Map<String, Object>> list = dashboardMapper.selectStatusDistribution(
                startDate, endDate, queueName, sampleTypeId, userId, role
        );
        return convertToChartData(list);
    }

    @Override
    public Map<String, Object> getSourceDistribution(String startDate, String endDate, String queueName,
                                                     Long sampleTypeId, Long userId, String role) {
        List<Map<String, Object>> list = dashboardMapper.selectSourceDistribution(
                startDate, endDate, queueName, sampleTypeId, userId, role
        );
        return convertToChartData(list);
    }

    @Override
    public List<Map<String, Object>> getWarnings(String startDate, String endDate, String queueName,
                                                 Long sampleTypeId, Long userId, String role) {
        return dashboardMapper.selectWarnings(startDate, endDate, queueName, sampleTypeId, userId, role);
    }

    private Map<String, Object> convertToChartData(List<Map<String, Object>> list) {
        Map<String, Object> result = new HashMap<>();
        result.put("xAxis", list.stream().map(m -> String.valueOf(m.get("xName"))).toList());
        result.put("series", list.stream().map(m -> Integer.parseInt(String.valueOf(m.get("yValue")))).toList());
        return result;
    }
}