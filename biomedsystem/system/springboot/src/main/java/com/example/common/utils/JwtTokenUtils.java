package com.example.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.example.common.model.LoginUser;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTokenUtils {

    private static final String SECRET = "biomed-system-secret-key";
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000L;

    public static String createToken(Long userId, String username, String role) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("role", role);
        payload.put("expireTime", System.currentTimeMillis() + EXPIRE_TIME);

        return JWTUtil.createToken(payload, SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static LoginUser parseToken(String token) {
        try {
            boolean verify = JWTUtil.verify(token, SECRET.getBytes(StandardCharsets.UTF_8));
            if (!verify) {
                return null;
            }

            JWT jwt = JWTUtil.parseToken(token);
            Object expireTimeObj = jwt.getPayload("expireTime");
            if (expireTimeObj == null) {
                return null;
            }

            long expireTime = Long.parseLong(expireTimeObj.toString());
            if (expireTime < System.currentTimeMillis()) {
                return null;
            }

            Long userId = Long.parseLong(jwt.getPayload("userId").toString());
            String username = jwt.getPayload("username").toString();
            String role = jwt.getPayload("role").toString();

            return new LoginUser(userId, username, role);
        } catch (Exception e) {
            return null;
        }
    }
}
