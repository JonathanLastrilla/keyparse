/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.jll.json.key.parse.JsonKeyParse;
import com.jll.json.key.parse.ParsedKey;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author jon
 */
public class KeyParseJsonParser extends JsonParserDelegate {

    ParsedKey currentParsedKey;
    private final Map<String, ParsedKey> parsedKeys = new LinkedHashMap();
    private final Map<String, LogicalProperty> logicalProperties
            = new LinkedHashMap<>();

    private final LogicalPropertyResolver resolver = new LogicalPropertyResolver();

    public KeyParseJsonParser(JsonParser d) {
        super(d);
    }

    @Override
    public String getCurrentName() throws IOException {
        return logicalName(delegate.currentName());
    }

    @Override
    public String currentName() throws IOException {
        return logicalName(delegate.currentName());
    }

    @Override
    public String getText() throws IOException {
        String text = delegate.getText();
        if (delegate.currentToken() == JsonToken.FIELD_NAME && text != null) {
            return logicalName(text);
        }
        return text;
    }

    @Override
    public String nextFieldName() throws IOException {
        return logicalName(delegate.nextFieldName());
    }

    private String logicalName(String name) {
        if (name == null) {
            currentParsedKey = null;
            return null;
        }

        currentParsedKey = JsonKeyParse.parseKey(name)
                .orElse(null);

        if (currentParsedKey != null) {

            parsedKeys.put(
                    currentParsedKey.key(),
                    currentParsedKey
            );

            logicalProperties.put(
                    name,
                    new LogicalProperty(
                            currentParsedKey.key(),
                            name,
                            currentParsedKey
                    )
            );

//            resolver.register(logicalProperties.get(name));
            return currentParsedKey.key();
        }

        return name;
    }

    public LogicalPropertyResolver getResolver() {
        return resolver;
    }

    public Map<String, LogicalProperty> getLogicalProperties() {
        return Collections.unmodifiableMap(logicalProperties);
    }

    public ParsedKey getCurrentParsedKey() {
        return currentParsedKey;
    }

    public Map<String, ParsedKey> getParsedKeys() {
        return parsedKeys;
    }

    public String getPhysicalCurrentName() throws IOException {
        return delegate.currentName();
    }

}
