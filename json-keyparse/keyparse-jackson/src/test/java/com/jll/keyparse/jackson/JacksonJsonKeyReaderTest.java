/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jll.json.key.parse.JsonKeyParse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jon
 */
//@Disabled
public class JacksonJsonKeyReaderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JacksonJsonKeyReaderTest() {
    }

    @org.junit.jupiter.api.BeforeAll
    public static void setUpClass() throws Exception {
    }

    @org.junit.jupiter.api.AfterAll
    public static void tearDownClass() throws Exception {
    }

    @org.junit.jupiter.api.BeforeEach
    public void setUp() throws Exception {
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    void shouldReadAttributesAtCurrentNode() throws Exception {

        String json = """
            {
                "normal": "value",
                "key:[attr1:val1,attr2:val2]": "custom value",
                "nested": {
                    "nested:[foo:bar]": "nested value"
                }
            }
            """;

        assertTrue(JsonKeyParse.parseKey("key:[attr1:val1,attr2:val2]").isPresent());

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root
                = (ObjectNode) mapper.readTree(json);

        JacksonJsonKeyReader reader
                = JacksonJsonKeyReader.of(root);

        Optional<Map<String, String>> attributes
                = reader.attributes("key");

        assertTrue(attributes.isPresent());

        assertEquals("val1", attributes.get().get("attr1"));
        assertEquals("val2", attributes.get().get("attr2"));
    }

    @Test
    void shouldNotReadNestedNode() throws Exception {
        String json = """
            {
                "nested": {
                    "key:[attr1:val1]": "nested value"
                }
            }
            """;

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root
                = (ObjectNode) mapper.readTree(json);

        JacksonJsonKeyReader reader
                = JacksonJsonKeyReader.of(root);

        assertTrue(reader.attributes("key").isEmpty());
    }

    @Test
    void shouldReadAttributesAfterExplicitNavigation() throws Exception {
        String json = """
            {
                "nested": {
                    "key:[attr1:val1]": "value"
                }
            }
            """;

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root
                = (ObjectNode) mapper.readTree(json);

        ObjectNode nested
                = (ObjectNode) root.get("nested");

        JacksonJsonKeyReader reader
                = JacksonJsonKeyReader.of(nested);

        Optional<Map<String, String>> attributes
                = reader.attributes("key");

        assertTrue(attributes.isPresent());
        assertEquals("val1", attributes.get().get("attr1"));
    }

    @Test
    void normalJacksonShouldNotResolveLogicalProperty() throws Exception {
        String json = """
        {
            "product:[type:card,id:123]": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();

        // No KeyParse registration.
//        Product product
//                = mapper.readValue(json, Product.class);
        assertThrows(UnrecognizedPropertyException.class, () -> {
            mapper.readValue(json, Product.class);
        });
        // Establish normal Jackson behavior.
    }

    @Test
    void shouldResolveLogicalProperty() throws Exception {
        String json = """
        {
            "product:[type:card,id:123]": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());
        // Whatever extension mechanism KP-006 discovers.
        Product product
                = mapper.readValue(json, Product.class);
        assertEquals("GD01-001", product.getProduct());
        assertEquals("card", product.getType());
        assertEquals("123", product.getId());
    }

    @Test
    void shouldExposeLogicalPropertyName() throws IOException {
        String json = """
        {
            "product:[type:card,id:123]": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();

        JsonParser original
                = mapper.getFactory().createParser(json);

        JsonParser parser
                = new KeyParseJsonParser(original);

        assertEquals(
                JsonToken.START_OBJECT,
                parser.nextToken()
        );

        assertEquals(
                JsonToken.FIELD_NAME,
                parser.nextToken()
        );

        assertEquals(
                "product",
                parser.currentName()
        );

        assertEquals(
                "product",
                parser.getText()
        );

        assertEquals(
                JsonToken.VALUE_STRING,
                parser.nextToken()
        );

        assertEquals(
                "GD01-001",
                parser.getText()
        );
    }

    @Test
    void shouldDeserializeThroughKeyParseParser()
            throws Exception {

        String json = """
        {
            "product:[type:card,id:123]": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();

        JsonParser original
                = mapper.getFactory().createParser(json);

        JsonParser parser
                = new KeyParseJsonParser(original);

        Product product
                = mapper.readValue(parser, Product.class);

        assertEquals(
                "GD01-001",
                product.getProduct()
        );
    }

    static class Product {

        private String product;

        @PropertyAttribute(key = "product", attribute = "id")
        private String id;

        @PropertyAttribute(key = "product", attribute = "type")
        private String type;

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

}
