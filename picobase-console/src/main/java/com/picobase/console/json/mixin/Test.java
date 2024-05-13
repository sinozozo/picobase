package com.picobase.console.json.mixin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picobase.model.schema.Schema;
import com.picobase.model.schema.SchemaField;

public class Test {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        var s = Schema.newSchema();
        var sf = new SchemaField();
        sf.setId("aaa");
        s.getFields().add(sf);


        objectMapper.addMixIn(Schema.class, SchemaMixIn.class);


        System.out.println(objectMapper.writeValueAsString(s));

        System.out.println(objectMapper.readValue(objectMapper.writeValueAsString(s), Schema.class));
/*
        String json = "[{\"id\":\"\",\"name\":\"field\",\"type\":\"text\",\"system\":false,\"required\":false,\"options\":{},\"onMountSelect\":false,\"originalName\":\"field\",\"toDelete\":false}]";

        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.addMixIn(Schema.class, SchemaMixIn.class);
        System.out.println(objectMapper1.readValue(json, Schema.class));*/
    }
}
