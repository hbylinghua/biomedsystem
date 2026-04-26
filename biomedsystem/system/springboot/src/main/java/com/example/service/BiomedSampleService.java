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

    @Resource
    private SampleTypeMapper sampleTypeMapper;

    public String getSampleTypeName(Long typeId) {
        if (typeId == null) return "未知类型";
        SampleType type = sampleTypeMapper.selectById(typeId);
        return type == null ? "未知类型" : type.getTypeName();
    }

    public void add(BiomedSample biomedSample) {
        String prefix = "BIO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String suffix = String.format("%06d", System.currentTimeMillis() % 1000000);
        biomedSample.setSampleNo(prefix + suffix);
        if (biomedSample.getStatus() == null) biomedSample.setStatus(0);
        if (biomedSample.getCollectTime() == null) biomedSample.setCollectTime(LocalDateTime.now());
        if (biomedSample.getSampleName() == null || biomedSample.getSampleName().isBlank()) {
            biomedSample.setSampleName("样本-" + biomedSample.getSampleNo());
        }
        biomedSample.setCreateTime(LocalDateTime.now());
        biomedSample.setUpdateTime(LocalDateTime.now());
        biomedSampleMapper.insert(biomedSample);
    }

    public void deleteById(Long id) { biomedSampleMapper.deleteById(id); }

    public void updateById(BiomedSample biomedSample) {
        if (biomedSample.getSampleName() == null || biomedSample.getSampleName().isBlank()) {
            BiomedSample old = biomedSampleMapper.selectById(biomedSample.getId());
            if (old != null && old.getSampleName() != null && !old.getSampleName().isBlank()) {
                biomedSample.setSampleName(old.getSampleName());
            } else if (old != null && old.getSampleNo() != null) {
                biomedSample.setSampleName("样本-" + old.getSampleNo());
            }
        }
        biomedSample.setUpdateTime(LocalDateTime.now());
        biomedSampleMapper.updateById(biomedSample);
    }

    public BiomedSample selectById(Long id) { return biomedSampleMapper.selectById(id); }
    public List<BiomedSample> selectAll(BiomedSample biomedSample) { return biomedSampleMapper.selectAll(biomedSample); }

    public PageInfo<BiomedSample> selectPage(BiomedSample biomedSample, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<BiomedSample> list = biomedSampleMapper.selectAll(biomedSample);
        return PageInfo.of(list);
    }

    public void saveBatch(List<BiomedSample> sampleList) { biomedSampleMapper.insertBatch(sampleList); }
}
