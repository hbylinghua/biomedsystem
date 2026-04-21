package com.example.entity;

import java.time.LocalDateTime;

/**
 * 样本操作日志实体（对应biomed_oper_log表）
 */
public class BiomedOperLog {
    private Long id;                // 日志ID（主键）
    private Long sampleId;          // 样本ID（关联biomed_sample表）
    private String operType;         // 操作类型：录入/修改/删除/查询
    private Long operBy;             // 操作人ID（关联sys_user表）
    private LocalDateTime operTime; // 操作时间
    private String content;         // 操作内容：如"录入样本BIO20250001"

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public String getOperType() {
        return operType;
    }

    public void setOperType(String operType) {
        this.operType = operType;
    }

    public Long getOperBy() {
        return operBy;
    }

    public void setOperBy(Long operBy) {
        this.operBy = operBy;
    }

    public LocalDateTime getOperTime() {
        return operTime;
    }

    public void setOperTime(LocalDateTime operTime) {
        this.operTime = operTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}