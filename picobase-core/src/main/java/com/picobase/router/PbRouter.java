package com.picobase.router;


import com.picobase.PbManager;
import com.picobase.context.PbHolder;
import com.picobase.exception.BackResultException;
import com.picobase.exception.StopMatchException;
import com.picobase.fun.PbFunction;
import com.picobase.fun.PbParamFunction;
import com.picobase.fun.PbParamRetFunction;

import java.util.List;

/**
 * 路由匹配操作工具类
 *
 * <p> 提供了一系列的路由匹配操作方法，一般用在全局拦截器、过滤器做路由拦截鉴权。 </p>
 * <p> 简单示例： </p>
 * <pre>
 *    	// 指定一条 match 规则
 *    	PbRouter
 *    	   	.match("/**")    // 拦截的 path 列表，可以写多个
 *   	   	.notMatch("/user/doLogin")        // 排除掉的 path 列表，可以写多个
 *   	   	.check(r->StpUtil.checkLogin());        // 要执行的校验动作，可以写完整的 lambda 表达式
 * </pre>
 */
public class PbRouter {

    private PbRouter() {
    }

    // -------------------- 路由匹配相关 --------------------

    /**
     * 路由匹配
     *
     * @param pattern 路由匹配符
     * @param path    被匹配的路由
     * @return 是否匹配成功
     */
    public static boolean isMatch(String pattern, String path) {
        return PbManager.getPbContext().matchPath(pattern, path);
    }

