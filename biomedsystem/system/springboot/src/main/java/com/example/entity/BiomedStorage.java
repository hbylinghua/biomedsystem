package com.example.entity;

import java.time.LocalDateTime;

/**
 * 样本存储实体（对应biomed_storage表）
 */
public class BiomedStorage {
    private Long id;                // 存储ID（主键）
    private String position;        // 存储位置（冷库A-01/液氮罐B-02等）
    private String temp;            // 存储温度（-80℃/-20℃/4℃等）
    private LocalDateTime expireTime; // 保质期
    private String status;          // 存储状态（在库/使用/过期/销毁）
    private LocalDateTime createTime;

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    private String sampleName;// 创建时间

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}