package com.picobase.validator;

import com.picobase.util.CommonHelper;

import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

import static com.picobase.validator.Validation.newStringRuleWithError;


/**
 * TODO 自行补充
 */
public final class Is {

    private static final String reDomain = "^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-z0-9])?\\.)+(?:[a-zA-Z]{1,63}| xn--[a-z0-9]{1,59})$";
    public final static Pattern DOMAIN = Pattern.compile(reDomain, Pattern.CASE_INSENSITIVE);

    private static final int maxURLRuneCount = 2083;
    private static final int minURLRuneCount = 3;
    static final String IP = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";
    static final String URLSchema = "((ftp|tcp|udp|wss?|https?):\\/\\/)";
    static final String URLUsername = "(\\S+(:\\S*)?@)";
    static final String URLPath = "((\\/|\\?|#)[^\\s]*)";
    static final String URLPort = "(:(\\d{1,5}))";
    static final String URLIP = "([1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3]|24\\d|25[0-5])(\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])){2}(?:\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5]))";
    static final String URLSubdomain = "((www\\.)|([a-zA-Z0-9]+([-_\\.]?[a-zA-Z0-9])*[a-zA-Z0-9]\\.[a-zA-Z0-9]+))";
    static final String URL_REP = "^" + URLSchema + "?" + URLUsername + "?" + "(((" + URLIP + "|(\\[" + IP + "\\])|(([a-zA-Z0-9]([a-zA-Z0-9-_]+)?[a-zA-Z0-9]([-\\.]?[a-zA-Z0-9]+)*)|(" + URLSubdomain + ")?))?(([a-zA-Z\\x{00a1}-\\x{ffff}0-9]+-?-?)*[a-zA-Z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.([a-zA-Z\\x{00a1}-\\x{ffff}]{1,}))?))\\.?" + URLPort + "?" + URLPath + "?$";
    public final static Pattern rxURL = Pattern.compile(URL_REP, Pattern.CASE_INSENSITIVE);

    private static final Err ErrEmail = Err.newError("validation_is_email", "must be a valid email address");
    // ErrURL is the error that returns in case of an invalid URL.
    private static final Err ErrURL = Err.newError("validation_is_url", "must be a valid URL");
    // ErrDomain is the error that returns in case of an invalid domain.
    private static final Err ErrDomain = Err.newError("validation_is_domain", "must be a valid domain");


    public static final StringRule EmailFormat = newStringRuleWithError(Is::isEmail, ErrEmail);

    // URL validates if a string is a valid URL
    public static final StringRule URL = newStringRuleWithError(Is::isURL, ErrURL);


    // Domain validates if a string is valid domain
    public static final StringRule Domain = newStringRuleWithError(Is::isDomain, ErrDomain);

    private static boolean isEmail(String value) {
        return PatternPool.EMAIL.matcher(value).matches();
    }

    private static boolean isDomain(String value) {
        return DOMAIN.matcher(value).matches();
    }

    /**
     * IsURL checks if the string is an URL.
     */
    public static boolean isURL(String str) {
        if (str == null || str.length() >= maxURLRuneCount || str.length() <= minURLRuneCount || str.startsWith(".")) {
            return false;
        }

        String strTemp = str;
        if (str.contains(":") && !str.contains("://")) {
            // support no indicated urlscheme but with colon for port number
            // http:// is appended so URI.create will succeed, strTemp used so it does not impact rxURL.matches
            strTemp = "http://" + str;
        }

        URL u;
        try {
            u = new URI(strTemp).toURL();
        } catch (Exception e) {
            return false;
        }

        if (u.getHost() == null || u.getHost().startsWith(".")) {
            return false;
        }

        if (u.getHost().isEmpty() && (CommonHelper.isNotEmpty(u.getPath()) && !u.getPath().contains("."))) {
            return false;
        }
        return Pattern.matches(rxURL.pattern(), str);
    }

}
