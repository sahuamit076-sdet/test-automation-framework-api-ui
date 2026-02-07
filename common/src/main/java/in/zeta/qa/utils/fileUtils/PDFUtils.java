package in.zeta.qa.utils.fileUtils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PDFUtils {

    private static final Logger LOG = LogManager.getLogger(PDFUtils.class);

    @SneakyThrows
    public static Map<String, List<String>> readTableFromPDF(String filePath) {
        String text = readPDF(filePath);
        Map<String, List<String>> table = new LinkedHashMap<>();
        Map<Integer, String> columnNameMap = new LinkedHashMap<>();
        String[] lines = text.split("\\r?\\n"); // Split by new lines
        Map.Entry<Integer, Integer> rowColumnIndicesEntry = getFirstRowAndColumnIndex(lines);
        int firstRowIndex = rowColumnIndicesEntry.getKey();
        int firstColumnIndex = rowColumnIndicesEntry.getValue();
        if (firstRowIndex < 0) throw new RuntimeException("PDF file is empty.");
        String firstRow = lines[firstRowIndex];
        String[] cellsFromFirstRow = firstRow.trim().split("\\s"); // Split by one space
        for (int j = firstColumnIndex; j < cellsFromFirstRow.length; j++) {
            String columnName = cellsFromFirstRow[j];
            columnNameMap.put(j, columnName);
            table.put(columnName, new ArrayList<>());
        }
        for (int i = firstRowIndex + 1; i < lines.length; i++) {
            String line = lines[i];
            String[] cells = line.trim().split("\\s"); // Split by one space
            for (int j = firstColumnIndex; j < cells.length; j++) {
                String cell = cells[j];
                String columnName = columnNameMap.get(j);
                List<String> values = table.get(columnName);
                values.add(cell);
                table.put(columnName, values);
            }
        }
        return table;
    }

    private static Map.Entry<Integer, Integer> getFirstRowAndColumnIndex(String[] rows) {
        int rowCounter = 0;
        int cellCounter = 0;
        while (rowCounter < rows.length) {
            String row = rows[rowCounter];
            String[] cells = row.trim().split("\\s");
            while (cellCounter < cells.length) {
                String cell = cells[cellCounter];
                if (cell != null && !cell.isEmpty()) {
                    return new AbstractMap.SimpleEntry<>(rowCounter, cellCounter);
                }
                cellCounter++;
            }
            rowCounter++;
        }
        throw new RuntimeException("Given PDF file is empty.");
    }

    public static String readPDF(String filePath) throws IOException {
        File file = new File(filePath);
        StringBuilder content = new StringBuilder();
        try (PDDocument document = PDDocument.load(file)) {
            if (!document.isEncrypted()) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                content.append(pdfStripper.getText(document));
            } else {
                System.err.println("The document is encrypted and cannot be read.");
            }
        }
        return content.toString();
    }

    @SneakyThrows
    public static String[] readPdfFromUrl(String urlForPdf) {
        urlForPdf = urlForPdf.contains(" ")
                ? urlForPdf.replaceAll(" ", "%20")
                : urlForPdf;
        URL url = new URL(urlForPdf);
        LOG.info("Open a connection to the URL");
        URLConnection urlConnection = url.openConnection();
        LOG.info("Open the input stream from the URL connection");
        InputStream inputStream = urlConnection.getInputStream();
        PDDocument loadedPdf = PDDocument.load(inputStream);
        PDFTextStripper textStripper = new PDFTextStripper();
        final String parsedText = textStripper.getText(loadedPdf);
        return parsedText.split("\\n");
    }

    @SneakyThrows
    public static String convertDateToDiffTimeZone(String inputDate, String timeZone) {
        if (StringUtils.isNotEmpty(inputDate)) {
            DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter newYorkFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            try {
                LocalDateTime localDateTime = LocalDateTime.parse(inputDate, utcFormatter);
                ZonedDateTime utcDateTime = localDateTime.atZone(ZoneId.of("UTC"));

                // Convert to New York time zone
                ZonedDateTime newYorkDateTime = utcDateTime.withZoneSameInstant(ZoneId.of(timeZone));
                return newYorkDateTime.format(newYorkFormatter);
            } catch (Exception e) {
                System.err.println("Invalid date format: " + inputDate);
            }
        }
        return "";
    }

}
