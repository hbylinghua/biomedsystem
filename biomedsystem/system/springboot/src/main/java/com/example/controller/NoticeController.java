package com.example.controller;

import com.example.common.Result;
import com.example.entity.Notice;
import com.example.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @GetMapping("/selectAll")
    public Result selectAll(Notice notice) {
        List<Notice> list = noticeService.selectAll(notice);
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Notice notice) {
        // 1. 非空校验（避免空标题/空内容）
        if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            return Result.error("公告标题不能为空");
        }
        if (notice.getContent() == null || notice.getContent().trim().isEmpty()) {
            return Result.error("公告内容不能为空");
        }
        // 2. 自动填充实时时间（格式：yyyy-MM-dd HH:mm:ss）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(formatter);
        notice.setTime(currentTime); // 给实体类的time字段赋值
        // 3. 插入数据库
        noticeService.insert(notice);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Notice notice) {
        // 1. 非空校验
        if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            return Result.error("公告标题不能为空");
        }
        if (notice.getContent() == null || notice.getContent().trim().isEmpty()) {
            return Result.error("公告内容不能为空");
        }
        // 2. 编辑时刷新为当前实时时间（可选：若不想更新，可删除这行）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        notice.setTime(LocalDateTime.now().format(formatter));
        // 3. 更新数据库
        noticeService.updateById(notice);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        noticeService.deleteById(id);
        return Result.success();
    }
}