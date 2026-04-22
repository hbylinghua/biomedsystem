package com.example.mapper;

import com.example.entity.BiomedSample;
import com.example.entity.BiomedStorage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecommendationMapper {

    List<BiomedStorage> selectCandidateStorages();

    String selectPreferredTempBySampleType(
            @Param("sampleTypeId") Long sampleTypeId,
            @Param("userId") Long userId,
            @Param("role") String role
    );

    Integer countHistoryMatch(
            @Param("storageId") Long storageId,
            @Param("sampleTypeId") Long sampleTypeId,
            @Param("queueName") String queueName,
            @Param("source") String source,
            @Param("userId") Long userId,
            @Param("role") String role
    );

    List<BiomedSample> selectPendingSamples(
            @Param("userId") Long userId,
            @Param("role") String role
    );
}