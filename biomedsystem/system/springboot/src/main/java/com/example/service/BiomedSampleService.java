package com.example.service;

import com.example.entity.BiomedSample;
import com.example.entity.SampleType;
import com.example.mapper.BiomedSampleMapper;
import com.example.mapper.SampleTypeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BiomedSampleService {

    @Resource
    private BiomedSampleMapper biomedSampleMapper;


    private SampleTypeMapper sampleTypeMapper;

    // 根据样本类型ID获取名称（给导出用）
    public String getSampleTypeName(Long typeId) {
        if (typeId == null) return "未知类型";
        SampleType type = sampleTypeMapper.selectById(typeId);
        return type == null ? "未知类型" : type.getTypeName();
    }

    /**
     * 新增样本（自动生成唯一样本编号 + 填充时间）
     */
    public void add(BiomedSample biomedSample) {
        String prefix = "BIO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String suffix = String.format("%06d", System.currentTimeMillis() % 1000000);
        biomedSample.setSampleNo(prefix + suffix);

        if (biomedSample.getStatus() == null) {
            biomedSample.setStatus(0); // 默认状态，可按你的业务改
        }
        if (biomedSample.getCollectTime() == null) {
            biomedSample.setCollectTime(LocalDateTime.now());
        }

        biomedSample.setCreateTime(LocalDateTime.now());
        biomedSample.setUpdateTime(LocalDateTime.now());
        biomedSampleMapper.insert(biomedSample);
    }


    // 以下方法不变（deleteById/updateById/selectById/selectAll/selectPage）
    public void deleteById(Long id) {
        biomedSampleMapper.deleteById(id);
    }

    public void updateById(BiomedSample biomedSample) {
        biomedSample.setUpdateTime(LocalDateTime.now());
        biomedSampleMapper.updateById(biomedSample);
    }

    public BiomedSample selectById(Long id) {
        return biomedSampleMapper.selectById(id);
    }

    public List<BiomedSample> selectAll(BiomedSample biomedSample) {
        return biomedSampleMapper.selectAll(biomedSample);
    }

    public PageInfo<BiomedSample> selectPage(BiomedSample biomedSample, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<BiomedSample> list = biomedSampleMapper.selectAll(biomedSample);
        return PageInfo.of(list);
    }

    public void saveBatch(List<BiomedSample> sampleList) {
        // 批量插入数据库（不改动你原有任何方法）
        biomedSampleMapper.insertBatch(sampleList);
    }

}