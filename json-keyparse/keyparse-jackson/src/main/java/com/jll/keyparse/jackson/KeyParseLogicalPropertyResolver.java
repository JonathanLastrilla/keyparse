/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jll.json.key.parse.JsonKeyParse;
import com.jll.json.key.parse.ParsedKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jon
 */
public class KeyParseLogicalPropertyResolver {

    public ObjectNode resolve(ObjectNode source)
            throws JsonMappingException {

        ObjectNode logicalNode
                = JsonNodeFactory.instance.objectNode();

        Set<String> exactProperties
                = new HashSet<>();

        Set<String> attributedLogicalNames
                = new HashSet<>();

        // First pass: exact properties.
        for (Map.Entry<String, JsonNode> entry
                : source.properties()) {

            String physicalName = entry.getKey();

            if (JsonKeyParse.parseKey(physicalName).isEmpty()) {

                exactProperties.add(physicalName);

                logicalNode.set(
                        physicalName,
                        entry.getValue()
                );
            }
        }

        // Second pass: attributed properties.
        for (Map.Entry<String, JsonNode> entry
                : source.properties()) {

            String physicalName = entry.getKey();

            ParsedKey parsedKey
                    = JsonKeyParse.parseKey(physicalName)
                            .orElse(null);

            if (parsedKey == null) {
                continue;
            }

            String logicalName = parsedKey.key();

            // Exact property has priority.
            if (exactProperties.contains(logicalName)) {
                continue;
            }

            // Multiple attributed properties are ambiguous.
            if (!attributedLogicalNames.add(logicalName)) {
                throw new JsonMappingException(
                        null,
                        "Ambiguous logical property: "
                        + logicalName
                );
            }

            logicalNode.set(
                    logicalName,
                    entry.getValue()
            );
        }

        return logicalNode;
    }
}
