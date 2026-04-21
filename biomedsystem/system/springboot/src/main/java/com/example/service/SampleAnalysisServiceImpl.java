package com.example.service;

import com.example.mapper.BiomedSampleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SampleAnalysisServiceImpl implements SampleAnalysisService {

    @Autowired
    private BiomedSampleMapper sampleMapper;

    // ===================== 1. 队列样本占比分析 =====================
    @Override
    public Map<String, Object> queueRatioAnalysis(
            String queueName, Long sampleTypeId, String startDate, String endDate, Long userId, String role) {
        Map<String, Object> result = new HashMap<>();
        List<String> xAxis = new ArrayList<>();
        List<Integer> series = new ArrayList<>();
        result.put("xAxis", xAxis);
        result.put("series", series);

        try {
            Long createBy = "researcher".equals(role) ? userId : null;
            // 传入所有筛选条件！
            List<Map<String, Object>> dataList = sampleMapper.selectQueueRatio(
                    createBy, queueName, sampleTypeId, startDate, endDate
            );

            if (dataList == null || dataList.isEmpty()) return result;

            for (Map<String, Object> item : dataList) {
                Object queueObj = item.get("queue");
                if (queueObj == null || queueObj.toString().trim().isEmpty()) continue;

                Object countObj = item.get("count");
                int count = 0;
                if (countObj instanceof Number) {
                    count = ((Number) countObj).intValue();
                } else {
                    try {
                        count = Integer.parseInt(countObj.toString().trim());
                    } catch (NumberFormatException e) { count = 0; }
                }
                xAxis.add(queueObj.toString());
                series.add(count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // ===================== 2. 采集时间分布分析 =====================
    @Override
    public Map<String, Object> collectTimeDistAnalysis(
            String startDate, String endDate, String queueName, Long sampleTypeId, Long userId, String role) {
        Map<String, Object> result = new HashMap<>();
        List<String> xAxis = new ArrayList<>();
        List<Integer> series = new ArrayList<>();
        result.put("xAxis", xAxis);
        result.put("series", series);

        try {
            LocalDateTime start = null;
            LocalDateTime end = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDate.parse(startDate, formatter).atStartOfDay();
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDate.parse(endDate, formatter).atTime(23, 59, 59);
            }

            Long createBy = "researcher".equals(role) ? userId : null;
            // 传入所有筛选条件！
            List<Map<String, Object>> dataList = sampleMapper.selectCollectTimeDist(
                    start, end, createBy, queueName, sampleTypeId
            );

            if (dataList == null || dataList.isEmpty()) return result;

            for (Map<String, Object> item : dataList) {
                Object dateObj = item.get("collectDate");
                if (dateObj == null) continue;

                Object countObj = item.get("count");
                int count = 0;
                if (countObj instanceof Number) {
                    count = ((Number) countObj).intValue();
                } else {
                    try {
                        count = Integer.parseInt(countObj.toString().trim());
                    } catch (NumberFormatException e) { count = 0; }
                }
                xAxis.add(dateObj.toString());
                series.add(count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // ===================== 3. 样本类型数量分析 =====================
    @Override
    public Map<String, Object> sampleTypeCountAnalysis(
            String queueName, Long sampleTypeId, String startDate, String endDate, Long userId, String role) {
        Map<String, Object> result = new HashMap<>();
        List<String> xAxis = new ArrayList<>();
        List<Integer> series = new ArrayList<>();
        result.put("xAxis", xAxis);
        result.put("series", series);

        try {
            Long createBy = "researcher".equals(role) ? userId : null;
            // 传入所有筛选条件！
            List<Map<String, Object>> dataList = sampleMapper.selectSampleTypeCount(
                    createBy, queueName, sampleTypeId, startDate, endDate
            );

            if (dataList == null || dataList.isEmpty()) return result;

            for (Map<String, Object> item : dataList) {
                Object typeObj = item.get("typeName");
                if (typeObj == null) continue;

                Object countObj = item.get("count");
                int count = 0;
                if (countObj instanceof Number) {
                    count = ((Number) countObj).intValue();
                } else {
                    try {
                        count = Integer.parseInt(countObj.toString().trim());
                    } catch (NumberFormatException e) { count = 0; }
                }
                xAxis.add(typeObj.toString());
                series.add(count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}