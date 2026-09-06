package com.jll.keyparse.jackson;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LogicalPropertyResolver {

    private final Map<String, LogicalProperty> exactProperties
            = new LinkedHashMap<>();

    private final Map<String, LogicalProperty> attributedProperties
            = new LinkedHashMap<>();

    public void register(LogicalProperty property) {
        String logicalName = property.logicalName();

        if (property.parsedKey() == null) {
            exactProperties.put(logicalName, property);
            return;
        }

        if (attributedProperties.containsKey(logicalName)) {
            throw new IllegalArgumentException(
                    "Ambiguous logical property: " + logicalName
            );
        }

        attributedProperties.put(logicalName, property);
    }

    public LogicalProperty resolve(String logicalName) {

        LogicalProperty exact
                = exactProperties.get(logicalName);

        if (exact != null) {
            return exact;
        }

        return attributedProperties.get(logicalName);
    }
}
