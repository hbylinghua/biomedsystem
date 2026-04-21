package com.example.service;

import com.example.entity.SampleType;
import com.example.mapper.SampleTypeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SampleTypeService {

    @Resource
    private SampleTypeMapper sampleTypeMapper;

    public void add(SampleType sampleType) {
        sampleTypeMapper.insert(sampleType);
    }

    public void deleteById(Long id) {
        sampleTypeMapper.deleteById(id);
    }

    public void updateById(SampleType sampleType) {
        sampleTypeMapper.updateById(sampleType);
    }

    public SampleType selectById(Long id) {
        return sampleTypeMapper.selectById(id);
    }

    public List<SampleType> selectAll(SampleType sampleType) {
        return sampleTypeMapper.selectAll(sampleType);
    }

    public PageInfo<SampleType> selectPage(SampleType sampleType, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<SampleType> list = sampleTypeMapper.selectAll(sampleType);
        return PageInfo.of(list);
    }
}