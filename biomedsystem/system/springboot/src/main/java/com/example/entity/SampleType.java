package com.example.entity;

import java.time.LocalDateTime;

/**
 * 样本类型实体（完全匹配数据库表结构）
 */
public class SampleType {
    private Long id;
    private String typeName;  // 对应数据库 type_name
    private LocalDateTime createTime;

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}