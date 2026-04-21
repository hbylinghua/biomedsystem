package com.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    Map<String, Object> selectOverview(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("queueName") String queueName,
            @Param("sampleTypeId") Long sampleTypeId,
            @Param("userId") Long userId,
            @Param("role") String role
    );

    List<Map<String, Object>> selectSampleTrend(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("queueName") String queueName,
            @Param("sampleTypeId") Long sampleTypeId,
            @Param("userId") Long userId,
            @Param("role") String role
    );

    List<Map<String, Object>> selectStatusDistribution(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("queueName") String queueName,
            @Param("sampleTypeId") Long sampleTypeId,
            @Param("userId") Long userId,
            @Param("role") String role
    );

    List<Map<String, Object>> selectSourceDistribution(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("queueName") String queueName,
            @Param("sampleTypeId") Long sampleTypeId,
            @Param("userId") Long userId,
            @Param("role") String role
    );

    List<Map<String, Object>> selectWarnings(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("queueName") String queueName,
            @Param("sampleTypeId") Long sampleTypeId,
            @Param("userId") Long userId,
            @Param("role") String role
    );
}