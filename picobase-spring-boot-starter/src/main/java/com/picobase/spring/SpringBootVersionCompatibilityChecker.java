package com.picobase.spring;

import com.picobase.exception.PbException;
import com.picobase.util.CommonHelper;
import org.springframework.boot.SpringBootVersion;

/**
 * SpringBoot 版本与 Picobase 版本兼容检查器
 */
public class SpringBootVersionCompatibilityChecker {

    public SpringBootVersionCompatibilityChecker() {
        String version = SpringBootVersion.getVersion();
        if (CommonHelper.isEmpty(version) || version.startsWith("1.") || version.startsWith("2.")) {
            return;
        }
        String str = "当前 SpringBoot 版本（" + version + "）与 Picobase 依赖不兼容";
        System.err.println(str);
        throw new PbException(str);
    }
}
