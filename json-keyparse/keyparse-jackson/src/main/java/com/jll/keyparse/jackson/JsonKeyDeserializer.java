/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.jll.json.key.parse.JsonKeyParse;
import java.io.IOException;

/**
 *
 * @author jon
 */
public class JsonKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String string, DeserializationContext dc) throws IOException {
        return JsonKeyParse.parseKey(string);
    }

}
