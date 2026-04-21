package com.example.common.utils;

import com.example.common.context.UserContext;
import com.example.common.model.LoginUser;
import com.example.exception.CustomException;

public class PermissionUtils {

    public static LoginUser getCurrentUser() {
        LoginUser user = UserContext.get();
        if (user == null) {
            throw new CustomException("未登录");
        }
        return user;
    }

    public static void checkAdmin() {
        LoginUser user = getCurrentUser();
        if (!"admin".equals(user.getRole())) {
            throw new CustomException("无权限操作");
        }
    }
}
