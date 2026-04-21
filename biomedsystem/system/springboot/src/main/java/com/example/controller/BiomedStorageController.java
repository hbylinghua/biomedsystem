package com.example.controller;

import com.example.common.Result;
import com.example.entity.BiomedStorage;
import com.example.service.BiomedStorageService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/biomedStorage")
public class BiomedStorageController {

    @Resource
    private BiomedStorageService biomedStorageService;

    @PostMapping("/add")
    public Result add(@RequestBody BiomedStorage biomedStorage) {
        biomedStorageService.add(biomedStorage);
        return Result.success("存储信息新增成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        biomedStorageService.deleteById(id);
        return Result.success("存储信息删除成功");
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody BiomedStorage biomedStorage) {
        biomedStorageService.updateById(biomedStorage);
        return Result.success("存储信息修改成功");
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {
        BiomedStorage biomedStorage = biomedStorageService.selectById(id);
        return Result.success(biomedStorage);
    }

    @GetMapping("/selectAll")
    public Result selectAll(BiomedStorage biomedStorage) {
        List<BiomedStorage> list = biomedStorageService.selectAll(biomedStorage);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(BiomedStorage biomedStorage,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<BiomedStorage> page = biomedStorageService.selectPage(biomedStorage, pageNum, pageSize);
        return Result.success(page);
    }
}