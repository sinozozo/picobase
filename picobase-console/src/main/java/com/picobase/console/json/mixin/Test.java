package com.picobase.console.json.mixin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picobase.model.CollectionModel;
import com.picobase.model.schema.Schema;
import com.picobase.model.schema.SchemaField;

public class Test {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        var c = new CollectionModel();

        var s = new Schema();
        var sf = new SchemaField();
        sf.setId("aaa");
        s.getFields().add(sf);



        objectMapper.addMixIn(Schema.class, SchemaMixIn.class);


        System.out.println(objectMapper.writeValueAsString(c.setSchema(s)));

    }
}
