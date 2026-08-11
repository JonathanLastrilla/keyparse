/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jll.json.key.parse.JsonKeyParse;
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

}
