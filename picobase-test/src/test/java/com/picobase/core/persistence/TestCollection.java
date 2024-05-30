package com.picobase.core.persistence;


import com.picobase.PbUtil;
import com.picobase.StartUpApplication;
import com.picobase.logic.mapper.CollectionMapper;
import com.picobase.model.CollectionModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = {StartUpApplication.class})
public class TestCollection {

    @Test
    public void test() {
        CollectionMapper mapper = PbUtil.findMapper(CollectionModel.class);
        CollectionModel users = mapper.findCollectionByNameOrId("users");
        System.out.println(users);

    }
}
