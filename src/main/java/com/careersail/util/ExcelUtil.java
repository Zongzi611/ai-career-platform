package com.careersail.util;

import cn.hutool.core.bean.BeanUtil;
import com.careersail.common.BusinessException;
import com.careersail.common.ErrorCode;
import com.careersail.entity.CareerInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel 导入导出工具 (Apache POI)
 */
@Slf4j
public final class ExcelUtil {

    private ExcelUtil() {}

    /**
     * 从 Excel 文件导入职业数据
     */
    public static List<CareerInfo> importCareerExcel(MultipartFile file) {
        List<CareerInfo> list = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            // Header row mapping
            String[] headers = {"positionName", "industry", "majorMatch", "salaryMin", "salaryMax",
                    "skillsRequired", "careerPath", "description", "demandLevel"};

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                CareerInfo info = new CareerInfo();
                for (int j = 0; j < headers.length && j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) continue;
                    String value = getCellValue(cell);
                    try {
                        Field field = CareerInfo.class.getDeclaredField(headers[j]);
                        field.setAccessible(true);
                        if (field.getType() == Integer.class && j >= 3 && j <= 4) {
                            field.set(info, (int) Double.parseDouble(value));
                        } else {
                            field.set(info, value);
                        }
                    } catch (Exception ignored) {}
                }
                if (info.getPositionName() != null && !info.getPositionName().isBlank()) {
                    list.add(info);
                }
            }
        } catch (IOException e) {
            log.error("Excel import failed", e);
            throw new BusinessException(ErrorCode.CAREER_IMPORT_FAILED);
        }
        return list;
    }

    /**
     * 导出为 Excel 字节流
     */
    public static byte[] exportToExcel(List<Map<String, Object>> data, String[] headers, String[] keys) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            // Header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> record = data.get(i);
                for (int j = 0; j < keys.length; j++) {
                    Object value = record.get(keys[j]);
                    if (value != null) {
                        row.createCell(j).setCellValue(value.toString());
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Excel导出失败");
        }
    }

    private static String getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
