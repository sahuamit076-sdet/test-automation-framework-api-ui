package in.zeta.qa.utils.fileUtils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class ExcelWriter {

    ExcelReader excelReader;
    private static final Logger log = LogManager.getLogger(ExcelWriter.class);

    public ExcelWriter() {
        this.excelReader = new ExcelReader();
    }

    /**
     * Create a new Excel file with a single sheet.
     */
    public void createExcelSheet(String filePath, String sheetName) {
        try (Workbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(filePath)) {
            wb.createSheet(sheetName);
            wb.write(fos);
            log.info("Excel file created: {} with sheet '{}'", filePath, sheetName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Excel file: " + filePath, e);
        }
    }

    /**
     * Update or create a cell at row/col with value.
     */
    public void updateCellValue(Workbook workbook, String fileName, Sheet sheet, int rowNum, int colNum, String value) {
        var row = Objects.requireNonNullElseGet(sheet.getRow(rowNum), () -> sheet.createRow(rowNum));
        var cell = row.createCell(colNum, CellType.STRING);
        cell.setCellValue(value);
        saveAfterUpdate(fileName, workbook);
    }

    /**
     * Append value in sheet by creating new row if required.
     */
    public void updateCellValue(Workbook workbook, String fileName, String sheetName, int colNum, String value) {
        var sheet = workbook.getSheet(sheetName);
        if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);

        int lastRow = sheet.getLastRowNum();
        // Create new row if writing to first column, otherwise update last row
        var row = (colNum == 0) ? sheet.createRow(lastRow + 1) : sheet.getRow(lastRow);
        if (row == null) row = sheet.createRow(lastRow + 1);

        var cell = row.createCell(colNum, CellType.STRING);
        cell.setCellValue(value);
        saveAfterUpdate(fileName, workbook);
    }

    /**
     * Find row index that matches a given value in column.
     */
    public int getRowNumMatchingValue(String fileName, String sheetName, int colNum, String value) {
        try (Workbook workbook = excelReader.getWorkBook(fileName)) {
            if (workbook == null) return -1;

            var sheet = workbook.getSheet(sheetName);
            if (sheet == null) return -1;

            int lastRow = sheet.getLastRowNum();
            for (int i = 1; i <= lastRow; i++) {
                var row = sheet.getRow(i);
                if (row == null) continue;
                var cell = row.getCell(colNum);
                if (cell == null) continue;

                String cellValue = excelReader.getCellValue(sheet, i, colNum);
                if (cellValue != null && cellValue.trim().contains(value)) {
                    return i;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading workbook: " + fileName, e);
        }
        return -1; // not found
    }

    /**
     * Save workbook to file after updates.
     */
    private void saveAfterUpdate(String fileName, Workbook workbook) {
        try (FileOutputStream fos = new FileOutputStream(Path.of(fileName).toFile())) {
            workbook.write(fos);
            log.info("Workbook saved successfully: {}", fileName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save workbook: " + fileName, e);
        }
    }


}