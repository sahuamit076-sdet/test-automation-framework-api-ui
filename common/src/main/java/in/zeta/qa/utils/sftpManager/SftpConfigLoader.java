package in.zeta.qa.utils.sftpManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class SftpConfigLoader {
    private static JsonNode configData;

    static {
        try (InputStream input = SftpConfigLoader.class.getClassLoader()
                .getResourceAsStream("configurations/sftp/sftpConfig.json")) {
            if (input == null) {
                throw new RuntimeException("config.json file not found in resources.");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            configData = objectMapper.readTree(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.json file.", e);
        }
    }

    public static SftpConfig getConfig(String presetName) {
        JsonNode presetNode = configData.get(presetName);
        if (presetNode == null) {
            throw new IllegalArgumentException("Preset '" + presetName + "' not found in config.json.");
        }

        return new SftpConfig(
                presetNode.get("host").asText(),
                presetNode.get("port").asInt(),
                presetNode.get("user").asText(),
                presetNode.get("password").asText(),
                presetNode.get("remoteDirectory").asText()
        );
    }
}
