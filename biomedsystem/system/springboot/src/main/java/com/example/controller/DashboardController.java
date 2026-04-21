package com.example.controller;

import com.example.common.Result;
import com.example.common.context.UserContext;
import com.example.common.model.LoginUser;
import com.example.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public Result overview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) Long sampleTypeId
    ) {
        LoginUser currentUser = UserContext.get();
        Long userId = currentUser.getUserId();
        String role = currentUser.getRole();

        Map<String, Object> data = dashboardService.getOverview(
                startDate, endDate, queueName, sampleTypeId, userId, role
        );
        return Result.success(data);
    }

    @GetMapping("/sampleTrend")
    public Result sampleTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) Long sampleTypeId
    ) {
        LoginUser currentUser = UserContext.get();
        return Result.success(dashboardService.getSampleTrend(
                startDate, endDate, queueName, sampleTypeId,
                currentUser.getUserId(), currentUser.getRole()
        ));
    }

    @GetMapping("/statusDistribution")
    public Result statusDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) Long sampleTypeId
    ) {
        LoginUser currentUser = UserContext.get();
        return Result.success(dashboardService.getStatusDistribution(
                startDate, endDate, queueName, sampleTypeId,
                currentUser.getUserId(), currentUser.getRole()
        ));
    }

    @GetMapping("/sourceDistribution")
    public Result sourceDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) Long sampleTypeId
    ) {
        LoginUser currentUser = UserContext.get();
        return Result.success(dashboardService.getSourceDistribution(
                startDate, endDate, queueName, sampleTypeId,
                currentUser.getUserId(), currentUser.getRole()
        ));
    }

    @GetMapping("/warnings")
    public Result warnings(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) Long sampleTypeId
    ) {
        LoginUser currentUser = UserContext.get();
        return Result.success(dashboardService.getWarnings(
                startDate, endDate, queueName, sampleTypeId,
                currentUser.getUserId(), currentUser.getRole()
        ));
    }
}