package com.picobase.console.web;

import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.console.PbConsoleManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/admins")
public class AdminController {

    @PostMapping(value = "/auth-with-password")
    public void authWithPassword() {

        // 不需要校验 ， 直接给前端设置一个登录态
        if (!PbConsoleManager.getConfig().getAuth()) {
            // 执行登录
            PbManager.getPbLogic("pbAdmin").login("pbAdmin");
        }
    }


}
