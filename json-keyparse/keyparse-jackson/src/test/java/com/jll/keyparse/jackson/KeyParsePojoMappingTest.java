/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.jll.keyparse.jackson.JacksonJsonKeyReaderTest.Product;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
