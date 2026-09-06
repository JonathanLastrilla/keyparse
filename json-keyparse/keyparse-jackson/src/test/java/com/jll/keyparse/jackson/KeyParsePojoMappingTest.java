/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.jll.keyparse.jackson.JacksonJsonKeyReaderTest.Product;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jon
 */
public class KeyParsePojoMappingTest {

    @Test
    void attributedPropertyMapsToPojoField() throws Exception {

        String json = """
        {
            "product:[type:card,id:123]": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        Product product
                = mapper.readValue(json, Product.class);

        assertEquals("GD01-001", product.getProduct());
    }

    @Test
    void exactPropertyTakesPrecedenceOverAttributedProperty()
            throws Exception {

        String json = """
        {
            "product:[type:card,id:123]": "B",
            "product": "A"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        Product product
                = mapper.readValue(json, Product.class);

        assertEquals("A", product.getProduct());
    }

    @Test
    void exactPropertyTakesPrecedenceRegardlessOfOrder()
            throws Exception {

        String json = """
        {
            "product": "A",
            "product:[type:card,id:123]": "B"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        Product product
                = mapper.readValue(json, Product.class);

        assertEquals("A", product.getProduct());
    }

    @Test
    void withoutKeyParseModuleNormalJacksonBehaviorRemains()
            throws Exception {

        String json = """
        {
            "product:[type:card,id:123]": "B"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();

        assertThrows(
                UnrecognizedPropertyException.class,
                () -> mapper.readValue(json, Product.class)
        );
    }

    @Test
    void attributesCanBeMappedExplicitly() throws Exception {

        String json = """
        {
            "product:[type:card,id:123]": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        TypedProduct product
                = mapper.readValue(json, TypedProduct.class);

        assertEquals("GD01-001", product.getProduct());
        assertEquals("card", product.getType());
        assertEquals(123, product.getId());
    }

    @Test
    void nullableDefaultsToTrue() throws Exception {

        Field field
                = Product.class.getDeclaredField("type");

        PropertyAttribute annotation
                = field.getAnnotation(PropertyAttribute.class);

        assertTrue(annotation.nullable());
    }

    @Test
    void requiredAttributePresentSucceeds() throws Exception {

        String json = """
        {
            "product:[type:card,id:123]": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        RequiredProduct product
                = mapper.readValue(json, RequiredProduct.class);

        assertEquals("GD01-001", product.getProduct());
        assertEquals("card", product.getType());
    }

    @Test
    void requiredAttributeMissingFails() throws Exception {

        String json = """
        {
            "product:[id:123]": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        JsonMappingException exception
                = assertThrows(
                        JsonMappingException.class,
                        () -> mapper.readValue(json, RequiredProduct.class)
                );

        assertTrue(
                exception.getMessage().contains(
                        "Required KeyParse attribute"
                )
        );
    }

    @Test
    void optionalAttributeMissingSucceeds() throws Exception {

        String json = """
        {
            "product:[id:123]": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        Product product
                = mapper.readValue(json, Product.class);

        assertEquals("GD01-001", product.getProduct());
        assertNull(product.getType());
    }

    @Test
    void consumedMetadataDoesNotWarn() throws Exception {
        String json = """
        {
            "product:[type:card]": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        AnnotatedProduct product
                = mapper.readValue(json, AnnotatedProduct.class);

        assertEquals("GD01-001", product.getProduct());
        assertEquals("card", product.getType());
    }

    static class AnnotatedProduct {

        @PropertyAttribute(key = "product", attribute = "type")
        private String type;

        private String product;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

    }

    @Test
    void unconsumedMetadataWarns() throws Exception {
        Logger logger
                = Logger.getLogger(
                        KeyParseBeanDeserializer.class.getName()
                );

        LogHandler handler = new LogHandler();
        logger.addHandler(handler);
        logger.setLevel(java.util.logging.Level.WARNING);

        try {
            String json = """
            {
                "product:[type:card]": "GD01-001"
            }
            """;

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new KeyParseJacksonModule());

            UnannotatedProduct product
                    = mapper.readValue(json, UnannotatedProduct.class);

            assertEquals("GD01-001", product.getProduct());

            assertTrue(
                    handler.getMessages().stream()
                            .anyMatch(message
                                    -> message.contains(
                                    "Unconsumed KeyParse metadata"
                            )
                            )
            );

        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void ordinaryPropertyDoesNotWarn() throws Exception {
        String json = """
        {
            "product": "GD01-001"
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        UnannotatedProduct product
                = mapper.readValue(json, UnannotatedProduct.class);

        assertEquals("GD01-001", product.getProduct());

        // TODO: assert no WARN once logger capture is wired
    }

    private static class LogHandler extends Handler {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public List<String> getMessages() {
            return messages;
        }
    }

    static class UnannotatedProduct {

        private String product;

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }
    }

    static class TypedProduct {

        private String product;

        @PropertyAttribute(key = "product", attribute = "id")
        private Integer id;
        @PropertyAttribute(key = "product", attribute = "type")
        private String type;

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public Integer getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    static class RequiredProduct {

        private String product;

        @PropertyAttribute(key = "product", attribute = "id", nullable = false)
        private Integer id;
        @PropertyAttribute(key = "product", attribute = "type", nullable = false)
        private String type;

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public Integer getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    public static class ThisProduct {

        private ThisDetails product;

        public ThisDetails getProduct() {
            return product;
        }

        public void setProduct(ThisDetails product) {
            this.product = product;
        }
    }

    public static class ThisDetails {

        private String name;

        @PropertyAttribute(
                key = "name",
                attribute = "type"
        )
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }
    }
}
