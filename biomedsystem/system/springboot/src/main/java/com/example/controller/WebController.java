package com.example.controller;

import com.example.common.Result;
import com.example.common.context.UserContext;
import com.example.common.model.LoginResponse;
import com.example.common.model.LoginUser;
import com.example.common.utils.JwtTokenUtils;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.UpdatePasswordRequest;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebController {

    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    @GetMapping("/")
    public Result hello() {
        return Result.success("欢迎使用生物医学样本管理系统");
    }

    /**
     * 统一登录接口：区分 admin / researcher
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        try {
            if (account == null) {
                throw new CustomException("请求参数不能为空");
            }
            if (isBlank(account.getUsername()) || isBlank(account.getPassword()) || isBlank(account.getRole())) {
                throw new CustomException("用户名/密码/角色不能为空");
            }

            String username = account.getUsername().trim();
            String password = account.getPassword().trim();
            String role = account.getRole().trim();

            if ("admin".equals(role)) {
                Admin admin = new Admin();
                admin.setUsername(username);
                admin.setPassword(password);

                Admin loginAdmin = adminService.login(admin);
                if (loginAdmin == null) {
                    throw new CustomException("管理员用户名或密码错误");
                }

                String token = JwtTokenUtils.createToken(
                        loginAdmin.getId(),
                        loginAdmin.getUsername(),
                        loginAdmin.getRole()
                );

                LoginResponse response = new LoginResponse(
                        loginAdmin.getId(),
                        loginAdmin.getUsername(),
                        loginAdmin.getRole(),
                        token
                );
                return Result.success("管理员登录成功", response);
            }

            if ("researcher".equals(role)) {
                User user = new User();
                user.setUsername(username);
                user.setPassword(password);

                User loginUser = userService.login(user);
                if (loginUser == null) {
                    throw new CustomException("研究员用户名或密码错误");
                }

                String token = JwtTokenUtils.createToken(
                        loginUser.getId(),
                        loginUser.getUsername(),
                        loginUser.getRole()
                );

                LoginResponse response = new LoginResponse(
                        loginUser.getId(),
                        loginUser.getUsername(),
                        loginUser.getRole(),
                        token
                );
                return Result.success("研究员登录成功", response);
            }

            throw new CustomException("角色选择错误，仅支持 admin / researcher");
        } catch (CustomException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("登录失败，请稍后重试");
        }
    }

    /**
     * 注册接口：仅支持研究员注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try {
            if (user == null) {
                throw new CustomException("请求参数不能为空");
            }
            if (isBlank(user.getUsername()) || isBlank(user.getPassword())) {
                throw new CustomException("用户名/密码不能为空");
            }

            user.setUsername(user.getUsername().trim());
            user.setPassword(user.getPassword().trim());
            user.setRole("researcher");

            userService.register(user);
            return Result.success("研究员账号注册成功");
        } catch (CustomException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注册失败，请稍后重试");
        }
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/auth/me")
    public Result me() {
        try {
            LoginUser currentUser = UserContext.get();
            if (currentUser == null) {
                return Result.error("未登录");
            }
            return Result.success(currentUser);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取当前用户信息失败");
        }
    }

    /**
     * 修改密码：保持明文比较
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody UpdatePasswordRequest request) {
        try {
            LoginUser currentUser = UserContext.get();
            if (currentUser == null) {
                throw new CustomException("未登录");
            }

            if (request == null) {
                throw new CustomException("请求参数不能为空");
            }
            if (isBlank(request.getOldPassword()) || isBlank(request.getNewPassword()) || isBlank(request.getConfirmPassword())) {
                throw new CustomException("参数不能为空");
            }

            String oldPassword = request.getOldPassword().trim();
            String newPassword = request.getNewPassword().trim();
            String confirmPassword = request.getConfirmPassword().trim();

            if (!newPassword.equals(confirmPassword)) {
                throw new CustomException("两次输入的新密码不一致");
            }

            if (oldPassword.equals(newPassword)) {
                throw new CustomException("新密码不能与旧密码相同");
            }

            if ("admin".equals(currentUser.getRole())) {
                Admin admin = adminService.selectById(currentUser.getUserId());
                if (admin == null) {
                    throw new CustomException("用户不存在");
                }
                if (!admin.getPassword().equals(oldPassword)) {
                    throw new CustomException("旧密码错误");
                }

                admin.setPassword(newPassword);
                adminService.updateById(admin);
                return Result.success("密码修改成功");
            }

            if ("researcher".equals(currentUser.getRole())) {
                User user = userService.selectById(currentUser.getUserId());
                if (user == null) {
                    throw new CustomException("用户不存在");
                }
                if (!user.getPassword().equals(oldPassword)) {
                    throw new CustomException("旧密码错误");
                }

                user.setPassword(newPassword);
                userService.updateById(user);
                return Result.success("密码修改成功");
            }

            throw new CustomException("当前角色不支持修改密码");
        } catch (CustomException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("密码修改失败，请稍后重试");
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}