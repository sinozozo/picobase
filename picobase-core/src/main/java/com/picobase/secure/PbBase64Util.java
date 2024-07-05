package com.picobase.secure;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 工具类
 */
public class PbBase64Util {

    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();

    /**
     * Base64编码，byte[] 转 String
     *
     * @param bytes byte[]
     * @return 字符串
     */
    public static String encodeBytesToString(byte[] bytes) {
        return encoder.encodeToString(bytes);
    }

    /**
     * Base64解码，String 转 byte[]
     *
     * @param text 字符串
     * @return byte[]
     */
    public static byte[] decodeStringToBytes(String text) {
        return decoder.decode(text);
    }

    /**
     * Base64编码，String 转 String
     *
     * @param text 字符串
     * @return Base64格式字符串
     */
    public static String encode(String text) {
        return encoder.encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64解码，String 转 String
     *
     * @param base64Text Base64格式字符串
     * @return 字符串
     */
    public static String decode(String base64Text) {
        return new String(decoder.decode(base64Text), StandardCharsets.UTF_8);
    }

}
