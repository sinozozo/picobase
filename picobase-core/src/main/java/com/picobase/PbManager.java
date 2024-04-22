package com.picobase;

import com.picobase.cache.PbCache;
import com.picobase.cache.PbCacheDefaultImpl;
import com.picobase.config.PbConfig;
import com.picobase.config.PbConfigFactory;
import com.picobase.context.PbContext;
import com.picobase.error.PbErrorCode;
import com.picobase.exception.PbException;
import com.picobase.json.PbJsonTemplate;
import com.picobase.json.PbJsonTemplateDefaultImpl;
import com.picobase.listener.PbEventCenter;
import com.picobase.log.PbLog;
import com.picobase.log.PbLogForConsole;
import com.picobase.logic.PbLogic;
import com.picobase.logic.PbPermissionInterface;
import com.picobase.logic.PermissionInterfaceDefaultImpl;
import com.picobase.strategy.PbStrategy;
import com.picobase.util.PbInnerUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理 PicoBase 所有全局组件，可通过此类快速获取、写入各种全局组件对象
 */
public class PbManager {

    /**
     * 全局配置对象
     */
    public volatile static PbConfig config;


    public static void setConfig(PbConfig config) {

        setConfigMethod(config);

        // 打印 banner
        if (config != null && config.getIsPrint()) {
            PbInnerUtil.printBanner();
        }

        // 如果此 config 对象没有配置 isColorLog 的值，则框架为它自动判断一下
        if (config != null && config.getIsLog() != null && config.getIsLog() && config.getIsColorLog() == null) {
            config.setIsColorLog(PbInnerUtil.isCanColorLog());
        }

        // $$ 全局事件
        PbEventCenter.doSetConfig(config);
    }

    private static void setConfigMethod(PbConfig config) {
        PbManager.config = config;
    }

    /**
     * @return Pb全局配置信息
     */
    public static PbConfig getConfig() {
        if (config == null) {
            synchronized (PbManager.class) {
                if (config == null) {
                    setConfigMethod(PbConfigFactory.createConfig());
                }
            }
        }
        return config;
    }

    // 日志

    /**
     * 日志输出器
     */
    public volatile static PbLog log = new PbLogForConsole();

    public static void setLog(PbLog log) {
        PbManager.log = log;
        PbEventCenter.doRegisterComponent("PbLog", log);
    }

    public static PbLog getLog() {
        return PbManager.log;
    }


    // json

    /**
     * JSON 转换器
     */
    private volatile static PbJsonTemplate pbJsonTemplate;

    public static void setSaJsonTemplate(PbJsonTemplate pbJsonTemplate) {
        PbManager.pbJsonTemplate = pbJsonTemplate;
        PbEventCenter.doRegisterComponent("PbJsonTemplate", pbJsonTemplate);
    }

    public static PbJsonTemplate getPbJsonTemplate() {
        if (pbJsonTemplate == null) {
            synchronized (PbManager.class) {
                if (pbJsonTemplate == null) {
                    PbManager.pbJsonTemplate = new PbJsonTemplateDefaultImpl();
                }
            }
        }
        return pbJsonTemplate;
    }


    /**
     * 一级上下文 SaTokenContextContext
     */
    private volatile static PbContext pbContext;

    public static void setPbContext(PbContext pbContext) {
        PbManager.pbContext = pbContext;
        PbEventCenter.doRegisterComponent("PbContext", pbContext);
    }

    public static PbContext getPbContext() {
        return pbContext;
    }


    /**
     * 缓存组件
     */
    private volatile static PbCache pbCache;

    public static void setPbCache(PbCache pbCache) {
        setPbCacheMethod(pbCache);
        PbEventCenter.doRegisterComponent("PbCache", pbCache);
    }

    private static void setPbCacheMethod(PbCache pbCache) {
        if (PbManager.pbCache != null) {
            PbManager.pbCache.destroy();
        }
        PbManager.pbCache = pbCache;
        if (PbManager.pbCache != null) {
            PbManager.pbCache.init();
        }
    }

    public static PbCache getPbCache() {
        if (pbCache == null) {
            synchronized (PbManager.class) {
                if (pbCache == null) {
                    setPbCacheMethod(new PbCacheDefaultImpl());
                }
            }
        }
        return pbCache;
    }

    /**
     * PbLogic 集合, 记录框架所有成功初始化的 PbLogic
     */
    public static Map<String, PbLogic> pbLogicMap = new LinkedHashMap<>();

    /**
     * 向全局集合中 put 一个 PbLogic
     *
     * @param pbLogic PbLogic
     */
    public static void putPbLogic(PbLogic pbLogic) {
        pbLogicMap.put(pbLogic.getLoginType(), pbLogic);
    }

    /**
     * 在全局集合中 移除 一个 PbLogic
     */
    public static void removePbLogic(String loginType) {
        pbLogicMap.remove(loginType);
    }


    /**
     * 根据 LoginType 获取对应的 PbLogic，如果不存在则新建并返回
     *
     * @param loginType 对应的账号类型
     * @return 对应的 PbLogic
     */
    public static PbLogic getPbLogic(String loginType) {
        return getPbLogic(loginType, true);
    }

    /**
     * 根据 LoginType 获取对应的 PbLogic，如果不存在，isCreate = 是否自动创建并返回
     *
     * @param loginType 对应的账号类型
     * @param isCreate  在 PbLogic 不存在时，true=新建并返回，false=抛出异常
     * @return 对应的 PbLogic
     */
    private static PbLogic getPbLogic(String loginType, boolean isCreate) {
        // 如果type为空则返回框架默认内置的
        if (loginType == null || loginType.isEmpty()) {
            return PbUtil.pbLogic;
        }
        // 从集合中获取
        PbLogic pbLogic = pbLogicMap.get(loginType);
        if (pbLogic == null) {

            // isCreate=true时，自创建模式：自动创建并返回
            if (isCreate) {
                synchronized (PbManager.class) {
                    pbLogic = pbLogicMap.get(loginType);
                    if (pbLogic == null) {
                        pbLogic = PbStrategy.instance.createPbLogic.apply(loginType);
                    }
                }
            }
            // isCreate=false时，严格校验模式：抛出异常
            else {
                /*
                 * 此时有两种情况会造成 PbLogic == null
                 * 1. loginType拼写错误，请改正 （建议使用常量）
                 * 2. 自定义 PbLoginUtil 尚未初始化（静态类中的属性至少一次调用后才会初始化），解决方法两种
                 * 		(1) 从main方法里调用一次
                 * 		(2) 在自定义 PbLoginUtil 类加上类似 @Component 的注解让容器启动时扫描到自动初始化
                 */
                throw new PbException("未能获取对应StpLogic，type=" + loginType).setCode(PbErrorCode.CODE_10002);
            }
        }

        // 返回
        return pbLogic;
    }

    /**
     * 权限数据源组件
     */
    private volatile static PbPermissionInterface permissionInterface;

    public static void setStpInterface(PbPermissionInterface permissionInterface) {
        PbManager.permissionInterface = permissionInterface;
        PbEventCenter.doRegisterComponent("PbPermissionInterface", permissionInterface);
    }

    public static PbPermissionInterface getPermissionInterface() {
        if (permissionInterface == null) {
            synchronized (PbManager.class) {
                if (permissionInterface == null) {
                    PbManager.permissionInterface = new PermissionInterfaceDefaultImpl();
                }
            }
        }
        return permissionInterface;
    }

}
