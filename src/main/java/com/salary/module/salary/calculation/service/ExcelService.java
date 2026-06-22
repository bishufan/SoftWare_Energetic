package com.salary.module.salary.calculation.service;

import com.salary.module.org.repository.EmployeeRepository;
import com.salary.module.salary.item.SalaryItemRepository;
import com.salary.module.salary.item.domain.SalaryItem;
import com.salary.module.salary.calculation.domain.SalaryRecord;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelService {
    private final SalaryItemRepository salaryItemRepository;

    public Map<String, Map<Long, Double>> importVariableData(MultipartFile file) throws IOException {
        Map<String, Map<Long, Double>> result = new HashMap<>();
        List<SalaryItem> variableItems = salaryItemRepository.findByActiveTrueOrderBySortOrder()
                .stream().filter(i -> i.getFormula() == null || i.getFormula().isBlank()).toList();
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            Map<Integer, Long> colItemMap = new HashMap<>();
            for (int i = 1; i < headerRow.getLastCellNum(); i++) {
                String val = getCellValue(headerRow.getCell(i));
                for (SalaryItem item : variableItems) {
                    if (item.getName().equals(val)) colItemMap.put(i, item.getId());
                }
            }
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String empNo = getCellValue(row.getCell(0));
                if (empNo.isEmpty()) continue;
                Map<Long, Double> values = new HashMap<>();
                for (Map.Entry<Integer, Long> e : colItemMap.entrySet()) {
                    String v = getCellValue(row.getCell(e.getKey()));
                    if (!v.isEmpty()) values.put(e.getValue(), Double.parseDouble(v));
                }
                result.put(empNo, values);
            }
        }
        return result;
    }

    public byte[] exportToExcel(List<SalaryRecord> records) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("工资明细");
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = wb.createFont(); headerFont.setBold(true); headerStyle.setFont(headerFont);
            String[] headers = {"工资号","姓名","部门","应发合计","个税","社保","公积金","实发合计"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) { Cell c = headerRow.createCell(i); c.setCellValue(headers[i]); c.setCellStyle(headerStyle); }
            int rn = 1;
            for (SalaryRecord rec : records) {
                Row row = sheet.createRow(rn++);
                row.createCell(0).setCellValue(rec.getEmployee().getEmployeeNo());
                row.createCell(1).setCellValue(rec.getEmployee().getName());
                row.createCell(2).setCellValue(rec.getEmployee().getDepartment().getName());
                row.createCell(3).setCellValue(rec.getGrossPay());
                row.createCell(4).setCellValue(rec.getIncomeTax());
                row.createCell(5).setCellValue(rec.getSocialInsurance());
                row.createCell(6).setCellValue(rec.getHousingFund());
                row.createCell(7).setCellValue(rec.getNetPay());
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(); wb.write(bos); return bos.toByteArray();
        }
    }

    public byte[] exportTaxReportToExcel(List<Map<String, Object>> taxData) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("个税申报表");
            if (!taxData.isEmpty()) {
                Row headerRow = sheet.createRow(0);
                int ci = 0;
                for (String key : taxData.get(0).keySet()) headerRow.createCell(ci++).setCellValue(key);
                int rn = 1;
                for (Map<String, Object> rowData : taxData) {
                    Row row = sheet.createRow(rn++); int ci2 = 0;
                    for (Object val : rowData.values()) row.createCell(ci2++).setCellValue(val != null ? val.toString() : "");
                }
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream(); wb.write(bos); return bos.toByteArray();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
