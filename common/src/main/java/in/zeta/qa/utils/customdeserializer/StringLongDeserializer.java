package in.zeta.qa.utils.customdeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;

public class StringLongDeserializer extends StdDeserializer<Long> {

    public StringLongDeserializer() {
        super(ArrayList.class);
    }

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return jsonParser.getValueAsString().isEmpty() ? 0
                : jsonParser.getValueAsString().equalsIgnoreCase("NA") ? 0
                : StringUtils.isNumeric(jsonParser.getValueAsString().replaceAll("[^\\d]", "")) ?
                Long.parseLong(jsonParser.getValueAsString().replaceAll("[^\\d]", ""))
                : 0;
    }
}
