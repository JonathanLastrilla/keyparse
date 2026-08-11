/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.jll.json.key.parse;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author jon
 */
public class JsonKeyParse {

    private static final String ATTR_MAP_REGEX = "^\\[[^:\\],]+:[^:\\],]+(?:,[^:\\],]+:[^:\\],]+)*\\]\\s*$";
    private static final Pattern ATTR_MAP_PATTERN;
    private static final Logger LOG = Logger.getLogger(JsonKeyParse.class.getName());

    static {
        ATTR_MAP_PATTERN = Pattern.compile(ATTR_MAP_REGEX);
    }

    public static void main(String[] args) {

    }

    /**
     * read a compliant key from the JSON and parse the attributes.
     *
     * @param jsonKey
     * @return if pattern is not compliant, silently throw error log
     */
    public static Optional<ParsedKey> parseKey(String jsonKey) {
        String[] splitted = splitIfCompliant(jsonKey);
        if (splitted.length != 2) {
            return Optional.empty();
        }
        String unparsedAttrMap = splitted[1];
        if (!validateFormat(unparsedAttrMap)) {
            LOG.log(Level.SEVERE, "Unable to parse attributes, values: ".concat(unparsedAttrMap));
            return Optional.empty();
        }
        Map<String, String> attrMap
                = Arrays.stream(unparsedAttrMap.substring(1, unparsedAttrMap.length() - 1).split(","))
                        .map(attr -> attr.split(":", 2))
                        .collect(Collectors.toMap(
                                pair -> pair[0].trim(),
                                pair -> pair[1].trim()
                        ));

        return Optional.of(new ParsedKey(splitted[0], attrMap));

    }

    private static String[] splitIfCompliant(String key) {
        int marker = key.indexOf(":[");
        String[] splitted = key.split(":\\[");

        if (marker != -1) {
            splitted[1] = "[".concat(splitted[1]);
        }

        return -1 != marker ? splitted : new String[]{key};
    }

    private static boolean validateFormat(String attrMap) {
        return ATTR_MAP_PATTERN.matcher(attrMap).matches();
    }
}
