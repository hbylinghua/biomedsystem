package com.example.service;

import com.example.entity.BiomedStorage;
import com.example.mapper.BiomedStorageMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BiomedStorageService {

    @Resource
    private BiomedStorageMapper biomedStorageMapper;

    public void add(BiomedStorage biomedStorage) {
        biomedStorage.setCreateTime(LocalDateTime.now());
        biomedStorageMapper.insert(biomedStorage);
    }

    public void deleteById(Long id) {
        biomedStorageMapper.deleteById(id);
    }

    public void updateById(BiomedStorage biomedStorage) {
        biomedStorageMapper.updateById(biomedStorage);
    }

    public BiomedStorage selectById(Long id) {
        return biomedStorageMapper.selectById(id);
    }

    public List<BiomedStorage> selectAll(BiomedStorage biomedStorage) {
        return biomedStorageMapper.selectAll(biomedStorage);
    }

    public PageInfo<BiomedStorage> selectPage(BiomedStorage biomedStorage, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<BiomedStorage> list = biomedStorageMapper.selectAll(biomedStorage);
        return PageInfo.of(list);
    }
}