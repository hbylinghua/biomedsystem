package com.example.service;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    // 登录
    public User login(User user) {
        return userMapper.login(user);
    }

    // 注册
    public void register(User user) {
        userMapper.insert(user);
    }

    // 新增用户（和register逻辑一致，新增别名方便理解）
    public void add(User user) {
        userMapper.insert(user);
    }

    // 根据ID删除
    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }

    // 根据ID更新
    public void updateById(User user) {
        userMapper.updateById(user);
    }

    // 根据ID查询
    public User selectById(Long id) {
        return userMapper.selectById(id);
    }

    // 条件查询所有
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }
}