package in.zeta.qa.utils.fileUtils;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import in.zeta.qa.utils.misc.AllureLoggingUtils;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CSVUtil {

    private static final Logger LOG = LogManager.getLogger(CSVUtil.class);

    /**
     * Creates a CSV file with header from an Enum and data rows.
     */
    public <E extends Enum<E>> void createCSV(String fileName, Class<E> enumClass, List<List<String>> data) {
        var file = Path.of(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            // Write header
            var header = Arrays.stream(enumClass.getEnumConstants())
                    .map(Enum::name)
                    .collect(Collectors.joining(","));
            writer.write(header);
            writer.newLine();

            // Write data rows
            for (var row : data) {
                writer.write(String.join(",", row));
                writer.newLine();
            }

            AllureLoggingUtils.logsToAllureReport("CSV file created successfully: " + file.toAbsolutePath());
            LOG.info("CSV file created successfully: {}", file.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create CSV file: " + fileName, e);
        }
    }

    /**
     * Writes plain lines to a CSV file.
     */
    public File writeToCsv(String filePath, List<String> lines) {
        var file = Path.of(filePath).toFile();
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            for (var line : lines) {
                writer.write(line);
                writer.newLine();
            }
            LOG.info("CSV written successfully: {}", file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write to CSV: " + filePath, e);
        }
    }

    /**
     * Creates CSV with a custom header object and data rows.
     */
    @SneakyThrows
    public <T, H> File createCsvFileWithHeader(String filePath, H header, List<T> data) {
        var csvMapper = new CsvMapper();
        var headerSchema = csvMapper.schemaFor(header.getClass()).withoutHeader().withoutQuoteChar();

        var file = new File(filePath);
        LOG.info("Writing CSV to {}", file.getAbsolutePath());

        try (var writer = new BufferedWriter(new FileWriter(file, false))) {
            csvMapper.writer(headerSchema).writeValue(writer, header);
        }

        if (data != null && !data.isEmpty()) {
            var txnSchema = csvMapper.schemaFor(data.get(0).getClass())
                    .withoutHeader()
                    .withoutQuoteChar()
                    .withColumnReordering(true);

            try (var writer = new BufferedWriter(new FileWriter(file, true))) {
                csvMapper.writer(txnSchema).writeValue(writer, data);
            }
        }

        return file;
    }

    /**
     * Creates CSV with header from the object's class.
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public <T> File createCsvFileWithHeader(String filePath, List<T> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be null or empty");
        }
        CsvMapper csvMapper = new CsvMapper();
        Class<T> clazz = (Class<T>) data.get(0).getClass();
        CsvSchema schema = csvMapper.schemaFor(clazz).withHeader().withoutQuoteChar();
        File file = new File(filePath);
        csvMapper.writer(schema).writeValue(file, data);
        LOG.info("CSV created with header: {}", file.getAbsolutePath());
        return file;
    }


    /**
     * Appends lines to an existing CSV file.
     */
    public void appendLines(File file, List<String> lines) {
        try {
            Files.write(file.toPath(), lines, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            LOG.info("Lines appended to CSV: {}", file.getAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to append lines to CSV: " + file, e);
        }
    }
}
