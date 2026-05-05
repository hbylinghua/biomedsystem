package com.example.service;

import com.example.entity.BiomedSample;
import com.example.entity.SampleType;
import com.example.mapper.BiomedSampleMapper;
import com.example.mapper.SampleTypeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BiomedSampleService {

    @Resource
    private BiomedSampleMapper biomedSampleMapper;

    @Resource
    private SampleTypeMapper sampleTypeMapper;

    @Resource
    private BiomedStorageService biomedStorageService;

    public String getSampleTypeName(Long typeId) {
        if (typeId == null) return "未知类型";
        SampleType type = sampleTypeMapper.selectById(typeId);
        return type == null ? "未知类型" : type.getTypeName();
    }

    public void add(BiomedSample biomedSample) {
        prepareSampleForSave(biomedSample, false);
        biomedSampleMapper.insert(biomedSample);
    }

    public void deleteById(Long id) {
        biomedSampleMapper.deleteById(id);
    }

    public void updateById(BiomedSample biomedSample) {
        prepareSampleForSave(biomedSample, true);
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
        if (sampleList == null || sampleList.isEmpty()) return;
        for (BiomedSample sample : sampleList) {
            prepareSampleForSave(sample, false);
        }
        biomedSampleMapper.insertBatch(sampleList);
    }

    /**
     * 统一处理单条新增、编辑和 Excel 导入：
     * 1. 样本编号为空时自动生成；
     * 2. 样本名称为空时自动生成，不再要求前端强制填写；
     * 3. Excel 中填写样本类型名称时，自动转换为 sampleTypeId；
     * 4. 用户填写存储位置但没有 storageId 时，自动创建存储记录并回填 storageId；
     * 5. 编辑样本时如果修改了存储位置，同步更新对应存储记录。
     */
    private void prepareSampleForSave(BiomedSample sample, boolean update) {
        if (sample == null) throw new RuntimeException("样本数据不能为空");

        if (!StringUtils.hasText(sample.getSampleNo())) {
            sample.setSampleNo(generateSampleNo());
        }

        if (sample.getStatus() == null) sample.setStatus(0);
        if (sample.getCollectTime() == null) sample.setCollectTime(LocalDateTime.now());

        resolveSampleType(sample);
        resolveStorage(sample, update);

        if (!StringUtils.hasText(sample.getSampleName())) {
            sample.setSampleName("样本-" + sample.getSampleNo());
        }

        if (!StringUtils.hasText(sample.getSource())) {
            throw new RuntimeException("样本来源/志愿者不能为空");
        }
        if (!StringUtils.hasText(sample.getQueue())) {
            throw new RuntimeException("样本队列不能为空");
        }
        if (sample.getSampleTypeId() == null) {
            throw new RuntimeException("样本类型不能为空");
        }

        if (!update) {
            sample.setCreateTime(LocalDateTime.now());
        }
        sample.setUpdateTime(LocalDateTime.now());
    }

    private void resolveSampleType(BiomedSample sample) {
        if (sample.getSampleTypeId() != null) {
            SampleType type = sampleTypeMapper.selectById(sample.getSampleTypeId());
            if (type == null) {
                throw new RuntimeException("样本类型ID不存在：" + sample.getSampleTypeId());
            }
            return;
        }

        if (StringUtils.hasText(sample.getSampleTypeName())) {
            SampleType type = sampleTypeMapper.selectByTypeName(sample.getSampleTypeName().trim());
            if (type == null) {
                throw new RuntimeException("样本类型不存在：" + sample.getSampleTypeName() + "，请先在样本类型管理中新增该类型");
            }
            sample.setSampleTypeId(type.getId());
        }
    }

    private void resolveStorage(BiomedSample sample, boolean update) {
        String position = sample.getStoragePosition();

        if (StringUtils.hasText(position)) {
            if (sample.getStorageId() == null) {
                Long storageId = biomedStorageService.findOrCreateStorage(
                        position,
                        sample.getStorageTemp(),
                        StringUtils.hasText(sample.getStorageStatus()) ? sample.getStorageStatus() : "在库",
                        sample.getStorageExpireTime()
                );
                sample.setStorageId(storageId);
            } else if (update) {
                biomedStorageService.updateStoragePosition(
                        sample.getStorageId(),
                        position,
                        sample.getStorageTemp(),
                        sample.getStorageStatus(),
                        sample.getStorageExpireTime()
                );
            }
        }
    }

    private String generateSampleNo() {
        String prefix = "BIO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = String.format("%03d", System.currentTimeMillis() % 1000);
        return prefix + suffix;
    }
}
