package com.picobase.application;


import com.picobase.util.CommonHelper;

/**
 * 应用全局信息
 */
public class ApplicationInfo {

    /**
     * 应用前缀
     */
    public static String routePrefix;

    /**
     * 为指定 path 裁剪掉 routePrefix 前缀
     *
     * @param path 指定 path
     * @return /
     */
    public static String cutPathPrefix(String path) {
        if (!CommonHelper.isEmpty(routePrefix) && !routePrefix.equals("/") && path.startsWith(routePrefix)) {
            path = path.substring(routePrefix.length());
        }
        return path;
    }

}