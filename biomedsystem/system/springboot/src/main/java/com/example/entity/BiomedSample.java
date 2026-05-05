package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class BiomedSample {
    private Long id;
    private String sampleNo;
    private Long sampleTypeId;
    private String queue;
    private String source;
    private Long storageId;
    private Long createBy;
    private Integer status;
    private String sampleName;

    /**
     * 前端/Excel 导入辅助字段：样本类型名称。
     * 数据库 biomed_sample 表中没有该字段，仅用于把“血液、组织”等名称解析为 sampleTypeId。
     */
    private String sampleTypeName;

    /**
     * 前端/Excel 导入辅助字段：存储位置。
     * 数据库 biomed_sample 表中没有该字段，仅用于自动创建或更新 biomed_storage 后回填 storageId。
     */
    private String storagePosition;

    /**
     * 前端/Excel 导入辅助字段：存储温度或补充说明。
     * 可为空，自动创建存储记录时写入 biomed_storage.temp。
     */
    private String storageTemp;

    /**
     * 前端/Excel 导入辅助字段：存储状态。
     * 可为空，自动创建存储记录时默认使用“在库”。
     */
    private String storageStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime collectTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime storageExpireTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSampleNo() { return sampleNo; }
    public void setSampleNo(String sampleNo) { this.sampleNo = sampleNo; }
    public Long getSampleTypeId() { return sampleTypeId; }
    public void setSampleTypeId(Long sampleTypeId) { this.sampleTypeId = sampleTypeId; }
    public String getQueue() { return queue; }
    public void setQueue(String queue) { this.queue = queue; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Long getStorageId() { return storageId; }
    public void setStorageId(Long storageId) { this.storageId = storageId; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getSampleName() { return sampleName; }
    public void setSampleName(String sampleName) { this.sampleName = sampleName; }
    public String getSampleTypeName() { return sampleTypeName; }
    public void setSampleTypeName(String sampleTypeName) { this.sampleTypeName = sampleTypeName; }
    public String getStoragePosition() { return storagePosition; }
    public void setStoragePosition(String storagePosition) { this.storagePosition = storagePosition; }
    public String getStorageTemp() { return storageTemp; }
    public void setStorageTemp(String storageTemp) { this.storageTemp = storageTemp; }
    public String getStorageStatus() { return storageStatus; }
    public void setStorageStatus(String storageStatus) { this.storageStatus = storageStatus; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public LocalDateTime getCollectTime() { return collectTime; }
    public void setCollectTime(LocalDateTime collectTime) { this.collectTime = collectTime; }
    public LocalDateTime getStorageExpireTime() { return storageExpireTime; }
    public void setStorageExpireTime(LocalDateTime storageExpireTime) { this.storageExpireTime = storageExpireTime; }
}
