/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.jll.json.key.parse.ParsedKey;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author jon
 */
public class KeyParseBeanDeserializer<T> extends StdDeserializer<T> implements ResolvableDeserializer {

    private final JsonDeserializer<?> delegate;
    private final Class<?> beanType;

    public KeyParseBeanDeserializer(
            Class<?> beanType,
            JsonDeserializer<?> delegate) {
        super(beanType);
        this.beanType = beanType;
        this.delegate = delegate;
        System.out.println(
                "WRAPPING DELEGATE = " + delegate.getClass()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JacksonException {
        System.out.println("DESERIALIZE " + beanType);
        KeyParseJsonParser logicalParser
                = new KeyParseJsonParser(parser);

        /*
     * The delegate was created by Jackson normally.
     * We need a context whose active parser is our
     * KeyParseJsonParser.
         */
        DefaultDeserializationContext defaultContext
                = (DefaultDeserializationContext) ctxt;

        System.out.println("CURRENT TOKEN = " + parser.currentToken());
        System.out.println("CURRENT NAME = " + parser.currentName());
        System.out.println("CURRENT TEXT = " + parser.getText());

        DeserializationContext logicalContext
                = defaultContext.createInstance(
                        ctxt.getConfig(),
                        logicalParser,
                        null
                );

        JsonParser p = logicalContext.getParser();

        System.out.println("DESERIALIZE " + beanType);
        System.out.println("CURRENT TOKEN = " + p.currentToken());
        System.out.println("CURRENT NAME = " + p.currentName());

        try {
            if (delegate instanceof BeanDeserializer bd) {
                Iterator<SettableBeanProperty> it = bd.properties();

                while (it.hasNext()) {
                    SettableBeanProperty prop = it.next();

                    System.out.println(
                            "PROPERTY = " + prop.getName()
                            + ", DESER = " + prop.getValueDeserializer()
                    );
                }
            }
            T result = (T) delegate.deserialize(p, ctxt);
            System.out.println("DELEGATE SUCCESS");
            applyAttributes(result, logicalParser.getParsedKeys());

            return result;
        } catch (Exception e) {
            System.out.println("DELEGATE FAILED:");
            e.printStackTrace();
            throw e;
        }

    }

    private void applyAttributes(
            T result,
            Map<String, ParsedKey> parsedKeys)
            throws IOException {

        for (Field field : beanType.getDeclaredFields()) {

            PropertyAttribute annotation
                    = field.getAnnotation(PropertyAttribute.class);

            if (annotation == null) {
                continue;
            }

            ParsedKey key
                    = parsedKeys.get(annotation.key());

            if (key == null) {
                continue;
            }

            String value
                    = key.attributes()
                            .get(annotation.attribute());

            if (value == null) {
                continue;
            }

            try {
                field.setAccessible(true);

                Object converted
                        = convertAttribute(
                                value,
                                field.getType()
                        );

                field.set(result, converted);

            } catch (IllegalAccessException e) {

                throw new IOException(
                        "Unable to set KeyParse attribute '"
                        + annotation.attribute()
                        + "' on "
                        + field.getName(),
                        e
                );
            }
        }
    }

    private Object convertAttribute(
            String value,
            Class<?> type) {

        if (type == String.class) {
            return value;
        }

        if (type == int.class || type == Integer.class) {
            return Integer.valueOf(value);
        }

        if (type == long.class || type == Long.class) {
            return Long.valueOf(value);
        }

        if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(value);
        }

        if (type == double.class || type == Double.class) {
            return Double.valueOf(value);
        }

        if (type == float.class || type == Float.class) {
            return Float.valueOf(value);
        }

        throw new IllegalArgumentException(
                "Unsupported @KeyAttribute type: "
                + type.getName()
        );
    }

    @Override
    public void resolve(DeserializationContext ctxt)
            throws JsonMappingException {

        if (delegate instanceof ResolvableDeserializer resolvable) {
            resolvable.resolve(ctxt);
        }
    }

}
