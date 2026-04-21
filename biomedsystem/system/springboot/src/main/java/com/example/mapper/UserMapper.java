package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

// 只保留@Mapper注解，删除所有SQL注解
@Mapper
public interface UserMapper {
    // 仅保留方法签名，无注解
    User login(User user);
    List<User> selectAll(User user);
    void insert(User user);
    void updateById(User user);
    void deleteById(Long id);
    User selectById(Long id);
}