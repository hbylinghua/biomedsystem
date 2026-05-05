package com.example.controller;

import com.example.common.Result;
import com.example.common.context.UserContext;
import com.example.common.model.LoginUser;
import com.example.entity.BiomedStorage;
import com.example.service.BiomedOperLogService;
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
    @Resource
    private BiomedOperLogService biomedOperLogService;

    @PostMapping("/add")
    public Result add(@RequestBody BiomedStorage biomedStorage) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) return Result.error("未登录");
        biomedStorageService.add(biomedStorage);
        biomedOperLogService.record(null, currentUser.getUserId(), "新增存储", "新增存储记录：" + biomedStorage.getPosition());
        return Result.success("存储信息新增成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) return Result.error("未登录");
        BiomedStorage old = biomedStorageService.selectById(id);
        biomedStorageService.deleteById(id);
        biomedOperLogService.record(null, currentUser.getUserId(), "删除存储", "删除存储记录：" + (old == null ? id : old.getPosition()));
        return Result.success("存储信息删除成功");
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody BiomedStorage biomedStorage) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) return Result.error("未登录");
        biomedStorageService.updateById(biomedStorage);
        biomedOperLogService.record(null, currentUser.getUserId(), "修改存储", "修改存储记录：" + biomedStorage.getPosition());
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
