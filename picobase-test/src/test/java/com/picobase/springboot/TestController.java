package com.picobase.springboot;

import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.log.PbLog;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {
    PbLog log = PbManager.getLog();

    @RequestMapping("/testRequestBind")
    public BindRequestDto testContextBind() {
        log.info("测试 PbContext.bind");
        BindRequestDto bind = PbUtil.bindRequest(BindRequestDto.class).get();
        System.out.println(bind);
        return bind;
    }
}
