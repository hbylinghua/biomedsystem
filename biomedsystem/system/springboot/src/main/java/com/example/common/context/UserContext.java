package com.example.common.context;

import com.example.common.model.LoginUser;

public class UserContext {
    private static final ThreadLocal<LoginUser> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(LoginUser loginUser) {
        THREAD_LOCAL.set(loginUser);
    }

    public static LoginUser get() {
        return THREAD_LOCAL.get();
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }
}