    /**
     * 路由匹配
     *
     * @param patterns 路由匹配符集合
     * @param path     被匹配的路由
     * @return 是否匹配成功
     */
    public static boolean isMatch(List<String> patterns, String path) {
        if (patterns == null) {
            return false;
        }
        for (String pattern : patterns) {
            if (isMatch(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 路由匹配
     *
     * @param patterns 路由匹配符数组
     * @param path     被匹配的路由
     * @return 是否匹配成功
     */
    public static boolean isMatch(String[] patterns, String path) {
        if (patterns == null) {
            return false;
        }
        for (String pattern : patterns) {
            if (isMatch(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Http请求方法匹配
     *
     * @param methods      Http请求方法断言数组
     * @param methodString Http请求方法
     * @return 是否匹配成功
     */
    public static boolean isMatch(PbHttpMethod[] methods, String methodString) {
        if (methods == null) {
            return false;
        }
        for (PbHttpMethod method : methods) {
            if (method == PbHttpMethod.ALL || (method != null && method.toString().equalsIgnoreCase(methodString))) {
                return true;
            }
        }
        return false;
    }

    // ------ 使用当前URI匹配

    /**
     * 路由匹配 (使用当前URI)
     *
     * @param pattern 路由匹配符
     * @return 是否匹配成功
     */
    public static boolean isMatchCurrURI(String pattern) {
        return isMatch(pattern, PbHolder.getRequest().getRequestPath());
    }

    /**
     * 路由匹配 (使用当前URI)
     *
     * @param patterns 路由匹配符集合
     * @return 是否匹配成功
     */
    public static boolean isMatchCurrURI(List<String> patterns) {
        return isMatch(patterns, PbHolder.getRequest().getRequestPath());
    }

    /**
     * 路由匹配 (使用当前URI)
     *
     * @param patterns 路由匹配符数组
     * @return 是否匹配成功
     */
    public static boolean isMatchCurrURI(String[] patterns) {
        return isMatch(patterns, PbHolder.getRequest().getRequestPath());
    }

    /**
     * Http请求方法匹配 (使用当前请求方式)
     *
     * @param methods Http请求方法断言数组
     * @return 是否匹配成功
     */
    public static boolean isMatchCurrMethod(PbHttpMethod[] methods) {
        return isMatch(methods, PbHolder.getRequest().getMethod());
    }


    // -------------------- 开始匹配 --------------------

    /**
     * 初始化一个PbRouterStaff，开始匹配
     *
     * @return PbRouterStaff
     */
    public static PbRouterStaff newMatch() {
        return new PbRouterStaff();
    }

    // ----------------- path匹配

    /**
     * 路由匹配
     *
     * @param patterns 路由匹配符集合
     * @return PbRouterStaff
     */
    public static PbRouterStaff match(String... patterns) {
        return new PbRouterStaff().match(patterns);
    }

    /**
     * 路由匹配排除
     *
     * @param patterns 路由匹配符排除数组
     * @return PbRouterStaff
     */
    public static PbRouterStaff notMatch(String... patterns) {
        return new PbRouterStaff().notMatch(patterns);
    }

    /**
     * 路由匹配
     *
     * @param patterns 路由匹配符集合
     * @return 对象自身
     */
    public static PbRouterStaff match(List<String> patterns) {
        return new PbRouterStaff().match(patterns);
    }

    /**
     * 路由匹配排除
     *
     * @param patterns 路由匹配符排除集合
     * @return 对象自身
     */
    public static PbRouterStaff notMatch(List<String> patterns) {
        return new PbRouterStaff().notMatch(patterns);
    }

    // ----------------- Method匹配

    /**
     * Http请求方式匹配 (Enum)
     *
     * @param methods Http请求方法断言数组
     * @return PbRouterStaff
     */
    public static PbRouterStaff match(PbHttpMethod... methods) {
        return new PbRouterStaff().match(methods);
    }

    /**
     * Http请求方法匹配排除 (Enum)
     *
     * @param methods Http请求方法断言排除数组
     * @return PbRouterStaff
     */
    public static PbRouterStaff notMatch(PbHttpMethod... methods) {
        return new PbRouterStaff().notMatch(methods);
    }

    /**
     * Http请求方法匹配 (String)
     *
     * @param methods Http请求方法断言数组
     * @return PbRouterStaff
     */
    public static PbRouterStaff matchMethod(String... methods) {
        return new PbRouterStaff().matchMethod(methods);
    }

    /**
     * Http请求方法匹配排除 (String)
     *
     * @param methods Http请求方法断言排除数组
     * @return PbRouterStaff
     */
    public static PbRouterStaff notMatchMethod(String... methods) {
        return new PbRouterStaff().notMatchMethod(methods);
    }

    // ----------------- 条件匹配

    /**
     * 根据 boolean 值进行匹配
     *
     * @param flag boolean值
     * @return PbRouterStaff
     */
    public static PbRouterStaff match(boolean flag) {
        return new PbRouterStaff().match(flag);
    }

    /**
     * 根据 boolean 值进行匹配排除
     *
     * @param flag boolean值
     * @return PbRouterStaff
     */
    public static PbRouterStaff notMatch(boolean flag) {
        return new PbRouterStaff().notMatch(flag);
    }

    /**
     * 根据自定义方法进行匹配 (lazy)
     *
     * @param fun 自定义方法
     * @return PbRouterStaff
     */
    public static PbRouterStaff match(PbParamRetFunction<Object, Boolean> fun) {
        return new PbRouterStaff().match(fun);
    }

    /**
     * 根据自定义方法进行匹配排除 (lazy)
     *
     * @param fun 自定义排除方法
     * @return PbRouterStaff
     */
    public static PbRouterStaff notMatch(PbParamRetFunction<Object, Boolean> fun) {
        return new PbRouterStaff().notMatch(fun);
    }


    // -------------------- 直接指定check函数 --------------------

    /**
     * 路由匹配，如果匹配成功则执行认证函数
     *
     * @param pattern 路由匹配符
     * @param fun     要执行的校验方法
     * @return /
     */
    public static PbRouterStaff match(String pattern, PbFunction fun) {
        return new PbRouterStaff().match(pattern, fun);
    }

    /**
     * 路由匹配，如果匹配成功则执行认证函数
     *
     * @param pattern 路由匹配符
     * @param fun     要执行的校验方法
     * @return /
     */
    public static PbRouterStaff match(String pattern, PbParamFunction<PbRouterStaff> fun) {
        return new PbRouterStaff().match(pattern, fun);
    }

    /**
     * 路由匹配 (并指定排除匹配符)，如果匹配成功则执行认证函数
     *
     * @param pattern        路由匹配符
     * @param excludePattern 要排除的路由匹配符
     * @param fun            要执行的方法
     * @return /
     */
    public static PbRouterStaff match(String pattern, String excludePattern, PbFunction fun) {
        return new PbRouterStaff().match(pattern, excludePattern, fun);
    }

    /**
     * 路由匹配 (并指定排除匹配符)，如果匹配成功则执行认证函数
     *
     * @param pattern        路由匹配符
     * @param excludePattern 要排除的路由匹配符
     * @param fun            要执行的方法
     * @return /
     */
    public static PbRouterStaff match(String pattern, String excludePattern, PbParamFunction<PbRouterStaff> fun) {
        return new PbRouterStaff().match(pattern, excludePattern, fun);
    }


    // -------------------- 提前退出 --------------------

    /**
     * 停止匹配，跳出函数 (在多个匹配链中一次性跳出Auth函数)
     *
     * @return PbRouterStaff
     */
    public static PbRouterStaff stop() {
        throw new StopMatchException();
    }

    /**
     * 停止匹配，结束执行，向前端返回结果
     *
     * @return PbRouterStaff
     */
    public static PbRouterStaff back() {
        throw new BackResultException("");
    }

    /**
     * 停止匹配，结束执行，向前端返回结果
     *
     * @param result 要输出的结果
     * @return PbRouterStaff
     */
    public static PbRouterStaff back(Object result) {
        throw new BackResultException(result);
    }

}
