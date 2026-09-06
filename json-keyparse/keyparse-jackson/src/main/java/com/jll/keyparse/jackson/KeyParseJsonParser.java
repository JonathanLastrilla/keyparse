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

/**
 *
 * @author jon
 */
public class KeyParseJsonParser extends JsonParserDelegate {

    public KeyParseJsonParser(JsonParser d) {
        super(d);
    }

    @Override
    public String getCurrentName() throws IOException {
        String name = delegate.currentName();
        if (name == null) {
            return null;
        }

        return JsonKeyParse.parseKey(name)
                .map(ParsedKey::key)
                .orElse(name);
    }

    @Override
    public String currentName() throws IOException {
        String name = delegate.currentName();
        if (name == null) {
            return null;
        }

        return JsonKeyParse.parseKey(name)
                .map(ParsedKey::key)
                .orElse(name);
    }

    @Override
    public String getText() throws IOException {
        String text = delegate.getText();
        if (delegate.currentToken() == JsonToken.FIELD_NAME && text != null) {
            return JsonKeyParse.parseKey(text)
                    .map(ParsedKey::key)
                    .orElse(text);
        }
        return text;
    }

    @Override
    public String nextFieldName() throws IOException {
        String name = delegate.nextFieldName();
        if(name==null){
            return null;
        }
        return JsonKeyParse.parseKey(name)
                .map(ParsedKey::key)
                .orElse(name);
    }

    
}
