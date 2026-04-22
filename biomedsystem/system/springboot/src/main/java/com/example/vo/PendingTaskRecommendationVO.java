package com.example.vo;

import java.time.LocalDateTime;

public class PendingTaskRecommendationVO {
    private Long sampleId;
    private String sampleNo;
    private String sampleName;
    private Integer priorityScore;
    private String recommendAction;
    private String reason;
    private LocalDateTime collectTime;

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleNo() {
        return sampleNo;
    }

    public void setSampleNo(String sampleNo) {
        this.sampleNo = sampleNo;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public Integer getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(Integer priorityScore) {
        this.priorityScore = priorityScore;
    }

    public String getRecommendAction() {
        return recommendAction;
    }

    public void setRecommendAction(String recommendAction) {
        this.recommendAction = recommendAction;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(LocalDateTime collectTime) {
        this.collectTime = collectTime;
    }
}