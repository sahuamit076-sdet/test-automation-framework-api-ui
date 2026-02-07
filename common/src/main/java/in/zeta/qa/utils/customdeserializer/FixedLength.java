package in.zeta.qa.utils.customdeserializer;

import in.zeta.qa.constants.anotation.FixedField;
import in.zeta.qa.constants.anotation.StartWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixedLength {
    private final List<Class<?>> lineTypes = new ArrayList<>();

    public FixedLength registerLineType(Class<?> recordClass) {
        lineTypes.add(recordClass);
        return this;
    }

    // Parse the fixed-length file after specific start strings and set values for Java objects
    public List<Object> parse(InputStream inputStream) throws IOException, IllegalAccessException, InstantiationException {
        List<Object> records = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        Map<Class<?>, String> startStrings = new HashMap<>();
        for (Class<?> recordClass : lineTypes) {
            String startString = getStartString(recordClass);
            if (!startString.isEmpty()) {
                startStrings.put(recordClass, startString);
            }
        }

        String line;
        while ((line = reader.readLine()) != null) {
            for (Class<?> recordClass : lineTypes) {
                String startString = startStrings.get(recordClass);
                if (startString != null && line.startsWith(startString)) {
                    Object record = parseRecord(line, recordClass);
                    if (record != null) {
                        records.add(record);
                    }
                    break;
                }
            }
        }

        return records;
    }


    // Parse a single record line
    private Object parseRecord(String line, Class<?> recordClass) throws IllegalAccessException, InstantiationException {
        Object record = null;
        if (line.startsWith(getStartString(recordClass))) {
            record = recordClass.newInstance();

            Field[] fields = recordClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(FixedField.class)) {
                    FixedField annotation = field.getAnnotation(FixedField.class);
                    int offset = annotation.offset()-1;
                    int length = annotation.length();
                    if (offset + length <= line.length()) {
                        String value = line.substring(offset, offset + length);
                        field.setAccessible(true);
                        field.set(record, value);
                    } else {
                        throw new IllegalArgumentException("Field length exceeds line length");
                    }
                }
            }
        }
        return record;
    }
    // Utility method to get the start string from the annotation
    private static String getStartString(Class<?> recordClass) {
        if (recordClass.isAnnotationPresent(StartWith.class)) {
            StartWith startWith = recordClass.getAnnotation(StartWith.class);
            return startWith.value();
        }
        return "";
    }
}