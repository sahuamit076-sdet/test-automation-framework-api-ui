package in.zeta.qa.utils.customdeserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SimulatorCustomFieldsDeserializer extends JsonDeserializer<Map<String, Object>> {
    @Override
    public Map<String, Object> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        String value = jsonParser.getValueAsString();
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(jsonParser.getValueAsString())) return map;
        // Normalize delimiters (commas, spaces, newlines -> comma)
        String normalizedValue = value.replaceAll("[,\\s\\n\\r]+", ","); // Replace
        String[] pairs = normalizedValue.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String val = keyValue[1].trim();
                map.put(key, val);
            }
        }

        return map;
    }
}
