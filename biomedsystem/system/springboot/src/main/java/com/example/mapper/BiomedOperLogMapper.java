package com.example.mapper;

import com.example.entity.BiomedOperLog;
import java.util.List;

public interface BiomedOperLogMapper {
    int insert(BiomedOperLog biomedOperLog);
    int deleteById(Long id);
    BiomedOperLog selectById(Long id);
    List<BiomedOperLog> selectAll(BiomedOperLog biomedOperLog);
}