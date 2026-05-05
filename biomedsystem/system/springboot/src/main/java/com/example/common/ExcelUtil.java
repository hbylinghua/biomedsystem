package com.example.common;

import com.example.entity.BiomedSample;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ExcelUtil {

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    /**
     * 读取样本导入 Excel。
     *
     * 新模板尽量和“新增单条样本”表单保持一致：
     * 样本编号(可选)、样本名称(可选)、样本来源/志愿者、样本队列、采集时间、样本类型、存储位置、样本状态。
     *
     * 兼容旧模板字段：样本类型ID、存储ID。
     */
    public static List<BiomedSample> readSampleExcel(InputStream inputStream) throws Exception {
        List<BiomedSample> sampleList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            workbook.close();
            throw new RuntimeException("Excel表头不能为空");
        }

        Map<String, Integer> headerMap = buildHeaderMap(headerRow, formatter);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isEmptyRow(row, formatter)) continue;

            String sampleNo = getCell(row, headerMap, formatter, "样本编号");
            String sampleName = getCell(row, headerMap, formatter, "样本名称");
            String source = getCell(row, headerMap, formatter, "样本来源/志愿者", "样本来源", "来源", "志愿者");
            String queue = getCell(row, headerMap, formatter, "样本队列", "队列");
            String collectTimeText = getCell(row, headerMap, formatter, "采集时间");
            String sampleTypeText = getCell(row, headerMap, formatter, "样本类型", "样本类型名称");
            String sampleTypeIdText = getCell(row, headerMap, formatter, "样本类型ID", "类型ID");
            String storagePosition = getCell(row, headerMap, formatter, "存储位置", "存储记录", "库位");
            String storageIdText = getCell(row, headerMap, formatter, "存储ID");
            String storageTemp = getCell(row, headerMap, formatter, "存储温度", "温度");
            String storageStatus = getCell(row, headerMap, formatter, "存储状态");
            String statusText = getCell(row, headerMap, formatter, "样本状态", "状态");

            if (source.isBlank()) throw new RuntimeException("第 " + (i + 1) + " 行：样本来源/志愿者不能为空");
            if (queue.isBlank()) throw new RuntimeException("第 " + (i + 1) + " 行：样本队列不能为空");
            if (sampleTypeText.isBlank() && sampleTypeIdText.isBlank()) {
                throw new RuntimeException("第 " + (i + 1) + " 行：样本类型不能为空（可填写类型名称，如：血液；也可填写样本类型ID）");
            }

            BiomedSample sample = new BiomedSample();
            sample.setSampleNo(sampleNo.isBlank() ? generateSampleNo() : sampleNo);
            sample.setSampleName(sampleName);
            sample.setSource(source);
            sample.setQueue(queue);
            sample.setCollectTime(parseDateTime(collectTimeText, i + 1));
            sample.setStatus(parseStatus(statusText));
            sample.setStoragePosition(storagePosition);
            sample.setStorageTemp(storageTemp);
            sample.setStorageStatus(storageStatus.isBlank() ? "在库" : storageStatus);

            if (!sampleTypeIdText.isBlank()) {
                sample.setSampleTypeId(parseLong(sampleTypeIdText, "样本类型ID", i + 1));
            } else if (!sampleTypeText.isBlank() && sampleTypeText.matches("\\d+")) {
                sample.setSampleTypeId(parseLong(sampleTypeText, "样本类型", i + 1));
            } else {
                sample.setSampleTypeName(sampleTypeText);
            }

            if (!storageIdText.isBlank()) {
                sample.setStorageId(parseLong(storageIdText, "存储ID", i + 1));
            }

            sampleList.add(sample);
        }

        workbook.close();
        return sampleList;
    }

    private static Map<String, Integer> buildHeaderMap(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String title = formatter.formatCellValue(headerRow.getCell(i)).trim();
            if (!title.isBlank()) headerMap.put(title, i);
        }
        return headerMap;
    }

    private static boolean isEmptyRow(Row row, DataFormatter formatter) {
        for (int j = 0; j < row.getLastCellNum(); j++) {
            if (!formatter.formatCellValue(row.getCell(j)).trim().isBlank()) return false;
        }
        return true;
    }

    private static String getCell(Row row, Map<String, Integer> headerMap, DataFormatter formatter, String... names) {
        for (String name : names) {
            Integer idx = headerMap.get(name);
            if (idx != null) return formatter.formatCellValue(row.getCell(idx)).trim();
        }
        return "";
    }

    private static Long parseLong(String text, String fieldName, int rowNum) {
        try {
            return Long.parseLong(text.trim());
        } catch (Exception e) {
            throw new RuntimeException("第 " + rowNum + " 行：" + fieldName + "必须是数字，或请改用名称字段填写中文名称");
        }
    }

    private static LocalDateTime parseDateTime(String text, int rowNum) {
        if (text == null || text.isBlank()) return LocalDateTime.now();
        String value = text.trim();

        List<DateTimeFormatter> dateTimeFormatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy/M/d H:mm")
        );
        for (DateTimeFormatter formatter : dateTimeFormatters) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        List<DateTimeFormatter> dateFormatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/M/d")
        );
        for (DateTimeFormatter formatter : dateFormatters) {
            try {
                LocalDate date = LocalDate.parse(value, formatter);
                return LocalDateTime.of(date, LocalTime.MIN);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new RuntimeException("第 " + rowNum + " 行：采集时间格式错误，建议使用 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd");
    }

    private static int parseStatus(String statusText) {
        if (statusText == null || statusText.isBlank()) return 0;
        String status = statusText.trim();
        return switch (status) {
            case "0", "待处理" -> 0;
            case "1", "正常", "已使用" -> 1;
            case "2", "异常", "废弃" -> 2;
            default -> 0;
        };
    }

    private static String generateSampleNo() {
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int num = SEQ.getAndIncrement() % 1000;
        return "BIO" + prefix + String.format("%03d", num);
    }
}
