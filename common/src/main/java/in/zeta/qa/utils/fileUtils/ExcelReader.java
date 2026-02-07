package in.zeta.qa.utils.fileUtils;

import lombok.SneakyThrows;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.StringJoiner;


public class ExcelReader {


    /**
     * Reads entire XLSX file as a string (tab-delimited).
     */
    @SneakyThrows
    public String readFromXls(String filePath) {
        StringJoiner content = new StringJoiner(System.lineSeparator());
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                StringJoiner rowContent = new StringJoiner("\t");
                for (Cell cell : row) {
                    rowContent.add(getCellAsString(cell));
                }
                content.add(rowContent.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }
        return content.toString();
    }

    /**
     * Returns a Workbook instance from file path.
     */
    @SneakyThrows
    public Workbook getWorkBook(String filePath) {
        try {
            return new XSSFWorkbook(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open workbook: " + filePath, e);
        }
    }

    /**
     * Returns a Workbook instance using FileInputStream.
     */
    public Workbook getFisWorkBook(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return new XSSFWorkbook(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open workbook via FIS: " + filePath, e);
        }
    }

    /**
     * Returns cell value as string for given sheet/row/col.
     */
    public String getCellValue(String filePath, String sheetName, int rowNum, int colNum) {
        try (Workbook workbook = getWorkBook(filePath)) {
            Sheet sheet = Objects.requireNonNullElseGet(
                    workbook.getSheet(sheetName),
                    () -> workbook.createSheet(sheetName)
            );
            Row row = sheet.getRow(rowNum);
            if (row == null) return null;
            Cell cell = row.getCell(colNum);
            return getCellAsString(cell);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read cell value", e);
        }
    }

    /**
     * Returns cell value as string from sheet reference.
     */
    public String getCellValue(Sheet sheet, int rowNum, int colNum) {
        Row row = sheet.getRow(rowNum);
        if (row == null) return null;
        Cell cell = row.getCell(colNum);
        return getCellAsString(cell);
    }

    /**
     * Utility method to convert cell to String safely.
     */
    private String getCellAsString(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case NUMERIC, FORMULA -> String.valueOf((int) cell.getNumericCellValue());
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case BLANK -> "";
            default -> "UNSUPPORTED";
        };
    }


}