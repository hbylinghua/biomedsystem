package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 生物医学样本核心实体
 */
public class BiomedSample {
    private Long id;
    private String sampleNo;          // 样本编号（BIO20250001）
    private Long sampleTypeId;        // 样本类型ID（关联sample_type表）
    private String queue;             // 队列名称
    private String source;            // 样本来源
    private Long storageId;           // 存储ID（关联biomed_storage表）
    private Long createBy;
    private Integer status;     //样本状态
    private String SampleName;//样本名称


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime; // 录入时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime; // 修改时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime collectTime;

    public String getSampleName() {
        return SampleName;
    }

    public void setSampleName(String sampleName) {
        SampleName = sampleName;
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSampleNo() { return sampleNo; }
    public void setSampleNo(String sampleNo) { this.sampleNo = sampleNo; }

    public Long getSampleTypeId() { return sampleTypeId; }
    public void setSampleTypeId(Long sampleTypeId) { this.sampleTypeId = sampleTypeId; }

    public String getQueue() { return queue; }
    public void setQueue(String queue) { this.queue = queue; }

    public LocalDateTime getCollectTime() { return collectTime; }
    public void setCollectTime(LocalDateTime collectTime) { this.collectTime = collectTime; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Long getStorageId() { return storageId; }
    public void setStorageId(Long storageId) { this.storageId = storageId; }

    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}