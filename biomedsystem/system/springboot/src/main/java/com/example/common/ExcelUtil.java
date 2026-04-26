package com.example.common;

import com.example.entity.BiomedSample;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ExcelUtil {

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    public static List<BiomedSample> readSampleExcel(InputStream inputStream) throws Exception {
        List<BiomedSample> sampleList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();

        // 列顺序：
        // 0 样本编号  1 样本名称  2 样本来源  3 样本类型ID  4 样本队列  5 存储ID  6 样本状态
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String sampleNoExcel = formatter.formatCellValue(row.getCell(0)).trim();
            String sampleName = formatter.formatCellValue(row.getCell(1)).trim();
            String source = formatter.formatCellValue(row.getCell(2)).trim();
            String sampleTypeIdText = formatter.formatCellValue(row.getCell(3)).trim();
            String queue = formatter.formatCellValue(row.getCell(4)).trim();
            String storageIdText = formatter.formatCellValue(row.getCell(5)).trim();
            String statusText = formatter.formatCellValue(row.getCell(6)).trim();

            if (sampleName.isBlank() && source.isBlank() && sampleTypeIdText.isBlank() && queue.isBlank()) {
                continue;
            }

            if (sampleName.isBlank()) throw new RuntimeException("第 " + (i + 1) + " 行：样本名称不能为空");
            if (source.isBlank()) throw new RuntimeException("第 " + (i + 1) + " 行：样本来源不能为空");
            if (sampleTypeIdText.isBlank()) throw new RuntimeException("第 " + (i + 1) + " 行：样本类型ID不能为空");
            if (queue.isBlank()) throw new RuntimeException("第 " + (i + 1) + " 行：样本队列不能为空");

            Long sampleTypeId;
            try {
                sampleTypeId = Long.parseLong(sampleTypeIdText);
            } catch (NumberFormatException e) {
                throw new RuntimeException("第 " + (i + 1) + " 行：样本类型ID必须是数字");
            }

            Long storageId = null;
            if (!storageIdText.isBlank()) {
                try {
                    storageId = Long.parseLong(storageIdText);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("第 " + (i + 1) + " 行：存储ID必须是数字");
                }
            }

            int status = 0;
            if ("正常".equals(statusText)) {
                status = 1;
            } else if ("异常".equals(statusText)) {
                status = 2;
            } else {
                status = 0;
            }

            BiomedSample sample = new BiomedSample();
            sample.setSampleNo(sampleNoExcel.isBlank() ? generateSampleNo() : sampleNoExcel);
            sample.setSampleName(sampleName);
            sample.setSource(source);
            sample.setSampleTypeId(sampleTypeId);
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
