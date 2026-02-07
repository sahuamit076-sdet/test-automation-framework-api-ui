package in.zeta.qa.utils.fileUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Utility class for serializing and deserializing objects to and from XML.
 */
public class XmlUtils {
    private static final Logger LOG = LogManager.getLogger(XmlUtils.class);

    private XmlMapper getXmlMapper() {
        XmlMapper mapper = new XmlMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
        mapper.setDefaultUseWrapper(false);
        return mapper;
    }

    @SneakyThrows
    public <T> T getPojoFromString(String xmlText, Class<T> classname) {
        return getXmlMapper().readValue(xmlText, classname);
    }

    @SneakyThrows
    public <T> T getPojoFromStringWithoutRootNameCheck(String xmlText, Class<T> classname) {
        return getXmlMapper().readerFor(classname).withoutRootName().readValue(xmlText);
    }

    /**
     * @param object
     * @param <T>
     * @return
     */
    public <T> String convertObjectToXmlString(T object) {
        try {
            return getXmlMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting object to XML string", e);
        }
    }

    /**
     * @param object
     * @param <T>
     * @return
     */
    public <T> String convertObjectToXmlStringWithRootNameForNPCI(T object, String rootName) {
        String PREFIX = "ns2:";
        String NAMESPACE = " xmlns:ns2=\"http://npci.org/upi/schema/\"";
        try {
            // Convert the object to XML with the specified root name
            String xml = getXmlMapper().writer().withRootName(PREFIX + rootName).writeValueAsString(object);
            // Dynamically replace the root element with the namespace
            xml = xml.replaceFirst("<" + PREFIX + rootName + ">",
                    "<" + PREFIX + rootName + NAMESPACE + ">");
            return xml;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting object to XML string", e);
        }
    }


    /**
     * Serializes the given object to XML and writes it to the specified output file.
     *
     * @param object     The object to serialize.
     * @param outputFile The file to write the serialized XML data to.
     * @param <T>        The type of the object being serialized.
     */
    public static <T> void serialize(T object, File outputFile) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            LOG.info("serialising object to xml");
            jaxbMarshaller.marshal(object, outputFile);
        } catch (Exception e) {
            LOG.warn("serialising failed with :: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deserializes an object from the specified XML file.
     *
     * @param xmlFile   The XML file to deserialize the object from.
     * @param valueType The class of the object to deserialize.
     * @param <T>       The type of the object being deserialized.
     * @return The deserialized object.
     */
    public static <T> T deserialize(File xmlFile, Class<T> valueType) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(valueType);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            LOG.info("de-serialising object to xml");
            return valueType.cast(unmarshaller.unmarshal(xmlFile));
        } catch (Exception e) {
            LOG.warn("deserialising failed with :: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
