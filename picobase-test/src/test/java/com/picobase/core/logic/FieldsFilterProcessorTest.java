package com.picobase.core.logic;

import cn.hutool.json.JSONUtil;
import com.picobase.logic.FieldsFilterProcessor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FieldsFilterProcessorTest {

    static Stream<Arguments> testPickFieldsData() {
        return Stream.of(
                Arguments.of("empty fields",
                        Map.of("a", 1, "b", 2, "c", "test"),
                        "",
                        false,
                        "{\"a\":1,\"b\":2,\"c\":\"test\"}"
                ),
                Arguments.of("missing fields",
                        new HashMap<>(Map.of("a", 1, "b", 2, "c", "test")),
                        "missing",
                        false,
                        "{}"),
                Arguments.of("non map data",
                        "test",
                        "a,b,test",
                        false,
                        "test"),
                Arguments.of("non slice of map data",
                        new ArrayList<String>(List.of("a", "b", "test")),
                        "a,test",
                        false,
                        "[\"a\",\"b\",\"test\"]"),
                Arguments.of("map with no matching field",
                        new HashMap<String, Object>(Map.of("a", 1, "b", 2, "c", "test")),
                        "missing",
                        false,
                        "{}"
                ),
                Arguments.of("map with existing and missing fields",
                        new HashMap<String, Object>(Map.of("a", 1, "b", 2, "c", "test")),
                        "a, c ,missing",
                        false,
                        "{\"a\":1,\"c\":\"test\"}"
                ),
                Arguments.of("slice of maps with existing and missing fields",
                        new ArrayList<Object>(List.of(new HashMap(Map.of("a", 11, "b", 11, "c", "test1")),
                                new HashMap(Map.of("a", 22, "b", 22, "c", "test2")))),
                        "a, c ,missing",
                        false,
                        "[{\"a\":11,\"c\":\"test1\"},{\"a\":22,\"c\":\"test2\"}]"
                )
                ,
                Arguments.of("nested fields with mixed map and any slices",
                        getComplexMap(),
                        "a, c, anySlice.A, mapSlice.C, mapSlice.D.DA, anySlice.D,fullMap",
                        false,
                        "{\"a\":1,\"anySlice\":[{\"A\":[1,2,3],\"D\":{\"DA\":1,\"DB\":2}},{\"A\":\"test\"}],\"c\":\"test\",\"fullMap\":[{\"A\":[1,2,3],\"B\":[\"1\",\"2\",3],\"C\":\"test\"},{\"B\":[\"1\",\"2\",3],\"D\":[{\"DA\":2},{\"DA\":3}]}],\"mapSlice\":[{\"C\":\"test\",\"D\":[{\"DA\":1}]},{\"D\":[{\"DA\":2},{\"DA\":3},{}]}]}"
                ),
                Arguments.of("root wildcard with nested exception",
                        new HashMap<String, Object>(
                                Map.of("id", "123", "title", "lorem", "rel", new HashMap<String, Object>(Map.of("id", "456", "title", "rel_title"
                                )))),
                        "*,rel.id",
                        false,
                        "{\"id\":\"123\",\"rel\":{\"id\":\"456\"},\"title\":\"lorem\"}"
                ),
                Arguments.of("sub wildcard",
                        new HashMap<String, Object>(
                                Map.of("id", "123", "title", "lorem", "rel",
                                        new HashMap<String, Object>(Map.of("id", "456", "title", "rel_title",
                                                "sub", new HashMap<String, Object>(Map.of("id", "789", "title", "sub_title")))
                                        ))),
                        "id,rel.*",
                        false,
                        "{\"id\":\"123\",\"rel\":{\"id\":\"456\",\"sub\":{\"id\":\"789\",\"title\":\"sub_title\"},\"title\":\"rel_title\"}}"
                ),
                Arguments.of("sub wildcard with nested exception",
                        new HashMap<String, Object>(
                                Map.of("id", "123", "title", "lorem", "rel",
                                        new HashMap<String, Object>(Map.of("id", "456", "title", "rel_title",
                                                "sub", new HashMap<String, Object>(Map.of("id", "789", "title", "sub_title")))
                                        ))),
                        "id,rel.*,rel.sub.id",
                        false,
                        "{\"id\":\"123\",\"rel\":{\"id\":\"456\",\"sub\":{\"id\":\"789\"},\"title\":\"rel_title\"}}"
                ),
                Arguments.of("invalid excerpt modifier",
                        new HashMap<>(Map.of("a", 1, "b", 2, "c", "test")),
                        "*:excerpt",
                        true,
                        "{\"a\":1,\"b\":2,\"c\":\"test\"}"),
                Arguments.of("valid excerpt modifier",
                        new HashMap<String, Object>(
                                Map.of("id", "123", "title", "lorem", "rel",
                                        new HashMap<String, Object>(Map.of("id", "456", "title", "<p>rel_title</p>",
                                                "sub", new HashMap<String, Object>(Map.of("id", "789", "title", "sub_title")))
                                        ))),
                        "*:excerpt(2),rel.title:excerpt(3, true)",
                        false,
                        "{\"id\":\"12\",\"rel\":{\"title\":\"rel...\"},\"title\":\"lo\"}"
                )

        );
    }


    private static Map<String, Object> getComplexMap() {
        String json = """
                {
                	"a": 1,
                	"b": 2,
                	"c": "test",
                	"anySlice": [{
                		"A": [1, 2, 3],
                		"B": ["1", "2", 3],
                		"C": "test",
                		"D": {
                			"DA": 1,
                			"DB": 2
                		}
                	}, {
                		"A": "test"
                	}],
                	"mapSlice": [{
                		"A": [1, 2, 3],
                		"B": ["1", "2", 3],
                		"C": "test",
                		"D": [{
                			"DA": 1
                		}]
                	}, {
                		"B": ["1", "2", 3],
                		"D": [{
                			"DA": 2
                		}, {
                			"DA": 3
                		}, {
                			"DB": 4
                		}]
                	}],
                	"fullMap": [{
                		"A": [1, 2, 3],
                		"B": ["1", "2", 3],
                		"C": "test"
                	}, {
                		"B": ["1", "2", 3],
                		"D": [{
                			"DA": 2
                		}, {
                			"DA": 3
                		}]
                	}]
                }
                """;

        return JSONUtil.parseObj(json);

    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("testPickFieldsData")
    public void testPickFields(String name, Object data, String fields, boolean expectError, String result) {

        boolean isErr = false;
        try {

            FieldsFilterProcessor.pickFields(data, fields);
            String jsonStr = JSONUtil.toJsonStr(data);
            System.out.println("解析后的值 : " + jsonStr);
            JSONAssert.assertEquals(result, jsonStr, true);

        } catch (Exception e) {
            isErr = true;
            e.printStackTrace();
        }
        if (isErr != expectError) {
            throw new RuntimeException("error");
        }


    }
}
