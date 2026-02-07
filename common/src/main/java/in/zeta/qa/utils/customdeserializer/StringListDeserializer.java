package in.zeta.qa.utils.customdeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StringListDeserializer extends StdDeserializer<List<String>> {

    public StringListDeserializer() {
        super(ArrayList.class);
    }

    @Override
    public List<String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        JsonNode node;
        try {
            node = jsonParser.getCodec().readTree(jsonParser);
        } catch (Exception e) {
            return new ArrayList<>(); // Return an empty list if there's an error reading the JSON
        }

        if (node.isTextual()) {
            String value = node.asText();
            if (value.contains("=")) {
                return Arrays.stream(value.split("=")[1].split(","))
                        .filter(Objects::nonNull).map(String::trim).collect(Collectors.toList());
            } else if (value.contains("\n")) {
                return Arrays.stream(value.split("\n"))
                        .filter(Objects::nonNull).map(String::trim).collect(Collectors.toList());
            } else {
                return Arrays.stream(value.replace("[", "").replace("]", "")
                        .split(",")).filter(Objects::nonNull).map(String::trim).collect(Collectors.toList());
            }
        } else {
            return new ArrayList<>();
        }

    }
}
