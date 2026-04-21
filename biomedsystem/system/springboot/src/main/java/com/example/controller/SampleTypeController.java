package com.example.controller;

import com.example.common.Result;
import com.example.entity.SampleType;
import com.example.service.SampleTypeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sampleType")
public class SampleTypeController {

    @Resource
    private SampleTypeService sampleTypeService;

    @PostMapping("/add")
    public Result add(@RequestBody SampleType sampleType) {
        // 修正1：方法名改为 getTypeName()（匹配实体类驼峰命名）
        if (!StringUtils.hasText(sampleType.getTypeName())) {
            // 调用 Result 现有 error(String msg) 方法
            return Result.error("样本类型名称不能为空");
        }
        sampleTypeService.add(sampleType);
        // 调用 Result 现有 success() 方法（无参，默认code=200、msg=请求成功）
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        sampleTypeService.deleteById(id);
        // 调用 Result 现有 success() 方法
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody SampleType sampleType) {
        // 修正2：方法名改为 getTypeName()
        if (!StringUtils.hasText(sampleType.getTypeName())) {
            return Result.error("样本类型名称不能为空");
        }
        sampleTypeService.updateById(sampleType);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {
        SampleType sampleType = sampleTypeService.selectById(id);
        // 调用 Result 现有 success(Object data) 方法（返回数据，默认code=200、msg=请求成功）
        return Result.success(sampleType);
    }

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam(required = false) String name) {
        SampleType sampleType = new SampleType();
        // 修正3：方法名改为 setTypeName()
        sampleType.setTypeName(name);
        List<SampleType> list = sampleTypeService.selectAll(sampleType);
        // 调用 Result 现有 success(Object data) 方法
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 修正4：方法名改为 setTypeName()
        SampleType sampleType = new SampleType();
        sampleType.setTypeName(name);

        PageInfo<SampleType> page = sampleTypeService.selectPage(sampleType, pageNum, pageSize);
        // 调用 Result 现有 success(Object data) 方法（返回分页数据）
        return Result.success(page);
    }
}