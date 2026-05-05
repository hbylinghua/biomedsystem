package com.example.controller;

import com.example.common.Result;
import com.example.entity.BiomedOperLog;
import com.example.service.BiomedOperLogService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志控制器。
 * 注意：日志应由样本新增、修改、删除、导入等业务自动生成，不提供前端手动新增/删除接口。
 */
@RestController
@RequestMapping("/biomedOperLog")
public class BiomedOperLogController {

    @Resource
    private BiomedOperLogService biomedOperLogService;

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {
        BiomedOperLog biomedOperLog = biomedOperLogService.selectById(id);
        return Result.success(biomedOperLog);
    }

    @GetMapping("/selectAll")
    public Result selectAll(BiomedOperLog biomedOperLog) {
        List<BiomedOperLog> list = biomedOperLogService.selectAll(biomedOperLog);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(BiomedOperLog biomedOperLog,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<BiomedOperLog> page = biomedOperLogService.selectPage(biomedOperLog, pageNum, pageSize);
        return Result.success(page);
    }
}
