package com.example.mapper;

import com.example.entity.Notice;
import java.util.List;

public interface NoticeMapper {

    List<Notice> selectAll(Notice notice);

    int insert(Notice notice);

    int updateById(Notice notice);

    int deleteById(Integer id);
}