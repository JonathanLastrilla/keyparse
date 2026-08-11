/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;

/**
 *
 * @author jon
 */
public class TestData {

    @JsonDeserialize(keyUsing = JsonKeyDeserializer.class)
    private Map<Map<String, String>, String> values;

    public Map<Map<String, String>, String> getValues() {
        return values;
    }

    public void setValues(Map<Map<String, String>, String> values) {
        this.values = values;
    }
}
