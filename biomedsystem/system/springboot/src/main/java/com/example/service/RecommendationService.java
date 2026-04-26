package com.example.service;

import com.example.vo.PendingTaskRecommendationVO;
import com.example.vo.StorageRecommendationVO;

import java.util.List;
import java.util.Map;

public interface RecommendationService {

    List<StorageRecommendationVO> recommendStorage(
            Long sampleTypeId,
            String queueName,
            String source,
            Long userId,
            String role
    );

    List<PendingTaskRecommendationVO> recommendPendingTasks(
            Integer limit,
            Long userId,
            String role
    );

    Map<String, Object> assistantAsk(
            String question,
            Long userId,
            String role
    );
}
