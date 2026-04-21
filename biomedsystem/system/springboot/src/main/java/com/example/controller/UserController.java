package com.example.controller;

import com.example.common.Result;
import com.example.common.model.LoginUser;
import com.example.common.utils.PermissionUtils;
import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 查询所有用户
     * 仅管理员可访问
     */
    @GetMapping("/selectAll")
    public Result selectAll(User user) {
        PermissionUtils.checkAdmin();
        List<User> list = userService.selectAll(user);
        return Result.success(list);
    }

    /**
     * 新增用户
     * 仅管理员可访问
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        PermissionUtils.checkAdmin();
        userService.add(user);
        return Result.success("新增成功");
    }

    /**
     * 更新用户
     * 仅管理员可访问
     * 为避免误操作，这里禁止在用户管理页直接编辑自己
     */
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        PermissionUtils.checkAdmin();

        LoginUser currentUser = PermissionUtils.getCurrentUser();
        if (user.getId() != null && user.getId().equals(currentUser.getUserId())) {
            return Result.error("不能在用户管理中编辑自己，请到个人资料页面修改");
        }

        User targetUser = userService.selectById(user.getId());
        if (targetUser == null) {
            return Result.error("目标用户不存在");
        }

        userService.updateById(user);
        return Result.success("更新成功");
    }

    /**
     * 删除用户
     * 仅管理员可访问
     * 禁止删除自己，避免把自己删掉后无法登录
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        PermissionUtils.checkAdmin();

        LoginUser currentUser = PermissionUtils.getCurrentUser();
        if (id.equals(currentUser.getUserId())) {
            return Result.error("不能删除自己");
        }

        User targetUser = userService.selectById(id);
        if (targetUser == null) {
            return Result.error("目标用户不存在");
        }

        userService.deleteById(id);
        return Result.success("删除成功");
    }
}
