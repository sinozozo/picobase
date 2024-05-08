package com.picobase.model;


import java.util.Map;

import static com.picobase.util.PbConstants.FieldValueModifiers;


/**
 * RequestInfo defines a HTTP request data struct, usually used
 * as part of the `@request.*` filter resolver.
 */
public class RequestInfo {

    public static final String REQUEST_INFO_CONTEXT_DEFAULT = "default";
    public static final String REQUEST_INFO_CONTEXT_REALTIME = "realtime";
    public static final String REQUEST_INFO_CONTEXT_PROTECTED_FILE = "protectedFile";
    public static final String REQUEST_INFO_CONTEXT_OAUTH2 = "oauth2";

    private String context;
    private Map<CharSequence, CharSequence> query;
    private Map<String, Object> data;
    private Map<String, Object> headers;
    private RecordModel authRecord;
    private AdminModel admin;
    private String method;


    /**
     * hasModifierDataKeys loosely checks if the current struct has any modifier Data keys.
     */
    public boolean hasModifierDataKeys() {
        String[] allModifiers = FieldValueModifiers();

        for (String key : data.keySet()) {
            for (String m : allModifiers) {
                if (key.endsWith(m)) {
                    return true;
                }
            }
        }
        return false;
    }


    public String getContext() {
        return context;
    }

    public RequestInfo setContext(String context) {
        this.context = context;
        return this;
    }

    public Map<CharSequence, CharSequence> getQuery() {
        return query;
    }

    public RequestInfo setQuery(Map<CharSequence, CharSequence> query) {
        this.query = query;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public RequestInfo setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public RequestInfo setHeaders(Map<String, Object> headers) {
        this.headers = headers;
        return this;
    }

    public RecordModel getAuthRecord() {
        return authRecord;
    }

    public RequestInfo setAuthRecord(RecordModel authRecord) {
        this.authRecord = authRecord;
        return this;
    }

    public AdminModel getAdmin() {
        return admin;
    }

    public RequestInfo setAdmin(AdminModel admin) {
        this.admin = admin;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public RequestInfo setMethod(String method) {
        this.method = method;
        return this;
    }
}