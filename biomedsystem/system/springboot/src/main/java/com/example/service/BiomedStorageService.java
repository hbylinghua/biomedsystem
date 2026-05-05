package com.example.service;

import com.example.entity.BiomedStorage;
import com.example.mapper.BiomedStorageMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BiomedStorageService {

    @Resource
    private BiomedStorageMapper biomedStorageMapper;

    public void add(BiomedStorage biomedStorage) {
        if (biomedStorage.getStatus() == null || biomedStorage.getStatus().isBlank()) {
            biomedStorage.setStatus("在库");
        }
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

    public BiomedStorage selectByPosition(String position) {
        if (!StringUtils.hasText(position)) return null;
        return biomedStorageMapper.selectByPosition(position.trim());
    }

    /**
     * 根据存储位置查找；不存在时自动创建。
     * 用于新增样本和 Excel 导入时自动生成存储记录并回填 storageId。
     */
    public Long findOrCreateStorage(String position, String temp, String status, LocalDateTime expireTime) {
        if (!StringUtils.hasText(position)) return null;
        String pos = position.trim();
        BiomedStorage old = biomedStorageMapper.selectByPosition(pos);
        if (old != null) {
            return old.getId();
        }
        BiomedStorage storage = new BiomedStorage();
        storage.setPosition(pos);
        storage.setTemp(StringUtils.hasText(temp) ? temp.trim() : null);
        storage.setStatus(StringUtils.hasText(status) ? status.trim() : "在库");
        storage.setExpireTime(expireTime);
        storage.setCreateTime(LocalDateTime.now());
        biomedStorageMapper.insert(storage);
        return storage.getId();
    }

    /**
     * 编辑样本时，如果用户直接修改了存储位置，则同步更新当前 storageId 对应的存储记录。
     */
    public void updateStoragePosition(Long storageId, String position, String temp, String status, LocalDateTime expireTime) {
        if (storageId == null || !StringUtils.hasText(position)) return;
        BiomedStorage storage = biomedStorageMapper.selectById(storageId);
        if (storage == null) return;
        storage.setPosition(position.trim());
        if (StringUtils.hasText(temp)) storage.setTemp(temp.trim());
        if (StringUtils.hasText(status)) storage.setStatus(status.trim());
        if (expireTime != null) storage.setExpireTime(expireTime);
        biomedStorageMapper.updateById(storage);
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
