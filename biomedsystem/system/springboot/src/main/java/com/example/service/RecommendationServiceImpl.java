package com.example.service;

import com.example.entity.BiomedSample;
import com.example.entity.BiomedStorage;
import com.example.entity.SampleType;
import com.example.mapper.RecommendationMapper;
import com.example.mapper.SampleTypeMapper;
import com.example.vo.PendingTaskRecommendationVO;
import com.example.vo.StorageRecommendationVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Resource
    private RecommendationMapper recommendationMapper;
    @Resource
    private SampleTypeMapper sampleTypeMapper;

    @Override
    public List<StorageRecommendationVO> recommendStorage(Long sampleTypeId, String queueName, String source,
                                                          Long userId, String role) {
        List<BiomedStorage> candidates = recommendationMapper.selectCandidateStorages();
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        String preferredTemp = sampleTypeId == null ? null : recommendationMapper.selectPreferredTempBySampleType(sampleTypeId, userId, role);
        if (preferredTemp == null || preferredTemp.isBlank()) preferredTemp = "-20℃";

        List<StorageRecommendationVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (BiomedStorage storage : candidates) {
            int score = 0;
            List<String> reasons = new ArrayList<>();

            if (preferredTemp.equals(storage.getTemp())) {
                score += 35;
                reasons.add("历史同类型样本常用温度匹配");
            } else if (storage.getTemp() != null && !storage.getTemp().isBlank()) {
                score += 10;
                reasons.add("该存储记录已配置温度");
            }

            if (storage.getStatus() == null || "在库".equals(storage.getStatus()) || "可用".equals(storage.getStatus()) || "使用".equals(storage.getStatus())) {
                score += 20;
                reasons.add("当前存储记录状态可用");
            }

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

            Integer matchCount = recommendationMapper.countHistoryMatch(storage.getId(), sampleTypeId, queueName, source, userId, role);
            int historyScore = Math.min((matchCount == null ? 0 : matchCount) * 5, 30);
            score += historyScore;
            if (matchCount != null && matchCount > 0) reasons.add("与历史存储使用习惯匹配");

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
        List<BiomedSample> samples = recommendationMapper.selectAllSamplesByScope(userId, role);
        List<PendingTaskRecommendationVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (BiomedSample sample : samples) {
            int score = 0;
            List<String> reasonList = new ArrayList<>();
            String action = "继续观察";

            if (sample.getStatus() != null && sample.getStatus() == 2) {
                score += 40;
                reasonList.add("样本状态异常");
                action = "优先复核";
            }
            if (sample.getStorageId() == null) {
                score += 25;
                reasonList.add("尚未关联存储记录");
                action = "优先入库";
            }
            if (sample.getCollectTime() != null) {
                long hours = Duration.between(sample.getCollectTime(), now).toHours();
                if (hours >= 24) {
                    score += 20;
                    reasonList.add("采集后超过24小时未处理");
                    if ("继续观察".equals(action)) action = "尽快处理";
                }
                if (hours >= 72) {
                    score += 10;
                    reasonList.add("采集后超过72小时");
                    if ("继续观察".equals(action)) action = "高优先处理";
                }
            }
            if (sample.getStatus() != null && sample.getStatus() == 0) {
                score += 15;
                reasonList.add("当前处于待处理状态");
                if ("继续观察".equals(action)) action = "优先处理";
            }
            if (score < 20) continue;

            PendingTaskRecommendationVO vo = new PendingTaskRecommendationVO();
            vo.setSampleId(sample.getId());
            vo.setSampleNo(sample.getSampleNo());
            vo.setSampleName(sample.getSampleName() == null || sample.getSampleName().isBlank() ? "样本-" + sample.getSampleNo() : sample.getSampleName());
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

    @Override
    public Map<String, Object> assistantAsk(String question, Long userId, String role) {
        String q = question == null ? "" : question.trim();
        Map<String, Object> result = new LinkedHashMap<>();
        List<BiomedSample> samples = recommendationMapper.selectAllSamplesByScope(userId, role);
        result.put("question", q);
        result.put("suggestions", List.of("最近7天新增了多少样本？", "未入库样本有哪些？", "异常样本有多少？", "哪个样本类型最多？", "哪个来源最多？", "哪个队列样本最多？"));

        if (q.isBlank()) {
            result.put("answer", "请输入问题，例如：最近7天新增了多少样本？未入库样本有哪些？异常样本有多少？");
            result.put("relatedSamples", Collections.emptyList());
            return result;
        }

        String normalized = q.replace("？", "").replace("?", "");
        LocalDateTime now = LocalDateTime.now();

        if (normalized.contains("未入库")) {
            List<BiomedSample> target = samples.stream().filter(s -> s.getStorageId() == null).toList();
            result.put("answer", "当前未入库样本共 " + target.size() + " 条。给样本绑定 storageId 后，这个预警就会消失。");
            result.put("relatedSamples", buildRelatedSamples(target, 10));
            return result;
        }
        if (normalized.contains("异常")) {
            List<BiomedSample> target = samples.stream().filter(s -> s.getStatus() != null && s.getStatus() == 2).toList();
            result.put("answer", "当前异常样本共 " + target.size() + " 条。下面列出最近的部分异常样本。");
            result.put("relatedSamples", buildRelatedSamples(target, 10));
            return result;
        }
        if (normalized.contains("待处理")) {
            List<BiomedSample> target = samples.stream().filter(s -> s.getStatus() != null && s.getStatus() == 0).toList();
            result.put("answer", "当前待处理样本共 " + target.size() + " 条。下面列出最近的部分待处理样本。");
            result.put("relatedSamples", buildRelatedSamples(target, 10));
            return result;
        }
        if (normalized.contains("最近7天") || normalized.contains("近7天")) {
            List<BiomedSample> target = samples.stream().filter(s -> s.getCollectTime() != null && s.getCollectTime().isAfter(now.minusDays(7))).toList();
            result.put("answer", "最近7天新增/采集的样本共 " + target.size() + " 条。下面列出最近的部分样本。");
            result.put("relatedSamples", buildRelatedSamples(target, 10));
            return result;
        }
        if (normalized.contains("类型") && (normalized.contains("最多") || normalized.contains("哪个"))) {
            Map<Long, Long> counter = samples.stream().filter(s -> s.getSampleTypeId() != null).collect(Collectors.groupingBy(BiomedSample::getSampleTypeId, Collectors.counting()));
            if (counter.isEmpty()) {
                result.put("answer", "当前没有可用于统计的样本类型数据。");
                result.put("relatedSamples", Collections.emptyList());
                return result;
            }
            Long typeId = counter.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
            String typeName = resolveTypeName(typeId);
            long count = counter.getOrDefault(typeId, 0L);
            List<BiomedSample> target = samples.stream().filter(s -> Objects.equals(s.getSampleTypeId(), typeId)).toList();
            result.put("answer", "当前数量最多的样本类型是“" + typeName + "”，共 " + count + " 条。下面列出该类型的部分样本。");
            result.put("relatedSamples", buildRelatedSamples(target, 10));
            return result;
        }
        if (normalized.contains("来源") && (normalized.contains("最多") || normalized.contains("哪个"))) {
            String source = getTopText(samples.stream().map(BiomedSample::getSource).filter(Objects::nonNull).filter(s -> !s.isBlank()).toList());
            long count = samples.stream().filter(s -> Objects.equals(s.getSource(), source)).count();
            List<BiomedSample> target = samples.stream().filter(s -> Objects.equals(s.getSource(), source)).toList();
            result.put("answer", source == null ? "当前没有可用于统计的来源信息。" : "当前数量最多的样本来源是“" + source + "”，共 " + count + " 条。下面列出部分样本。");
            result.put("relatedSamples", buildRelatedSamples(target, 10));
            return result;
        }
        if (normalized.contains("队列") && (normalized.contains("最多") || normalized.contains("哪个"))) {
            String queue = getTopText(samples.stream().map(BiomedSample::getQueue).filter(Objects::nonNull).filter(s -> !s.isBlank()).toList());
            long count = samples.stream().filter(s -> Objects.equals(s.getQueue(), queue)).count();
            List<BiomedSample> target = samples.stream().filter(s -> Objects.equals(s.getQueue(), queue)).toList();
            result.put("answer", queue == null ? "当前没有可用于统计的队列信息。" : "当前样本最多的队列是“" + queue + "”，共 " + count + " 条。下面列出部分样本。");
            result.put("relatedSamples", buildRelatedSamples(target, 10));
            return result;
        }
        if (normalized.contains("总数") || normalized.contains("多少样本") || normalized.equals("样本有多少")) {
            result.put("answer", "当前样本总量为 " + samples.size() + " 条。你还可以继续追问：未入库样本有哪些？异常样本有多少？哪个样本类型最多？");
            result.put("relatedSamples", Collections.emptyList());
            return result;
        }

        String bioToken = extractBioCode(normalized);
        if (bioToken != null) {
            List<BiomedSample> target = samples.stream().filter(s -> s.getSampleNo() != null && s.getSampleNo().contains(bioToken)).toList();
            result.put("answer", target.isEmpty() ? "没有找到编号包含“" + bioToken + "”的样本。" : "已找到编号包含“" + bioToken + "”的样本如下。");
            result.put("relatedSamples", buildRelatedSamples(target, 10));
            return result;
        }

        List<BiomedSample> fuzzy = samples.stream().filter(s -> containsIgnoreCase(s.getSampleName(), normalized) || containsIgnoreCase(s.getSource(), normalized) || containsIgnoreCase(s.getSampleNo(), normalized)).toList();
        if (!fuzzy.isEmpty()) {
            result.put("answer", "已为你找到与问题关键词相关的样本。你也可以继续问：这个样本是否未入库？该类型样本有多少？");
            result.put("relatedSamples", buildRelatedSamples(fuzzy, 10));
            return result;
        }

        result.put("answer", "我暂时没有精确理解这个问题。你可以换一种问法，例如：未入库样本有哪些？最近7天新增了多少样本？哪个样本类型最多？");
        result.put("relatedSamples", Collections.emptyList());
        return result;
    }

    private String resolveTypeName(Long typeId) {
        if (typeId == null) return "未知类型";
        SampleType type = sampleTypeMapper.selectById(typeId);
        return type == null ? "未知类型" : type.getTypeName();
    }
    private String getTopText(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        Map<String, Long> counter = list.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        return counter.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }
    private String extractBioCode(String text) {
        int idx = text.indexOf("BIO");
        if (idx < 0) return null;
        int end = idx;
        while (end < text.length()) {
            char c = text.charAt(end);
            if (Character.isLetterOrDigit(c)) end++; else break;
        }
        return text.substring(idx, end);
    }
    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && keyword != null && source.toLowerCase().contains(keyword.toLowerCase());
    }
    private List<Map<String, Object>> buildRelatedSamples(List<BiomedSample> list, int limit) {
        return list.stream().limit(limit).map(sample -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", sample.getId());
            item.put("sampleNo", sample.getSampleNo());
            item.put("sampleName", sample.getSampleName() == null || sample.getSampleName().isBlank() ? "样本-" + sample.getSampleNo() : sample.getSampleName());
            item.put("source", sample.getSource());
            item.put("queue", sample.getQueue());
            item.put("status", sample.getStatus());
            item.put("collectTime", sample.getCollectTime());
            item.put("storageId", sample.getStorageId());
            return item;
        }).toList();
    }
}
