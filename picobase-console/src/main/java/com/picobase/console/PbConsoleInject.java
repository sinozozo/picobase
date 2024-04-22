package com.picobase.console;

import com.picobase.console.config.PbConsoleConfig;
import com.picobase.console.web.AdminController;
import com.picobase.console.web.ConsoleController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AdminController.class, ConsoleController.class, PbConsoleRegister.class})
public class PbConsoleInject {
    @Autowired(required = false)
    public void setPbAdminConfig(PbConsoleConfig config) {
        PbConsoleManager.setConfig(config);
    }
}
