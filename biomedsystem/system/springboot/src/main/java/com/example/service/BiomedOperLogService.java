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

    /**
     * 日志只允许系统业务代码自动调用，不在前端提供手动新增入口。
     */
    public void add(BiomedOperLog biomedOperLog) {
        biomedOperLog.setOperTime(LocalDateTime.now());
        biomedOperLogMapper.insert(biomedOperLog);
    }

    public void record(Long sampleId, Long userId, String type, String content) {
        try {
            BiomedOperLog log = new BiomedOperLog();
            log.setSampleId(sampleId);
            log.setOperBy(userId);
            log.setOperType(type);
            log.setContent(content);
            add(log);
        } catch (Exception ignored) {
            // 日志失败不能影响主业务，例如样本新增、修改、删除。
        }
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
