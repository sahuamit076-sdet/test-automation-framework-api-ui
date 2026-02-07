package in.zeta.qa.utils.customdeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class StringDoubleDeserializer extends StdDeserializer<Double> {

    public StringDoubleDeserializer() {
        super(Double.class);
    }
    @Override
    public Double deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return jsonParser.getValueAsString().isEmpty() ? null
                : jsonParser.getValueAsString().equalsIgnoreCase("NA") ||  jsonParser.getValueAsString().equalsIgnoreCase("N")? 0
                : Double.parseDouble(jsonParser.getValueAsString().replaceAll("[^\\d.]", ""));
    }
}
