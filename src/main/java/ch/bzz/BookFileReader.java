package ch.bzz;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class BookFileReader {

    List<Book> read(String filePath) throws IOException {
        List<String[]> rows = readRows(filePath);
        if (rows.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> columns = mapColumns(rows.get(0));
        if (!columns.containsKey("isbn") || !columns.containsKey("title") || !columns.containsKey("author")) {
            throw new IOException("File must have isbn, title and author columns (found header: "
                    + String.join(", ", columns.keySet()) + ")");
        }

        List<Book> books = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String isbn = valueAt(row, columns.get("isbn"));
            String title = valueAt(row, columns.get("title"));
            String author = valueAt(row, columns.get("author"));
            if (isbn == null || title == null || author == null) {
                continue;
            }
            Integer publicationYear = parseYear(valueAt(row, columns.get("publication_year")));
            books.add(new Book(null, isbn, title, author, publicationYear));
        }
        return books;
    }

    private List<String[]> readRows(String filePath) throws IOException {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".xlsx")) {
            return readExcelRows(filePath);
        }
        if (lower.endsWith(".tsv")) {
            return readDelimitedRows(filePath, "\t");
        }
        if (lower.endsWith(".csv")) {
            return readDelimitedRows(filePath, ",");
        }
        throw new IOException("Unsupported file type (expected .xlsx, .tsv or .csv)");
    }

    private List<String[]> readExcelRows(String filePath) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (FileInputStream fileIn = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                int lastColumn = row.getLastCellNum();
                String[] values = new String[Math.max(lastColumn, 0)];
                for (int c = 0; c < values.length; c++) {
                    values[c] = getCellAsString(row.getCell(c));
                }
                rows.add(values);
            }
        }
        return rows;
    }

    private List<String[]> readDelimitedRows(String filePath, String delimiter) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    line = stripBom(line);
                    firstLine = false;
                }
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(delimiter, -1);
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].trim();
                }
                rows.add(fields);
            }
        }
        return rows;
    }

    private String stripBom(String line) {
        return (!line.isEmpty() && line.charAt(0) == '﻿') ? line.substring(1) : line;
    }

    private Map<String, Integer> mapColumns(String[] header) {
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            String name = header[i] == null ? "" : header[i].trim().toLowerCase();
            if (name.equals("author") || name.equals("authors")) {
                columns.put("author", i);
            } else if (name.equals("publication_year") || name.equals("year")) {
                columns.put("publication_year", i);
            } else if (!name.isEmpty()) {
                columns.put(name, i);
            }
        }
        return columns;
    }

    private String valueAt(String[] row, Integer index) {
        if (index == null || index >= row.length) {
            return null;
        }
        String value = row[index];
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private Integer parseYear(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getCellAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        String value = cell.getStringCellValue().trim();
        return value.isEmpty() ? null : value;
    }
}
