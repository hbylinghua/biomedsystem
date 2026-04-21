package com.example.controller;

import com.example.common.Result;
import com.example.entity.Admin;
import com.example.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping("/login")
    public Result login(@RequestBody Admin admin) {
        Admin loginAdmin = adminService.login(admin);
        if (loginAdmin != null) {
            return Result.success("样本系统管理员登录成功", loginAdmin);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    @PostMapping("/add")
    public Result add(@RequestBody Admin admin) {
        adminService.add(admin);
        return Result.success("用户新增成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        adminService.deleteById(id);
        return Result.success("用户删除成功");
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody Admin admin) {
        adminService.updateById(admin);
        return Result.success("用户修改成功");
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {
        Admin admin = adminService.selectById(id);
        return Result.success(admin);
    }

    @GetMapping("/selectAll")
    public Result selectAll(Admin admin) {
        List<Admin> list = adminService.selectAll(admin);
        return Result.success(list);
    }
}