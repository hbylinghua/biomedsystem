package com.example.controller;

import com.example.common.Result;
import com.example.common.context.UserContext;
import com.example.common.model.LoginUser;
import com.example.service.SampleAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/sample/analysis")
public class SampleAnalysisController {

    @Autowired
    private SampleAnalysisService analysisService;

    /**
     * 队列样本占比分析
     */
    @GetMapping("/queueRatio")
    public Result queueRatio(
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) Long sampleTypeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        try {
            LoginUser currentUser = UserContext.get();
            Long userId = currentUser.getUserId();
            String role = currentUser.getRole();

            // ✅ 这里传对 6 个参数！
            return Result.success(analysisService.queueRatioAnalysis(
                    queueName, sampleTypeId, startDate, endDate, userId, role
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("队列分析失败：" + e.getMessage());
        }
    }

    /**
     * 采集时间分布分析
     */
    @GetMapping("/collectTimeDist")
    public Result collectTimeDist(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) Long sampleTypeId,
            HttpServletRequest request) {
        try {
            LoginUser currentUser = UserContext.get();
            Long userId = currentUser.getUserId();
            String role = currentUser.getRole();

            // ✅ 传对参数！
            return Result.success(analysisService.collectTimeDistAnalysis(
                    startDate, endDate, queueName, sampleTypeId, userId, role
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("时间分布分析失败：" + e.getMessage());
        }
    }

    /**
     * 样本类型数量分析
     */
    @GetMapping("/sampleTypeCount")
    public Result sampleTypeCount(
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) Long sampleTypeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        try {
            LoginUser currentUser = UserContext.get();
            Long userId = currentUser.getUserId();
            String role = currentUser.getRole();

            // ✅ 传对参数！
            return Result.success(analysisService.sampleTypeCountAnalysis(
                    queueName, sampleTypeId, startDate, endDate, userId, role
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("类型分析失败：" + e.getMessage());
        }
    }
}