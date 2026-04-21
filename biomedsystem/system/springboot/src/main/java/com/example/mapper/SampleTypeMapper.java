package com.example.mapper;

import com.example.entity.SampleType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SampleTypeMapper {
    int insert(SampleType sampleType);
    int deleteById(Long id);
    int updateById(SampleType sampleType);
    SampleType selectById(Long id);
    List<SampleType> selectAll(SampleType sampleType);
    @Select("SELECT id FROM sample_type WHERE type_name = #{typeName}")
    SampleType selectByName(String typeName);

}