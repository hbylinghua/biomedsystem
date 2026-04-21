package com.example.entity;

/**
 * 登录账号实体（适配sys_user表的登录逻辑）
 */
public class Account {
    private String username;  // 用户名
    private String password;  // 密码
    private String role;      // 角色：admin/researcher（小写，匹配数据库）

    // Getter & Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}