package com.example.mapper;

import com.example.entity.Admin;
import java.util.List;

/**
 * 系统管理员Mapper（对接sys_user表）
 */
public interface AdminMapper {
    /**
     * 管理员登录（仅查role=admin的用户）
     */
    Admin login(Admin admin);

    /**
     * 新增用户（可指定角色）
     */
    int insert(Admin admin);

    /**
     * 根据ID删除用户
     */
    int deleteById(Long id);

    /**
     * 根据ID修改用户
     */
    int updateById(Admin admin);

    /**
     * 根据ID查询用户
     */
    Admin selectById(Long id);

    /**
     * 查询所有用户（管理员可看全部）
     */
    List<Admin> selectAll(Admin admin);
}