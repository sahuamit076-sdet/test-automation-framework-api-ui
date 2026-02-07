package in.zeta.qa.utils.customdeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;


public class SimulatorIntegerValueDeserializer extends JsonDeserializer<Integer> {
    @SneakyThrows
    @Override
    public Integer deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)  {
        JsonNode node = jsonParser.readValueAsTree();
        JsonNode valueNode = node.get("value");
        if (valueNode != null && valueNode.isTextual()) {
            String value =  valueNode.asText();
            return Integer.parseInt(value);
        }
        return null;
    }
}
