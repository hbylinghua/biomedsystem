package com.example.service;

import com.example.entity.Admin;
import com.example.mapper.AdminMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    public Admin login(Admin admin) {
        return adminMapper.login(admin);
    }

    public void add(Admin admin) {
        adminMapper.insert(admin);
    }

    public void deleteById(Long id) {
        adminMapper.deleteById(id);
    }

    public void updateById(Admin admin) {
        adminMapper.updateById(admin);
    }

    public Admin selectById(Long id) {
        return adminMapper.selectById(id);
    }

    public List<Admin> selectAll(Admin admin) {
        return adminMapper.selectAll(admin);
    }
}