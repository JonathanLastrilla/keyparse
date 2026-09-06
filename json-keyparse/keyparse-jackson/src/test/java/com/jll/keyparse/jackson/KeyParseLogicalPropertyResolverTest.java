/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jll.json.key.parse.JsonKeyParse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jon
 */
public class KeyParseLogicalPropertyResolverTest {

    @Test
    void exactPropertyWinsOverAttributedProperty() throws Exception {
        String json = """
    {
        "product": "A",
        "product:[type:card,id:123]": "B"
    }
    """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        JacksonJsonKeyReaderTest.Product product = mapper.readValue(json, JacksonJsonKeyReaderTest.Product.class);

        assertEquals("A", product.getProduct());
    }

    @Test
    void attributedPropertyResolvesToLogicalProperty() throws Exception {
        String json = """
    {
        "product:[type:card,id:123]": "B"
    }
    """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        JacksonJsonKeyReaderTest.Product product = mapper.readValue(json, JacksonJsonKeyReaderTest.Product.class);

        assertEquals("B", product.getProduct());
    }

    @Test
    void multipleAttributedPropertiesWithSameLogicalKeyAreRejected()
            throws Exception {

        String json = """
    {
        "product:[id:123]": "A",
        "product:[id:456]": "B"
    }
    """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        assertThrows(
                JsonMappingException.class,
                () -> mapper.readValue(json, JacksonJsonKeyReaderTest.Product.class)
        );
    }

    @Test
    void differentLogicalPropertiesResolveIndependently()
            throws Exception {

        String json = """
    {
        "product:[id:123]": "GD01-001",
        "name:[lang:en]": "Gundam"
    }
    """;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new KeyParseJacksonModule());

        JacksonJsonKeyReaderTest.Product product = mapper.readValue(json, JacksonJsonKeyReaderTest.Product.class);

        assertEquals("GD01-001", product.getProduct());
        assertEquals("Gundam", product.getName());
    }

    @Test
    void exactPropertyHasPriority() {
        LogicalPropertyResolver resolver
                = new LogicalPropertyResolver();

        resolver.register(
                new LogicalProperty(
                        "product",
                        "product",
                        null
                )
        );

        resolver.register(
                new LogicalProperty(
                        "product",
                        "product:[id:123]",
                        JsonKeyParse.parseKey(
                                "product:[id:123]"
                        ).orElseThrow()
                )
        );

        LogicalProperty resolved
                = resolver.resolve("product");

        assertEquals("product", resolved.physicalName());
    }

    @Test
    void duplicateAttributedPropertiesAreRejected() {
        LogicalPropertyResolver resolver
                = new LogicalPropertyResolver();

        resolver.register(
                new LogicalProperty(
                        "product",
                        "product:[id:123]",
                        JsonKeyParse.parseKey(
                                "product:[id:123]"
                        ).orElseThrow()
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.register(
                        new LogicalProperty(
                                "product",
                                "product:[id:456]",
                                JsonKeyParse.parseKey(
                                        "product:[id:456]"
                                ).orElseThrow()
                        )
                )
        );
    }
}
