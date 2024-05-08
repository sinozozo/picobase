package com.picobase.console;

import com.picobase.StartUpApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = {StartUpApplication.class, EventHandlerForAdminControllerTest.class, EventHandlerForAdminControllerTest2.class})
public class AdminControllerTest {

    public static int AdminAuthWithPasswordEvent_BEFORE = 0;
    public static int AdminAuthWithPasswordEvent_AFTER = 0;
    public static int AdminAuthRequestEvent = 0;
    public static boolean afterError = false;
    @Autowired
    private MockMvc mockMvc;

    static Stream<Arguments> testAdminAuthWithPasswordScenarios() {
        return Stream.of(
                Arguments.of("空数据", "{}", 400, "\"validation_required\",\"message\":\"cannot be blank\"", 0, 0, 0, false),
                Arguments.of("无效数据", "{", 400, "\"code\":400,\"message\":\"com.fasterxml", 0, 0, 0, false),
                Arguments.of("错误数据", "{\"identity\":\"missing@example.com\",\"password\":\"1234567890\"}", 400, "\"data\":{}", 1, 0, 0, false),
                Arguments.of("首次正确登录", "{\"identity\":\"zouqiang@test.com\",\"password\":\"zouqiang666\"}", 200, "{\"token\":\"", 1, 1, 1, false),
                Arguments.of("登录后继续登录valid email/password(already authorized)", "{\"identity\":\"zouqiang@test.com\",\"password\":\"zouqiang666\"}", 200, "{\"token\":\"", 1, 1, 1, false),
                Arguments.of("后置事件处理异常时，无法继续执行", "{\"identity\":\"zouqiang@test.com\",\"password\":\"zouqiang666\"}", 400, "\"code\":400,\"message\":", 1, 2, 0, true)


        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("testAdminAuthWithPasswordScenarios")
    public void testAdminAuthWithPassword(String name, String body, int status, String expectedResponse, int before, int after, int auth, boolean beforeAuthError) throws Exception {
        cleanParams();
        afterError = beforeAuthError;
        /**
         * 固定用户名和密码
         */
        PbConsoleManager.getConfig().setIdentity("zouqiang@test.com").setPassword("zouqiang666");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admins/auth-with-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(status))
                .andExpect(content().string(containsString(expectedResponse)));
        Assertions.assertTrue(AdminAuthWithPasswordEvent_BEFORE == before);
        Assertions.assertTrue(AdminAuthWithPasswordEvent_AFTER == after);
        Assertions.assertTrue(AdminAuthRequestEvent == auth);

    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("testAdminAuthWithPasswordScenarios")
    public void testAdminAuthWithPasswordForDB(String name, String body, int status, String expectedResponse, int before, int after, int auth, boolean beforeAuthError) throws Exception {
        cleanParams();
        afterError = beforeAuthError;
        PbConsoleManager.getConfig().setIdentity(null).setPassword(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admins/auth-with-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(status))
                .andExpect(content().string(containsString(expectedResponse)));
        Assertions.assertTrue(AdminAuthWithPasswordEvent_BEFORE == before);
        Assertions.assertTrue(AdminAuthWithPasswordEvent_AFTER == after);
        Assertions.assertTrue(AdminAuthRequestEvent == auth);

    }


    public void cleanParams() {
        this.AdminAuthRequestEvent = 0;
        this.AdminAuthWithPasswordEvent_AFTER = 0;
        this.AdminAuthWithPasswordEvent_BEFORE = 0;
    }
}