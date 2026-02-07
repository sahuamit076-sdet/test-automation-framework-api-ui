package in.zeta.qa.utils.customdeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;


public class SimulatorPrivateDataDE44FieldSerializer extends JsonDeserializer<String> {
    @SneakyThrows
    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)  {
        JsonNode node = jsonParser.readValueAsTree();
        JsonNode valueNode = node.get("value");
        if (valueNode != null && valueNode.isTextual()) {
            String DE44tag2 =  valueNode.asText();
            return (DE44tag2.length() > 1) ? DE44tag2.substring(1, 2) : DE44tag2;
        }
        return null;
    }
}
