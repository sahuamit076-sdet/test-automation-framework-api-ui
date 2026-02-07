package in.zeta.qa.utils.customdeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Arrays;

public class StringBooleanDeserializer extends StdDeserializer<Boolean> {

    public StringBooleanDeserializer() {
        super(Boolean.class);
    }

    @Override
    public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (jsonParser.getValueAsString().isEmpty()) return null;
        return jsonParser.getValueAsString().isEmpty() ? null
                : Arrays.asList("Y", "YES").contains(jsonParser.getValueAsString().trim().toUpperCase())
                || !Arrays.asList("N", "NO").contains(jsonParser.getValueAsString().trim().toUpperCase())
                && Boolean.parseBoolean(jsonParser.getValueAsString());
    }
}
