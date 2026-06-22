package com.salary.module.salary.payslip;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.colors.ColorConstants;
import com.salary.module.salary.calculation.domain.SalaryRecord;
import com.salary.module.salary.calculation.repository.SalaryRecordRepository;
import com.salary.module.salary.item.SalaryItemRepository;
import com.salary.module.salary.item.domain.SalaryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayslipService {
    private final SalaryRecordRepository recordRepository;
    private final SalaryItemRepository salaryItemRepository;
    private final ObjectMapper objectMapper;

    public byte[] generatePdf(Long recordId) {
        SalaryRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("工资记录不存在"));
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(bos));
            Document doc = new Document(pdf);
            doc.add(new Paragraph("工资条").setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("姓名: "+record.getEmployee().getName()));
            doc.add(new Paragraph("工资号: "+record.getEmployee().getEmployeeNo()));
            doc.add(new Paragraph("部门: "+record.getEmployee().getDepartment().getName()));
            doc.add(new Paragraph("月份: "+record.getYearMonth()));
            doc.add(new Paragraph(" "));

            Table table = new Table(2);
            table.addHeaderCell(new Cell().add(new Paragraph("项目").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("金额(元)").setBold()));

            Map<String, Double> itemValues = objectMapper.readValue(record.getItemValues(), new TypeReference<>() {});
            Map<String, String> codeNameMap = salaryItemRepository.findByActiveTrueOrderBySortOrder().stream()
                    .collect(Collectors.toMap(SalaryItem::getCode, SalaryItem::getName));

            for (Map.Entry<String, Double> e : itemValues.entrySet()) {
                table.addCell(codeNameMap.getOrDefault(e.getKey(), e.getKey()));
                table.addCell(String.format("%.2f", e.getValue()));
            }
            table.addCell(new Cell().add(new Paragraph("应发合计").setBold()));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", record.getGrossPay())).setBold()));
            table.addCell("个税"); table.addCell(String.format("%.2f", record.getIncomeTax()));
            table.addCell("社保(个人)"); table.addCell(String.format("%.2f", record.getSocialInsurance()));
            table.addCell("公积金(个人)"); table.addCell(String.format("%.2f", record.getHousingFund()));
            table.addCell(new Cell().add(new Paragraph("实发合计").setBold().setFontColor(ColorConstants.RED)));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", record.getNetPay())).setBold().setFontColor(ColorConstants.RED)));
            doc.add(table); doc.close();
            return bos.toByteArray();
        } catch (Exception e) { throw new RuntimeException("生成PDF工资条失败: "+e.getMessage()); }
    }

    public String generateHtml(Long recordId) {
        SalaryRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("工资记录不存在"));
        try {
            Map<String, Double> itemValues = objectMapper.readValue(record.getItemValues(), new TypeReference<>() {});
            Map<String, String> codeNameMap = salaryItemRepository.findByActiveTrueOrderBySortOrder().stream()
                    .collect(Collectors.toMap(SalaryItem::getCode, SalaryItem::getName));
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body style='font-family:SimSun,serif;padding:20px;'>");
            sb.append("<h2 style='text-align:center;'>工资条</h2>");
            sb.append("<table style='width:100%;border-collapse:collapse;'>");
            sb.append("<tr><td>姓名: ").append(record.getEmployee().getName()).append("</td>");
            sb.append("<td>工资号: ").append(record.getEmployee().getEmployeeNo()).append("</td></tr>");
            sb.append("<tr><td>部门: ").append(record.getEmployee().getDepartment().getName()).append("</td>");
            sb.append("<td>月份: ").append(record.getYearMonth()).append("</td></tr></table><br/>");
            sb.append("<table border='1' style='width:100%;border-collapse:collapse;text-align:center;'>");
            sb.append("<tr style='background:#f0f0f0;'><th>项目</th><th>金额(元)</th></tr>");
            for (Map.Entry<String, Double> e : itemValues.entrySet()) {
                sb.append("<tr><td>").append(codeNameMap.getOrDefault(e.getKey(), e.getKey())).append("</td>");
                sb.append("<td>").append(String.format("%.2f", e.getValue())).append("</td></tr>");
            }
            sb.append("<tr style='background:#f0f0f0;'><td><b>应发合计</b></td><td><b>").append(String.format("%.2f", record.getGrossPay())).append("</b></td></tr>");
            sb.append("<tr><td>个税</td><td>").append(String.format("%.2f", record.getIncomeTax())).append("</td></tr>");
            sb.append("<tr><td>社保(个人)</td><td>").append(String.format("%.2f", record.getSocialInsurance())).append("</td></tr>");
            sb.append("<tr><td>公积金(个人)</td><td>").append(String.format("%.2f", record.getHousingFund())).append("</td></tr>");
            sb.append("<tr style='background:#fff0f0;'><td><b>实发合计</b></td><td><b style='color:red;'>").append(String.format("%.2f", record.getNetPay())).append("</b></td></tr>");
            sb.append("</table></body></html>");
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException("生成HTML工资条失败: "+e.getMessage()); }
    }
}
