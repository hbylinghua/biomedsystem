package com.example.mapper;

import com.example.entity.BiomedStorage;
import java.util.List;

public interface BiomedStorageMapper {
    int insert(BiomedStorage biomedStorage);
    int deleteById(Long id);
    int updateById(BiomedStorage biomedStorage);
    BiomedStorage selectById(Long id);
    List<BiomedStorage> selectAll(BiomedStorage biomedStorage);
}