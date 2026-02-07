package in.zeta.qa.utils.fileUtils;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import in.zeta.qa.utils.misc.JsonHelper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class FileReadHelper {

    private static final Logger log = LogManager.getLogger(FileReadHelper.class);

    private static final Map<String, Path> RESOURCE_CACHE = new HashMap<>();
    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir")).getParent();

    static {
        try (Stream<Path> paths = Files.walk(PROJECT_ROOT)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().contains(File.separator + "src" + File.separator + "main" + File.separator + "resources")
                            || p.toString().contains(File.separator + "src" + File.separator + "test" + File.separator + "resources"))                     .filter(p -> !p.toString().endsWith("java")) // only resoure files in src
                    .forEach(p -> {
                        // Get path relative to project root
                        String relativePath = PROJECT_ROOT.relativize(p).toString().replace("\\", "/");
                        // Remove module name prefix: keep from /src/... onward
                        int srcIndex = relativePath.indexOf("/src/");
                        if (srcIndex >= 0) {
                            relativePath = relativePath.substring(srcIndex);
                        }
                        // Ensure leading slash
                        if (!relativePath.startsWith("/")) {
                            relativePath = "/" + relativePath;
                        }
                        RESOURCE_CACHE.put(relativePath, p);
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan project files", e);
        }
    }

    /**
     * Reads CSV into a list of string arrays (default comma separator).
     */
    @SneakyThrows
    public static List<String[]> readCSV(File file) {
        try (var reader = new CSVReader(new FileReader(file, StandardCharsets.UTF_8))) {
            return reader.readAll();
        } catch (Exception e) {
            log.error("Failed to read CSV from file: {}", file, e);
            throw e;
        }
    }

    /**
     * Reads CSV with configurable separator.
     */
    @SneakyThrows
    public static List<String[]> readCSV(File file, char separator) {
        try (var fileReader = new FileReader(file, StandardCharsets.UTF_8)) {
            var csvParser = new CSVParserBuilder().withSeparator(separator).build();
            try (var reader = new CSVReaderBuilder(fileReader).withCSVParser(csvParser).build()) {
                return reader.readAll();
            }
        } catch (Exception e) {
            log.error("Failed to read CSV with separator '{}' from file: {}", separator, file, e);
            throw e;
        }
    }

    /**
     * Opens a CSVReader for streaming row-by-row access.
     */
    public static CSVReader openCsvReader(String filePath) throws IOException {
        var csvParser = new CSVParserBuilder().withSeparator(',').build();
        return new CSVReaderBuilder(new FileReader(filePath, StandardCharsets.UTF_8))
                .withCSVParser(csvParser)
                .build();
    }

    /**
     * Maps CSV rows directly into Java beans.
     */
    public static <T> List<T> getObjectsFromCsv(String filePath, Class<T> clazz) throws IOException {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            CsvToBean<T> cb = new CsvToBeanBuilder<T>(reader).withType(clazz).build();
            return cb.parse();
        } catch (Exception e) {
            log.error("Failed to map CSV to objects from: {}", filePath, e);
            throw e;
        }
    }

    /**
     * Reads a file fully into a string (UTF-8).
     */
    public static String readFileToString(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
    }

    /**
     * Reads a file containing one JSON object per line into a list of objects.
     */
    public static <T> List<T> readJsonStringListFromFile(File file, Class<T> clazz) throws IOException {
        List<T> list = new ArrayList<>();
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8)) {
            var jsonUtil = new JsonHelper();
            while (scanner.hasNextLine()) {
                list.add(jsonUtil.getObjectFromString(scanner.nextLine(), clazz));
            }
        }
        return list;
    }

    @SneakyThrows
    public static List<File> getFilesFromDirectory(String fileDir) {
        try (Stream<Path> paths = Files.walk(Paths.get(fileDir))) {
            return paths.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .toList(); // Java 16+ replaces collect(Collectors.toList())
        }
    }

    /**
     * Reads a file from the classpath into a String (UTF-8).
     * Works across modules, e.g., when running UPI module but resource is in common module.
     */
    public static String readFileScanningModules(String resourceRelativePath) throws IOException {
        Path fullPath = RESOURCE_CACHE.get(resourceRelativePath.replaceFirst("^\\.", ""));
        if (fullPath != null && Files.exists(fullPath)) {
            return Files.readString(fullPath, StandardCharsets.UTF_8);
        } else {
            throw new FileNotFoundException("Resource not found in project cache: " + resourceRelativePath);
        }
    }

}
