package in.zeta.qa.utils.customdeserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomIgnoreDeserializer extends JsonDeserializer<Map<String, Object>> {

    @Override
    public Map<String, Object> deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException, JacksonException {
        TreeNode treeNode = p.readValueAsTree();

        if (treeNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) treeNode;
            // Check if additionalProperties field is present
            if (objectNode.has("additionalProperties")) {
                // Ignore the "additionalProperties" field during deserialization
                objectNode.remove("additionalProperties");
            }

            // Deserialize the JSON object into a Map
            return p.getCodec().treeToValue(objectNode, HashMap.class);
        } else {
            throw new IOException("Expected JSON object");
        }
    }
}
