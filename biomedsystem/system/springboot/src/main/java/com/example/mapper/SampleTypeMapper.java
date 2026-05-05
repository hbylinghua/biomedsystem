package com.example.mapper;

import com.example.entity.SampleType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SampleTypeMapper {
    int insert(SampleType sampleType);
    int deleteById(Long id);
    int updateById(SampleType sampleType);
    SampleType selectById(Long id);
    SampleType selectByTypeName(@Param("typeName") String typeName);
    List<SampleType> selectAll(SampleType sampleType);
}
