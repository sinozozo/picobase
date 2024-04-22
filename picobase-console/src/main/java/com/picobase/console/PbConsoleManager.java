package com.picobase.console;

import com.picobase.console.config.PbConsoleConfig;

/**
 * PbConsoleManager，持有 PbConsoleConfig 配置对象全局引用
 */
public class PbConsoleManager {

    /**
     * 配置文件 Bean
     */
    private static volatile PbConsoleConfig config;

    public static void setConfig(PbConsoleConfig config) {
        PbConsoleManager.config = config;

    }

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

}
