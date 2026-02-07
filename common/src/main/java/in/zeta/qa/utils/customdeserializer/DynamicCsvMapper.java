package in.zeta.qa.utils.customdeserializer;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import in.zeta.qa.constants.anotation.CsvDynamicBind;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicCsvMapper<T> {
    private final Class<T> clazz;

    public DynamicCsvMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    public List<T> readCsv(String filePath) throws IOException, CsvException {
        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            String[] header = Arrays.stream(csvReader.readNext()) // Read header line
                    .map(String::trim)
                    .toArray(String[]::new);
            Stream<String[]> csvLines = csvReader.readAll().stream();
            return csvLines
                    .parallel()
                    .filter(line -> !(line.length < header.length))
                    .map(line -> Arrays.stream(line).map(String::trim).toArray(String[]::new))
                    .map(line -> processCsvLine(line, header))
                    .collect(Collectors.toList());
        }
    }

    public List<T> readCsvFromString(String csvContent) throws IOException, CsvException {
        try (CSVReader csvReader = new CSVReader(new StringReader(csvContent))) {
            String[] header = Arrays.stream(csvReader.readNext()) // Read header line
                    .map(String::trim)
                    .toArray(String[]::new);
            Stream<String[]> csvLines = csvReader.readAll().stream();
            return csvLines
                    .parallel()
                    .filter(line -> !(line.length < header.length))
                    .map(line -> Arrays.stream(line).map(String::trim).toArray(String[]::new))
                    .map(line -> processCsvLine(line, header))
                    .collect(Collectors.toList());
        }
    }

    private T processCsvLine(String[] line, String[] header) {
        T obj = instantiateObject();
        for (int i = 0; i < header.length; i++) {
            String columnName = header[i].toLowerCase();
            setFieldValue(obj, columnName, line[i]);
        }
        return obj;
    }

    private T instantiateObject() {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating an instance of " + clazz.getSimpleName(), e);
        }
    }

    private void setFieldValue(T obj, String columnName, String value) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(CsvDynamicBind.class)) {
                CsvDynamicBind dynamicBindAnnotation = field.getAnnotation(CsvDynamicBind.class);
                String[] possibleNames = dynamicBindAnnotation.value();
                if (Arrays.stream(possibleNames).anyMatch(name -> name.equalsIgnoreCase(columnName))) {
                    field.setAccessible(true);
                    try {
                        field.set(obj, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error setting field value", e);
                    }
                    break;
                }
            }
        }
    }
}