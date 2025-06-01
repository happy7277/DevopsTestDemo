package com.example.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public final class UserExport {
    private UserExport() {}

    static byte[] write(List<User> users) throws IOException {
        var workbook = new SXSSFWorkbook(100);
        try (var bytes = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Users");
            var header = sheet.createRow(0);
            var columns = List.of("ID", "Name", "Email", "Status");
            for (int index = 0; index < columns.size(); index++) {
                header.createCell(index).setCellValue(columns.get(index));
            }
            int rowIndex = 1;
            for (var user : users) {
                var row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(user.id.toString());
                row.createCell(1).setCellValue(user.name);
                row.createCell(2).setCellValue(user.email);
                row.createCell(3).setCellValue(user.status.name());
            }
            workbook.write(bytes);
            return bytes.toByteArray();
        }
    }
}
