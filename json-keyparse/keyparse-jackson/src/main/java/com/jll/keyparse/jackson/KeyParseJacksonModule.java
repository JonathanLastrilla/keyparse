/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jll.keyparse.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class KeyParseJacksonModule
        extends SimpleModule {

    public KeyParseJacksonModule() {

        super("KeyParseJackson");

        setDeserializerModifier(
                new BeanDeserializerModifier() {

            @Override
            public JsonDeserializer<?> modifyDeserializer(
                    DeserializationConfig config,
                    BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer) {
                System.out.println("modify " + beanDesc.getClass() + " -> " + deserializer.getClass());

                if (!(deserializer instanceof BeanDeserializer)) {
                    return deserializer;
                }
                return new KeyParseBeanDeserializer<>(
                        beanDesc.getBeanClass(),
                        deserializer
                );
            }
        }
        );
    }

}
