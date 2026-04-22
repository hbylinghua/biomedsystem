package com.example.controller;

import com.example.common.Result;
import com.example.common.context.UserContext;
import com.example.common.model.LoginUser;
import com.example.service.RecommendationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    @Resource
    private RecommendationService recommendationService;

    @GetMapping("/storageForSample")
    public Result storageForSample(
            @RequestParam(required = false) Long sampleTypeId,
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) String source
    ) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        return Result.success(recommendationService.recommendStorage(
                sampleTypeId,
                queueName,
                source,
                currentUser.getUserId(),
                currentUser.getRole()
        ));
    }

    @GetMapping("/pendingTasks")
    public Result pendingTasks(
            @RequestParam(required = false, defaultValue = "5") Integer limit
    ) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        return Result.success(recommendationService.recommendPendingTasks(
                limit,
                currentUser.getUserId(),
                currentUser.getRole()
        ));
    }
}