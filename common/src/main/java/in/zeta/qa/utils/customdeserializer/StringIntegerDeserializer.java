package in.zeta.qa.utils.customdeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class StringIntegerDeserializer extends StdDeserializer<Integer> {

    public StringIntegerDeserializer() {
        super(Integer.class);
    }

    @Override
    public Integer deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return jsonParser.getValueAsString().isEmpty() ? 0
                : jsonParser.getValueAsString().equalsIgnoreCase("NA") ? 0
                : jsonParser.getValueAsString().equalsIgnoreCase("Not Applicable") ? 0
                : StringUtils.isNumeric(jsonParser.getValueAsString().replaceAll("[^\\d]", "")) ?
                Integer.parseInt(jsonParser.getValueAsString().replaceAll("[^\\d]", ""))
                : 0;
    }
}
