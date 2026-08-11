/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.json.key.parse;

import java.util.Map;

/**
 *
 * @author jon
 */
public record ParsedKey(
        String key,
        Map<String, String> attributes
        ) {

}
