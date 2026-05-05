package com.example.mapper;

import com.example.entity.BiomedStorage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BiomedStorageMapper {
    int insert(BiomedStorage biomedStorage);
    int deleteById(Long id);
    int updateById(BiomedStorage biomedStorage);
    BiomedStorage selectById(Long id);
    BiomedStorage selectByPosition(@Param("position") String position);
    List<BiomedStorage> selectAll(BiomedStorage biomedStorage);
}
