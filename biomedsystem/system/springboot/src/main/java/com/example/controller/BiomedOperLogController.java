package com.example.controller;

import com.example.common.Result;
import com.example.entity.BiomedOperLog;
import com.example.service.BiomedOperLogService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/biomedOperLog")
public class BiomedOperLogController {

    @Resource
    private BiomedOperLogService biomedOperLogService;

    @PostMapping("/add")
    public Result add(@RequestBody BiomedOperLog biomedOperLog) {
        biomedOperLogService.add(biomedOperLog);
        return Result.success("操作日志新增成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        biomedOperLogService.deleteById(id);
        return Result.success("操作日志删除成功");
    }

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