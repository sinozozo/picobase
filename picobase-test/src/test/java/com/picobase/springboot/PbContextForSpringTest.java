package com.picobase.springboot;

import com.picobase.PbManager;
import com.picobase.console.PbConsoleManager;
import com.picobase.spring.SpringMVCUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoConfigureMockMvc
@SpringBootTest
public class PbContextForSpringTest {


    @Autowired
    private MockMvc mockMvc;



    @BeforeEach
    public void setUp(){
        PbConsoleManager.getConfig().setAuth(false);
    }


    @Test
    void testContextBind() throws Exception {
        // 不进行鉴权
        PbConsoleManager.getConfig().setAuth(false);


        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/testRequestBind?name=zou&age=18&name=qia&int2=1&int2=2" +
                        "&int3=1&int3=2&int3=&int4=1&int4=").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        MvcResult mvcResult = resultActions.andReturn();
        System.out.println(mvcResult.getResponse().getContentAsString());

        // form 形式提交请求
         mockMvc.perform(MockMvcRequestBuilders
                        .post("/testRequestBind")  // POST请求
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)  // 设置请求内容类型为表单形式
                        .param("name", "zou")  // 添加表单参数
                        .param("age", "18")
                        .param("name", "qia")
                        .param("int2", "1")
                        .param("int2", "2")
                        .param("int3", "1")
                        .param("int3", "2")
                        .param("int3", "")
                        .param("int4", "1")
                        .param("int4", "")
                )
                .andExpect(MockMvcResultMatchers.status().isOk());  // 断言期望的状态码

        // json 形式提交
        // Create a JSON object to send as the request body
        JSONObject requestBody = new JSONObject();

        requestBody.put("age", 18);
        requestBody.put("int4", new JSONArray(Arrays.asList(1, null)));
// Convert the JSON object to a string
        String jsonRequest = requestBody.toString();

// Perform the request with the JSON body
        mockMvc.perform(MockMvcRequestBuilders
                .post("/testRequestBind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());



    }

}

