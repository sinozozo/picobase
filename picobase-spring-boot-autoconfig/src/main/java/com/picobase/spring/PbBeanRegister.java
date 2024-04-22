package com.picobase.spring;

import com.picobase.config.PbConfig;
import com.picobase.json.PbJsonTemplate;
import com.picobase.spring.context.path.ApplicationContextPathLoading;
import com.picobase.spring.json.PbJsonTemplateForJackson;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 注册 PicoBase 所需要的Bean
 * <p> Bean 的注册与注入应该分开在两个文件中，否则在某些场景下会造成循环依赖 </p>
 */
public class PbBeanRegister {
    /**
     * 获取配置Bean
     *
     * @return 配置对象
     */
    @Bean
    @ConfigurationProperties(prefix = "picobase")
    public PbConfig getPbConfig() {
        return new PbConfig();
    }

    /**
     * 获取 json 转换器 Bean (Jackson版)
     *
     * @return json 转换器 Bean (Jackson版)
     */
    @Bean
    public PbJsonTemplate getPbJsonTemplateForJackson() {
        return new PbJsonTemplateForJackson();
    }

    /**
     * 应用上下文路径加载器
     *
     * @return /
     */
    @Bean
    public ApplicationContextPathLoading getApplicationContextPathLoading() {
        return new ApplicationContextPathLoading();
    }
}
