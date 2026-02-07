package in.zeta.qa.utils.fileUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class PropertyFileReader {

    private static volatile Properties properties;

    private PropertyFileReader() {
        // prevent instantiation
    }

    // ---------------------- Core Methods ----------------------

    private static Properties readPropertiesFromResource(String resourcePath) throws IOException {
        Properties prop = new Properties();
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (input != null) {
                prop.load(input);
            } else {
                log.error("Resource not found: {}", resourcePath);
            }
        }
        return prop;
    }

    public static Properties readPropertiesFile(String fileName) throws IOException {
        Properties prop = new Properties();
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                prop.load(input);
            }
        } else {
            log.info("File not found: {}", fileName);
        }
        return prop;
    }

    public static Properties getGlobalProperties() throws IOException {
        return readPropertiesFromResource("environments/application.properties");
    }

    private static void setPropertyIfAbsent(Properties props, String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            props.setProperty(key, systemValue);
        } else if (props.getProperty(key) == null && defaultValue != null) {
            props.setProperty(key, defaultValue);
        }
    }

    /**
     * Returns the system property if present; otherwise the property from the file;
     * if both are null, returns the defaultValue.
     *
     * @param props   The loaded properties
     * @param propKey The key in the property file
     * @return The resolved value
     */
    private static String getSystemOrLocalProperty(Properties props, String propKey) {
        return Objects.requireNonNullElse(System.getProperty(propKey), props.getProperty(propKey));
    }


    // ---------------------- Main Properties Loader ----------------------

    public static Properties getProperties() throws IOException {
        if (properties != null) return properties;
        log.info("loading global properties...");
        properties = getGlobalProperties();

        applyDefaultSystemProperties(properties);
        log.info("loading module-specific properties...");
        loadModuleProperty(properties, "environment", "application-%s.properties");
        loadModuleProperty(properties, "tenantId", "tenant_%s_token.properties");
        loadModuleProperty(properties, "coa", "coa_%s.properties");
        log.info("properties loaded, processing placeholders...");
        resolvePlaceholders(properties);
        log.info("completed loading properties...");
        return properties;
    }

    private static void resolvePlaceholders(Properties props) {
        for (String key : props.stringPropertyNames()) {
            resolve(key, props);
        }
    }

    private static void resolve(String key, Properties props) {
        String value = props.getProperty(key, "");
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(value);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String refKey = matcher.group(1);
            String refValue = props.getProperty(refKey, ""); // no recursion
            matcher.appendReplacement(sb, Matcher.quoteReplacement(refValue));
        }
        matcher.appendTail(sb);
        props.setProperty(key, sb.toString());
    }
    

    private static void loadModuleProperty(Properties props, String key, String resourcePattern) throws IOException {
        String value = getSystemOrLocalProperty(props, key);
        if (StringUtils.isNotBlank(value)) {
            props.setProperty(key, value);
            String resourceName = String.format(resourcePattern, value);

            String classpath = System.getProperty("java.class.path");
            Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent(); //parent repo root

            Arrays.stream(classpath.split(File.pathSeparator)).map(File::new)
                    // Only keep entries inside the project root
                    .filter(file -> file.toPath().startsWith(projectRoot))
                    .forEach(file -> {
                        if (file.isDirectory()) {
                            scanDirectoryForResource(props, file.toPath(), resourceName);
                        }
                    });
        }
    }

    private static void scanDirectoryForResource(Properties props, Path dir, String resourceName) {
        try (Stream<Path> paths = Files.walk(dir)) { // recursively walks all nested directories
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith("properties"))
                    .filter(p -> p.getFileName().toString().endsWith(resourceName))
                    .forEach(p -> {
                        try (InputStream is = Files.newInputStream(p)) {
                            Properties loaded = new Properties();
                            loaded.load(is);
                            props.putAll(loaded);
                            log.info("Loaded properties from file: {}", p);
                        } catch (IOException e) {
                            log.error("Failed to load properties from {}", p, e);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to scan directory {}", dir, e);
        }
    }

    private static void applyDefaultSystemProperties(Properties props) {
        List.of("GROUPS", "FEATURES", "TYPE", "LOCALE", "CHANNEL", "AUTH_TYPE", "ENTRY_MODE", "MODE")
                .forEach(key -> {
                    String value = props.stringPropertyNames().stream()
                            .filter(k -> k.equalsIgnoreCase(key))
                            .findFirst()
                            .map(props::getProperty)
                            .filter(v -> !v.isBlank())
                            .orElse("ALL");
                    props.setProperty(key, value);
                });
    }

    // ---------------------- Convenience Getters ----------------------

    @SneakyThrows(IOException.class)
    public static String getPropertyValue(String key) {
        return getProperties().getProperty(key);
    }

    @SneakyThrows(IOException.class)
    public static String getPropertyValueOrDefault(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    @SneakyThrows(IOException.class)
    public static boolean getBooleanPropertyValue(String key, boolean defaultValue) {
        String val = getProperties().getProperty(key);
        return StringUtils.isNotEmpty(val) ? Boolean.parseBoolean(val) : defaultValue;
    }

    @SneakyThrows(IOException.class)
    public static List<String> getListPropertyValue(String key) {
        String val = getProperties().getProperty(key);
        return StringUtils.isNotEmpty(val) ? List.of(val.split(",")) : Collections.emptyList();
    }

}