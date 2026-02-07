package in.zeta.qa.utils.fileUtils;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class YamlReader {

    private final Yaml yaml;

    public YamlReader() {
        yaml = new Yaml();
    }

    /**
     * Reads a YAML file from the given resource path and returns its contents as a Map.
     * @param resourcePath
     * @return
     */
    public Map<String, Object> readYaml(String resourcePath) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
            return yaml.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error reading the YAML file: " + resourcePath, e);
        }
    }
}
