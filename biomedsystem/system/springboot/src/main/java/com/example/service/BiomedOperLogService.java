package com.example.service;

import com.example.entity.BiomedOperLog;
import com.example.mapper.BiomedOperLogMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BiomedOperLogService {

    @Resource
    private BiomedOperLogMapper biomedOperLogMapper;

    public void add(BiomedOperLog biomedOperLog) {
        biomedOperLog.setOperTime(LocalDateTime.now());
        biomedOperLogMapper.insert(biomedOperLog);
    }

    public void deleteById(Long id) {
        biomedOperLogMapper.deleteById(id);
    }

    public BiomedOperLog selectById(Long id) {
        return biomedOperLogMapper.selectById(id);
    }

    public List<BiomedOperLog> selectAll(BiomedOperLog biomedOperLog) {
        return biomedOperLogMapper.selectAll(biomedOperLog);
    }

    public PageInfo<BiomedOperLog> selectPage(BiomedOperLog biomedOperLog, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<BiomedOperLog> list = biomedOperLogMapper.selectAll(biomedOperLog);
        return PageInfo.of(list);
    }
}