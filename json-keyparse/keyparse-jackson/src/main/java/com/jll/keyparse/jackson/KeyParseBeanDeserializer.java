/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jll.json.key.parse.JsonKeyParse;
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
            ObjectNode rawNode = readObject(logicalParser, logicalContext);
            validateLogicalProperties(logicalParser);
            ObjectNode logicalNode = JsonNodeFactory.instance.objectNode();
            for (Map.Entry<String, JsonNode> entry : rawNode.properties()) {
                String physicalName = entry.getKey();
                if (JsonKeyParse.parseKey(physicalName).isEmpty()) {
                    logicalNode.set(physicalName, entry.getValue());
                }
            }

            for (Map.Entry<String, JsonNode> entry : rawNode.properties()) {
                String physicalName = entry.getKey();
                ParsedKey parsedKey = JsonKeyParse.parseKey(physicalName)
                        .orElse(null);
                if (parsedKey == null) {
                    continue;
                }
                String logicalName = parsedKey.key();
                if (logicalNode.has(logicalName)) {
                    continue;
                }

                logicalNode.set(logicalName, entry.getValue());
            }

            System.out.println("RAW NODE: " + rawNode);
            System.out.println("LOGICAL NODE: " + logicalNode);
            JsonParser resolvedParser = logicalNode.traverse(logicalContext.getParser().getCodec()
            );

            resolvedParser.nextToken();

            DefaultDeserializationContext defaultResolvedContext
                    = (DefaultDeserializationContext) ctxt;

            DeserializationContext resolvedContext = defaultResolvedContext
                    .createInstance(ctxt.getConfig(), resolvedParser, null);

            T result = (T) delegate.deserialize(resolvedParser, resolvedContext);

            System.out.println("DELEGATE SUCCESS");

            applyAttributes(result, logicalParser.getParsedKeys());

            return result;
        } catch (IOException e) {
            System.out.println("DELEGATE FAILED:");
            e.printStackTrace();
            throw e;
        }

    }

    private ObjectNode readObject(KeyParseJsonParser parser, DeserializationContext ctxt)
            throws IOException {

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            throw JsonMappingException.from(parser, "Expected START_OBJECT");
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String physicalName = parser.getPhysicalCurrentName();
            parser.getCurrentName();
            parser.nextToken();
            JsonNode value = ctxt.readTree(parser);
            node.set(physicalName, value);
        }
        return node;
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

    private void validateLogicalProperties(KeyParseJsonParser parser) throws JsonMappingException {
        LogicalPropertyResolver resolver = new LogicalPropertyResolver();
        try {
            parser.getLogicalProperties().values()
                    .stream()
                    .forEach(resolver::register);
        } catch (IllegalArgumentException e) {
            throw JsonMappingException.from(parser, e.getMessage(), e);
        }
    }

    private ObjectNode resolveLogicalProperties(ObjectNode rawNode, KeyParseJsonParser parser) {

        ObjectNode result = JsonNodeFactory.instance.objectNode();
        LogicalPropertyResolver resolver = new LogicalPropertyResolver();
        for (Map.Entry<String, LogicalProperty> entry : parser.getLogicalProperties().entrySet()) {
            resolver.register(entry.getValue());
        }

        for (Map.Entry<String, JsonNode> entry : rawNode.properties()) {
            String physicalName = entry.getKey();
            LogicalProperty property = parser.getLogicalProperties().get(physicalName);
            if (property == null) {
                result.set(physicalName, entry.getValue());
                continue;
            }

            LogicalProperty resolved = resolver.resolve(property.logicalName());
            if (resolved == null) {
                continue;
            }

            if (!resolved.physicalName().equals(physicalName)) {
                continue;
            }

            result.set(resolved.logicalName(), entry.getValue());
        }
        return result;
    }

}
