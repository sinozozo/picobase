package com.picobase.spring;

import com.picobase.PbManager;
import com.picobase.config.PbConfig;
import com.picobase.context.PbContext;
import com.picobase.json.PbJsonTemplate;
import com.picobase.listener.PbEventCenter;
import com.picobase.listener.PbListener;
import com.picobase.log.PbLog;
import com.picobase.spring.pathmatch.PbPathMatcherHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.PathMatcher;

import java.util.List;

/**
 * 为 picobase 注入所有组件
 */
public class PbBeanInject {

    /**
     * 组件注入
     * <p> 为确保 Log 组件正常打印，必须将 PbLog 和 PbConfig 率先初始化 </p>
     *
     * @param log      log 对象
     * @param pbConfig 配置对象
     */
    public PbBeanInject(
            @Autowired(required = false) PbLog log,
            @Autowired(required = false) PbConfig pbConfig
    ) {
        if (log != null) {
            PbManager.setLog(log);
        }
        if (pbConfig != null) {
            PbManager.setConfig(pbConfig);
        }
    }

    /**
     * 注入侦听器Bean
     *
     * @param listenerList 侦听器集合
     */
    @Autowired(required = false)
    public void setSaTokenListener(List<PbListener> listenerList) {
        PbEventCenter.registerListenerList(listenerList);
    }

    /**
     * 注入自定义的 JSON 转换器 Bean
     *
     * @param pbJsonTemplate JSON 转换器
     */
    @Autowired(required = false)
    public void setSaJsonTemplate(PbJsonTemplate pbJsonTemplate) {
        PbManager.setSaJsonTemplate(pbJsonTemplate);
    }

    /**
     * 注入上下文Bean
     *
     * @param pbContext PbContext
     */
    @Autowired(required = false)
    public void setSaTokenContext(PbContext pbContext) {
        PbManager.setPbContext(pbContext);
    }

    /**
     * 利用自动注入特性，获取Spring框架内部使用的路由匹配器
     *
     * @param pathMatcher 要设置的 pathMatcher
     */
    @Autowired(required = false)
    @Qualifier("mvcPathMatcher")
    public void setPathMatcher(PathMatcher pathMatcher) {
        PbPathMatcherHolder.setPathMatcher(pathMatcher);
    }
}
