
package com.picobase.persistence.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storage context holder.
 *
 */
public class StorageContextHolder {
    
    private static final ThreadLocal<ArrayList<ModifyRequest>> SQL_CONTEXT = ThreadLocal.withInitial(ArrayList::new);
    
    private static final ThreadLocal<Map<String, String>> EXTEND_INFO_CONTEXT = ThreadLocal.withInitial(HashMap::new);
    
    /**
     * Add sql context.
     *
     * @param sql  sql
     * @param args argument list
     */
    public static void addSqlContext(String sql, Object... args) {
        ArrayList<ModifyRequest> requests = SQL_CONTEXT.get();
        ModifyRequest context = new ModifyRequest();
        context.setExecuteNo(requests.size());
        context.setSql(sql);
        context.setArgs(args);
        requests.add(context);
        SQL_CONTEXT.set(requests);
    }


    public static void addSqlContext(String sql, Map<String,Object> nameArgs) {
        ArrayList<ModifyRequest> requests = SQL_CONTEXT.get();
        ModifyRequest context = new ModifyRequest();
        context.setExecuteNo(requests.size());
        context.setSql(sql);
        context.setNamedArgs(nameArgs);
        requests.add(context);
        SQL_CONTEXT.set(requests);
    }
    
    /**
     * Add sql context.
     *
     * @param rollbackOnUpdateFail  roll back when update fail
     * @param sql  sql
     * @param args argument list
     */
    public static void addSqlContext(boolean rollbackOnUpdateFail, String sql, Object... args) {
        ArrayList<ModifyRequest> requests = SQL_CONTEXT.get();
        ModifyRequest context = new ModifyRequest();
        context.setExecuteNo(requests.size());
        context.setSql(sql);
        context.setArgs(args);
        context.setRollBackOnUpdateFail(rollbackOnUpdateFail);
        requests.add(context);
        SQL_CONTEXT.set(requests);
    }

    public static void addSqlContext(boolean rollbackOnUpdateFail, String sql, Map<String,Object> nameArgs) {
        ArrayList<ModifyRequest> requests = SQL_CONTEXT.get();
        ModifyRequest context = new ModifyRequest();
        context.setExecuteNo(requests.size());
        context.setSql(sql);
        context.setNamedArgs(nameArgs);
        context.setRollBackOnUpdateFail(rollbackOnUpdateFail);
        requests.add(context);
        SQL_CONTEXT.set(requests);
    }
    
    /**
     * Put extend info.
     *
     * @param key   key
     * @param value value
     */
    public static void putExtendInfo(String key, String value) {
        Map<String, String> old = EXTEND_INFO_CONTEXT.get();
        old.put(key, value);
        EXTEND_INFO_CONTEXT.set(old);
    }
    
    /**
     * Put all extend info.
     *
     * @param map all extend info
     */
    public static void putAllExtendInfo(Map<String, String> map) {
        Map<String, String> old = EXTEND_INFO_CONTEXT.get();
        old.putAll(map);
        EXTEND_INFO_CONTEXT.set(old);
    }
    
    /**
     * Determine if key is included.
     *
     * @param key key
     * @return {@code true} if contains key
     */
    public static boolean containsExtendInfo(String key) {
        Map<String, String> extendInfo = EXTEND_INFO_CONTEXT.get();
        boolean exist = extendInfo.containsKey(key);
        EXTEND_INFO_CONTEXT.set(extendInfo);
        return exist;
    }
    
    public static List<ModifyRequest> getCurrentSqlContext() {
        return SQL_CONTEXT.get();
    }
    
    public static Map<String, String> getCurrentExtendInfo() {
        return EXTEND_INFO_CONTEXT.get();
    }
    
    public static void cleanAllContext() {
        SQL_CONTEXT.remove();
        EXTEND_INFO_CONTEXT.remove();
    }
}
