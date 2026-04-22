package com.example.service.impl;

import com.example.entity.BiomedSample;
import com.example.entity.BiomedStorage;
import com.example.mapper.RecommendationMapper;
import com.example.service.RecommendationService;
import com.example.vo.PendingTaskRecommendationVO;
import com.example.vo.StorageRecommendationVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Resource
    private RecommendationMapper recommendationMapper;

    @Override
    public List<StorageRecommendationVO> recommendStorage(Long sampleTypeId, String queueName, String source,
                                                          Long userId, String role) {
        List<BiomedStorage> candidates = recommendationMapper.selectCandidateStorages();
        String preferredTemp = recommendationMapper.selectPreferredTempBySampleType(sampleTypeId, userId, role);

        // 如果没有历史温度，给一个保守默认值
        if (preferredTemp == null || preferredTemp.isBlank()) {
            preferredTemp = "-20℃";
        }

        List<StorageRecommendationVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (BiomedStorage storage : candidates) {
            int score = 0;
            List<String> reasons = new ArrayList<>();

            // 1. 温度匹配
            if (preferredTemp.equals(storage.getTemp())) {
                score += 35;
                reasons.add("历史同类型样本常用温度匹配");
            }

            // 2. 存储状态正常
            if (storage.getStatus() == null
                    || "在库".equals(storage.getStatus())
                    || "可用".equals(storage.getStatus())
                    || "使用".equals(storage.getStatus())) {
                score += 20;
                reasons.add("当前存储记录状态可用");
            }

            // 3. 到期风险
            if (storage.getExpireTime() != null) {
                if (storage.getExpireTime().isAfter(now.plusDays(30))) {
                    score += 20;
                    reasons.add("到期风险低");
                } else if (storage.getExpireTime().isAfter(now.plusDays(7))) {
                    score += 10;
                    reasons.add("短期内无到期风险");
                } else {
                    reasons.add("到期时间较近，谨慎使用");
                }
            } else {
                score += 5;
                reasons.add("未设置到期时间，默认中性处理");
            }

            // 4. 历史匹配度
            Integer matchCount = recommendationMapper.countHistoryMatch(
                    storage.getId(), sampleTypeId, queueName, source, userId, role
            );
            int historyScore = Math.min((matchCount == null ? 0 : matchCount) * 5, 30);
            score += historyScore;
            if (matchCount != null && matchCount > 0) {
                reasons.add("与历史存储使用习惯匹配");
            }

            StorageRecommendationVO vo = new StorageRecommendationVO();
            vo.setStorageId(storage.getId());
            vo.setPosition(storage.getPosition());
            vo.setTemp(storage.getTemp());
            vo.setScore(score);
            vo.setReasons(reasons);
            result.add(vo);
        }

        result.sort(Comparator.comparing(StorageRecommendationVO::getScore).reversed());
        return result.stream().limit(3).toList();
    }

    @Override
    public List<PendingTaskRecommendationVO> recommendPendingTasks(Integer limit, Long userId, String role) {
        List<BiomedSample> samples = recommendationMapper.selectPendingSamples(userId, role);
        List<PendingTaskRecommendationVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (BiomedSample sample : samples) {
            int score = 0;
            List<String> reasonList = new ArrayList<>();
            String action = "继续观察";

            // 1. 异常样本优先
            if (sample.getStatus() != null && sample.getStatus() == 2) {
                score += 40;
                reasonList.add("样本状态异常");
                action = "优先复核";
            }

            // 2. 未入库样本优先
            if (sample.getStorageId() == null) {
                score += 25;
                reasonList.add("尚未关联存储位置");
                action = "优先入库";
            }

            // 3. 采集后长时间未处理
            if (sample.getCollectTime() != null) {
                long hours = Duration.between(sample.getCollectTime(), now).toHours();
                if (hours >= 24) {
                    score += 20;
                    reasonList.add("采集后超过24小时未处理");
                    if ("继续观察".equals(action)) {
                        action = "尽快处理";
                    }
                }
                if (hours >= 72) {
                    score += 10;
                    reasonList.add("采集后超过72小时");
                    if ("继续观察".equals(action)) {
                        action = "高优先处理";
                    }
                }
            }

            // 4. 状态为待处理
            if (sample.getStatus() != null && sample.getStatus() == 0) {
                score += 15;
                reasonList.add("当前处于待处理状态");
                if ("继续观察".equals(action)) {
                    action = "优先处理";
                }
            }

            // 分数太低的不返回，避免页面信息噪声太多
            if (score < 20) {
                continue;
            }

            PendingTaskRecommendationVO vo = new PendingTaskRecommendationVO();
            vo.setSampleId(sample.getId());
            vo.setSampleNo(sample.getSampleNo());
            vo.setSampleName(sample.getSampleName());
            vo.setPriorityScore(score);
            vo.setRecommendAction(action);
            vo.setReason(String.join("；", reasonList));
            vo.setCollectTime(sample.getCollectTime());
            result.add(vo);
        }

        result.sort(Comparator.comparing(PendingTaskRecommendationVO::getPriorityScore).reversed());

        int realLimit = (limit == null || limit <= 0) ? 5 : limit;
        return result.stream().limit(realLimit).toList();
    }
}