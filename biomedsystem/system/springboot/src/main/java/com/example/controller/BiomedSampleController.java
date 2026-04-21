package com.example.controller;

import com.example.common.ExcelUtil;
import com.example.common.Result;
import com.example.common.context.UserContext;
import com.example.common.model.LoginUser;
import com.example.entity.BiomedSample;
import com.example.service.BiomedSampleService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xssf.usermodel.*;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * 生物医学样本前端接口层
 * 替换原有GoodsController
 */
@RestController
@RequestMapping("/biomedSample")
public class BiomedSampleController {

    @Resource
    private BiomedSampleService biomedSampleService;

    /**
     * 新增样本
     * Postman请求：POST /biomedSample/add，Body选JSON格式
     */
    @PostMapping("/add")
    public Result add(@RequestBody BiomedSample biomedSample) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        biomedSample.setCreateBy(currentUser.getUserId());
        biomedSampleService.add(biomedSample);
        return Result.success("样本新增成功");
    }


    /**
     * 根据ID删除样本
     * Postman请求：DELETE /biomedSample/delete/1
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        BiomedSample biomedSample = biomedSampleService.selectById(id);
        if (biomedSample == null) {
            return Result.error("样本不存在");
        }

        if ("researcher".equals(currentUser.getRole())
                && !currentUser.getUserId().equals(biomedSample.getCreateBy())) {
            return Result.error("无权限删除该样本");
        }

        biomedSampleService.deleteById(id);
        return Result.success("样本删除成功");
    }


    /**
     * 修改样本
     * Postman请求：PUT /biomedSample/update，Body选JSON格式
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody BiomedSample biomedSample) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        if (biomedSample.getId() == null) {
            return Result.error("样本ID不能为空");
        }

        BiomedSample oldSample = biomedSampleService.selectById(biomedSample.getId());
        if (oldSample == null) {
            return Result.error("样本不存在");
        }

        if ("researcher".equals(currentUser.getRole())
                && !currentUser.getUserId().equals(oldSample.getCreateBy())) {
            return Result.error("无权限修改该样本");
        }

        // 防止前端乱改 createBy
        biomedSample.setCreateBy(oldSample.getCreateBy());

        biomedSampleService.updateById(biomedSample);
        return Result.success("样本修改成功");
    }


    /**
     * 根据ID查询样本
     * Postman请求：GET /biomedSample/selectById/1
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        BiomedSample biomedSample = biomedSampleService.selectById(id);
        if (biomedSample == null) {
            return Result.error("样本不存在");
        }

        if ("researcher".equals(currentUser.getRole())
                && !currentUser.getUserId().equals(biomedSample.getCreateBy())) {
            return Result.error("无权限查看该样本");
        }

        return Result.success(biomedSample);
    }


    /**
     * 条件查询所有样本
     * Postman请求：GET /biomedSample/selectAll
     */
    @GetMapping("/selectAll")
    public Result selectAll(BiomedSample biomedSample) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        if ("researcher".equals(currentUser.getRole())) {
            biomedSample.setCreateBy(currentUser.getUserId());
        }

        List<BiomedSample> list = biomedSampleService.selectAll(biomedSample);
        return Result.success(list);
    }


    /**
     * 分页查询样本
     * Postman请求：GET /biomedSample/selectPage?pageNum=1&pageSize=10
     */
    @GetMapping("/selectPage")
    public Result selectPage(BiomedSample biomedSample,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        LoginUser currentUser = UserContext.get();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        if ("researcher".equals(currentUser.getRole())) {
            biomedSample.setCreateBy(currentUser.getUserId());
        }

        PageInfo page = biomedSampleService.selectPage(biomedSample, pageNum, pageSize);
        return Result.success(page);
    }



    @PostMapping("/import")
    public Result importExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error("上传文件不能为空");
            }

            LoginUser currentUser = UserContext.get();

            List<BiomedSample> list = ExcelUtil.readSampleExcel(file.getInputStream());
            if (list == null || list.isEmpty()) {
                return Result.error("Excel中没有可导入的数据");
            }

            if (currentUser != null) {
                for (BiomedSample sample : list) {
                    sample.setCreateBy(currentUser.getUserId());
                }
            }

            biomedSampleService.saveBatch(list);
            return Result.success("导入成功：" + list.size() + "条");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 1. 获取数据
        List<BiomedSample> list = biomedSampleService.selectAll(null);

        // 2. 创建Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("样本数据");

        // 3. 表头
        XSSFRow head = sheet.createRow(0);
        head.createCell(0).setCellValue("样本编号");
        head.createCell(1).setCellValue("样本名称");
        head.createCell(2).setCellValue("样本类型");
        head.createCell(3).setCellValue("存储位置");
        head.createCell(4).setCellValue("采集时间");
        head.createCell(5).setCellValue("创建时间");

        // 4. 填充数据（兼容null，不依赖SampleTypeMapper）
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < list.size(); i++) {
            BiomedSample s = list.get(i);
            XSSFRow row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(s.getSampleNo());
            row.createCell(1).setCellValue(s.getSource());

            // 临时方案：先导出ID，避免依赖SampleTypeMapper报错
            row.createCell(2).setCellValue(s.getSampleTypeId() == null ? "未知" : s.getSampleTypeId().toString());

            row.createCell(3).setCellValue(s.getQueue());
            row.createCell(4).setCellValue(s.getCollectTime() == null ? "" : s.getCollectTime().format(dtf));
            row.createCell(5).setCellValue(s.getCreateTime() == null ? "" : s.getCreateTime().format(dtf));
        }

        // 5. 响应头（标准写法）
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("样本列表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName + ".xlsx");

        // 6. 输出（try-with-resources 自动关闭流）
        try (ServletOutputStream out = response.getOutputStream()) {
            workbook.write(out);
            out.flush();
        } finally {
            workbook.close();
        }
    }



}