package in.zeta.qa.utils.misc;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.zeta.qa.constants.CommonConstants;
import in.zeta.qa.utils.customdeserializer.CustomIgnoreDeserializer;
import in.zeta.qa.utils.fileUtils.FileReadHelper;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;


public class JsonHelper implements CommonConstants {

    private static final Logger LOG = LogManager.getLogger(JsonHelper.class);
    // 🔹 JSON Path
    private static final String DOT_REGEX = "\\.";
    private static final String LEFT_BRACKET = "[";
    private static final String RIGHT_BRACKET = "]";
    private static final String ROOT = "$";

    // ============================
    // 🔹 ObjectMapper Config
    // ============================

    public ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        mapper.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    public ObjectMapper getCustomDeserializeMapper() {
        ObjectMapper mapper = getMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Map.class, new CustomIgnoreDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    // ====================================================================================
    // 🔹 JSON ↔ Map / Object Conversions
    // ====================================================================================

    public Map<String, Object> convertJsonToMap(String json) {
        try {
            return getMapper().readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String convertMapToJsonString(Map<String, Object> jsonMap) {
        try {
            return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> String convertObjectToJsonString(T object) {
        try {
            return getMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> JsonNode convertObjectToJsonNode(T object) {
        try {
            return convertToJsonNode(getMapper().writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> convertObjectToList(Object obj) {
        List<String> list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            Object[] array = (Object[]) obj;
            for (Object element : array) {
                list.add(String.valueOf(element));
            }
        } else if (obj instanceof Collection) {
            for (Object element : (Collection<?>) obj) {
                list.add(String.valueOf(element));
            }
        }
        return list;
    }

    // ====================================================================================
    // 🔹 JSON ↔ JsonNode Handling
    // ====================================================================================

    @SneakyThrows
    public JsonNode convertToJsonNode(String response) {
        ObjectMapper mapper = getMapper();
        return mapper.readTree(response);
    }

    @SneakyThrows
    public JsonNode convertToJsonNode(Response response) {
        return getMapper().readTree(response.body().asString());
    }

    @SneakyThrows
    public JsonNode convertToJsonNodeFromFile(String filepath) {
        String jsonContent = FileReadHelper.readFileScanningModules(filepath);
        return getMapper().readTree(jsonContent);
    }

    public String convertJsonNodeToString(JsonNode jsonNode) throws Exception {
        return getMapper().writeValueAsString(jsonNode);
    }

    public String convertJsonNodeToString(List<JsonNode> nodes) throws Exception {
        return getMapper().writeValueAsString(nodes);
    }

    public String toPrettyJsonString(JsonNode jsonNode) throws Exception {
        return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    public String prettyPrintJson(String json) {
        try {
            Object jsonObj = getMapper().readValue(json, Object.class);
            return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
        } catch (Exception e) {
            return json; // Fallback
        }
    }

    // ============================
    // 🔹 JSON Validation & Checks
    // ============================

    public boolean isValidJson(String jsonString) {
        try {
            getMapper().readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isJsonArrayWithElements(String jsonString) {
        JsonNode node = convertToJsonNode(jsonString);
        return node.isArray() && !node.isEmpty();
    }

    // ============================
    // 🔹 JSON ↔ Object Deserialization
    // ============================

    @SneakyThrows
    public <T> T getObjectFromResponse(Response response, Class<T> classname) {
        return getObjectFromString(response.body().asString(), classname);
    }

    @SneakyThrows
    public <T> T getObjectFromString(String messageText, Class<T> classname) {
        if (messageText == null || messageText.isEmpty()) {
            LOG.warn("Empty or null JSON string provided for deserialization to {}", classname.getSimpleName());
            Assert.fail("Empty or null JSON string provided for deserialization to " + classname.getSimpleName());
            return null;
        }
        return getMapper().readValue(messageText, classname);
    }

    public <T> T getObjectFromStringUsingCustomObjectMapper(String messageText, Class<T> classname) throws Exception {
        return getCustomDeserializeMapper().readValue(messageText, classname);
    }

    public <T> List<T> getObjectsFromString(String messageText, Class<T> classname) throws Exception {
        CollectionType listType = getMapper().getTypeFactory()
                .constructCollectionType(ArrayList.class, classname);
        return getMapper().readValue(messageText, listType);
    }

    public <T> T getObjectFromJsonFile(String filePath, Class<T> classname) throws Exception {
        String jsonData = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
        return getObjectFromString(jsonData, classname);
    }

    public <T> List<T> getObjectsFromJsonFile(String filePath, Class<T> classname) throws Exception {
        String jsonData = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
        return getObjectsFromString(jsonData, classname);
    }

    public <T> T getObjectFromJsonNode(JsonNode jsonNode, Class<T> clazz) {
        try {
            String json = convertJsonNodeToString(jsonNode);
            return getMapper().readValue(json, clazz);
        } catch (Exception ignored) {
        }
        return null;
    }

    public <T> List<T> getObjectsFromJsonArrNode(JsonNode jsonNode, Class<T> clazz) {
        if (!jsonNode.isArray()) return new ArrayList<>();
        try {
            String json = convertJsonNodeToString(jsonNode);
            return getMapper().readValue(json,
                    getMapper().getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            return null;
        }
    }

    // ============================
    // 🔹 JSON Searching
    // ============================

    public Optional<JsonNode> searchPath(JsonNode node, String path) {
        String[] parts = path.split(DOT_REGEX);
        JsonNode current = node;

        for (String part : parts) {
            if (part.contains(LEFT_BRACKET)) {
                String fieldName = part.substring(0, part.indexOf(LEFT_BRACKET));
                int index = Integer.parseInt(part.substring(part.indexOf(LEFT_BRACKET) + 1, part.indexOf(RIGHT_BRACKET)));
                current = current.get(fieldName);
                if (current != null && current.isArray()) {
                    current = current.get(index);
                } else return Optional.empty();
            } else {
                current = current.get(part);
            }
            if (current == null) return Optional.empty();
        }
        return Optional.of(current);
    }

    public Optional<Object> searchKey(JsonNode node, String searchKey) {
        if (node.has(searchKey)) {
            var foundNode = node.get(searchKey);
            return switch (foundNode.getNodeType()) {
                case STRING -> Optional.of(foundNode.asText());
                case NUMBER -> Optional.of(foundNode.isIntegralNumber() ? foundNode.asLong() : foundNode.asDouble());
                case BOOLEAN -> Optional.of(foundNode.asBoolean());
                default -> Optional.of(foundNode);
            };
        }

        if (node.isArray() || node.isObject()) {
            var children = node.isArray() ? node.spliterator()
                    : ((Iterable<JsonNode>) node::elements).spliterator();
            return StreamSupport.stream(children, false)
                    .map(child -> searchKey(child, searchKey))
                    .flatMap(Optional::stream)
                    .findFirst();
        }
        return Optional.empty();
    }

    // ============================
    // 🔹 Utility & Reflection
    // ============================

    public <T> void setProperty(String key, String value, T clsObj) {
        Predicate<Field> filterByMatchingJsonProperty = f -> f.getAnnotation(JsonProperty.class) != null
                && key.matches(".*" + f.getAnnotation(JsonProperty.class).value());
        Predicate<Field> filterByMatchingJsonAliasProperty = f -> f.getAnnotation(JsonAlias.class) != null
                && Arrays.stream(f.getAnnotation(JsonAlias.class).value()).anyMatch(v -> key.matches(".*" + v));

        Optional<Field> field = Arrays.stream(clsObj.getClass().getDeclaredFields())
                .filter(filterByMatchingJsonProperty.or(filterByMatchingJsonAliasProperty))
                .findFirst();
        field.ifPresent(f -> {
            try {
                f.setAccessible(true);
                f.set(clsObj, value);
            } catch (IllegalAccessException ignored) {
            }
        });
    }

    public Map<String, String> loadDataUsingKey(String filePath, String parentKey) throws IOException {
        JsonNode rootNode = getMapper().readTree(new File(filePath));
        JsonNode parentNode = Objects.requireNonNull(rootNode.get(parentKey),
                () -> "Invalid parent key: " + parentKey);
        return getMapper().convertValue(parentNode, new TypeReference<>() {
        });
    }

    public static Object stringify(Object x) {
        if (x instanceof JSONObject obj) {
            obj.keySet().forEach(k -> obj.put(k, stringify(obj.get(k))));
            return obj;
        }
        if (x instanceof JSONArray arr) {
            for (int i = 0; i < arr.length(); i++) {
                arr.put(i, stringify(arr.get(i)));
            }
            return arr;
        }
        return String.valueOf(x);
    }

    @SafeVarargs
    public static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();
        return t -> {
            var keys = Arrays.stream(keyExtractors)
                    .map(k -> k.apply(t))
                    .toList();
            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    // ============================
    // 🔹 Generic Deserialization
    // ============================

    public static <T> T deserialize(String json, Class<T> targetType) {
        try {
            return new JsonHelper().getMapper().readValue(json, targetType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing JSON to " + targetType.getSimpleName(), e);
        }
    }

    public <T> T deserialize(String json, TypeReference<T> typeReference) {
        try {
            return getMapper().readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing JSON to " + typeReference.getType().getTypeName(), e);
        }
    }

    public static <T> T deserializeFromFile(String filePath, Class<T> targetType) {
        ObjectMapper mapper = new JsonHelper().getMapper();
        try {
            String jsonContent = FileReadHelper.readFileScanningModules(filePath);
            return mapper.readValue(jsonContent, targetType);
        } catch (IOException e) {
            throw new RuntimeException("Error reading or deserializing file: " + filePath, e);
        }
    }
}
