package com.example.common;

import com.example.entity.BiomedSample;
import com.example.entity.SampleType;
import com.example.mapper.SampleTypeMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ExcelUtil {

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    // 注入Mapper → 自动读数据库
    private static SampleTypeMapper sampleTypeMapper;

    @Resource
    public void setSampleTypeMapper(SampleTypeMapper sampleTypeMapper) {
        ExcelUtil.sampleTypeMapper = sampleTypeMapper;
    }

    public static List<BiomedSample> readSampleExcel(InputStream inputStream) throws Exception {
        List<BiomedSample> sampleList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();

        // 列顺序：
        // 0 样本编号  1 样本名称  2 样本类型  3 存储位置  4 存储ID  5 样本状态
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String sampleNoExcel = formatter.formatCellValue(row.getCell(0)).trim();
            String source = formatter.formatCellValue(row.getCell(1)).trim();
            String sampleTypeName = formatter.formatCellValue(row.getCell(2)).trim();
            String queue = formatter.formatCellValue(row.getCell(3)).trim();
            String storageIdText = formatter.formatCellValue(row.getCell(4)).trim();
            String statusText = formatter.formatCellValue(row.getCell(5)).trim();

            if (source.isBlank() && sampleTypeName.isBlank() && queue.isBlank() && storageIdText.isBlank()) {
                continue;
            }

            if (source.isBlank()) throw new RuntimeException("第 " + (i + 1) + " 行：样本名称不能为空");
            if (sampleTypeName.isBlank()) throw new RuntimeException("第 " + (i + 1) + " 行：样本类型不能为空");
            if (queue.isBlank()) throw new RuntimeException("第 " + (i + 1) + " 行：存储位置不能为空");
            if (storageIdText.isBlank()) throw new RuntimeException("第 " + (i + 1) + " 行：存储ID不能为空");

            long storageId = Long.parseLong(storageIdText.trim());

            // ==============================================
            // ✅ 从数据库自动查询样本类型ID（真正正确的方式）
            // ==============================================
            Long sampleTypeId;
            try {
                sampleTypeId = Long.parseLong(sampleTypeName.trim());
            } catch (NumberFormatException e) {
                throw new RuntimeException("第 " + (i + 1) + " 行：样本类型必须是数字ID（如12、13）");
            }

            int status = 0;
            if ("已使用".equals(statusText)) status = 1;
            else if ("废弃".equals(statusText)) status = 2;

            BiomedSample sample = new BiomedSample();
            sample.setSampleNo(generateSampleNo());
            sample.setSource(source);
            sample.setSampleTypeId(sampleTypeId); // 直接用 Excel 里填的 ID
            sample.setQueue(queue);
            sample.setStorageId(storageId);
            sample.setStatus(status);
            sample.setCollectTime(LocalDateTime.now());
            sample.setCreateTime(LocalDateTime.now());
            sample.setUpdateTime(LocalDateTime.now());

            sampleList.add(sample);
        }

        workbook.close();
        return sampleList;
    }

    private static String generateSampleNo() {
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int num = SEQ.getAndIncrement() % 1000;
        return "BIO" + prefix + String.format("%03d", num);
    }
}