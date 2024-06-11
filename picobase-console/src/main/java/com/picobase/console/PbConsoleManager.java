package com.picobase.console;

import com.picobase.console.config.PbConsoleConfig;
import com.picobase.listener.PbEventCenter;

/**
 * PbConsoleManager，持有 PbConsoleConfig 配置对象全局引用
 */
public class PbConsoleManager {

    /**
     * 配置文件 Bean
     */
    private static volatile PbConsoleConfig config;

    public static PbConsoleConfig getConfig() {
        if (config == null) {
            synchronized (PbConsoleManager.class) {
                if (config == null) {
                    setConfig(new PbConsoleConfig());
                }
            }
        }
        return config;
    }

    public static void setConfig(PbConsoleConfig config) {
        PbConsoleManager.config = config;
        PbEventCenter.doRegisterComponent("[PbConsoleManager]PbConsoleConfig", config); //TODO  这里不会打印日志 ， config注入了 但是并没有绑定配置文件中的参数, 考虑如何解决
    }


}
