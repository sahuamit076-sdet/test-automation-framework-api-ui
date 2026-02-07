package in.zeta.qa.utils.customdeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;


public class SimulatorStringValueDeserializer extends JsonDeserializer<String> {

    @SneakyThrows
    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)  {
        JsonNode node = jsonParser.readValueAsTree();
        JsonNode valueNode = node.get("value");
        if (valueNode != null && valueNode.isTextual()) {
            return valueNode.asText();
        }
        return null;
    }
}
