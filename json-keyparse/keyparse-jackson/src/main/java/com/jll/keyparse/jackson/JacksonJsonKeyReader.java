/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jll.json.key.parse.JsonKeyParse;
import com.jll.json.key.parse.ParsedKey;
import java.util.Map;

public final class JacksonJsonKeyReader {

    private final ObjectNode node;

    private JacksonJsonKeyReader(ObjectNode node) {
        this.node = node;
    }

    public static JacksonJsonKeyReader of(ObjectNode node) {
        return new JacksonJsonKeyReader(node);
    }

    public Optional<ParsedKey> findKey(String key) {

        Iterator<String> fields = node.fieldNames();

        while (fields.hasNext()) {

            String field = fields.next();

            Optional<ParsedKey> parsed
                    = JsonKeyParse.parseKey(field);

            if (parsed.isPresent()
                    && parsed.get().key().equals(key)) {

                return parsed;
            }
        }

        return Optional.empty();
    }

    public Optional<JsonNode> valueOf(ParsedKey key) {

        Iterator<String> fields = node.fieldNames();

        while (fields.hasNext()) {

            String field = fields.next();

            Optional<ParsedKey> parsed
                    = JsonKeyParse.parseKey(field);

            if (parsed.isPresent()
                    && parsed.get().equals(key)) {

                return Optional.ofNullable(node.get(field));
            }
        }

        return Optional.empty();
    }

    public ObjectNode node() {
        return node;
    }

    public Optional<Map<String, String>> attributes(String key) {
        return findKey(key).map(ParsedKey::attributes);
    }
}
