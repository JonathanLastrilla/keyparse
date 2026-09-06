/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.jll.json.key.parse.ParsedKey;

/**
 *
 * @author jon
 */
public record LogicalProperty(
        String logicalName,
        String physicalName,
        ParsedKey parsedKey) {

}
