package com.picobase.config;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * S3 存储的相关配置
 */
public class S3Config implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否开启 S3 存储
     */
    private Boolean enable = true;

    /**
     * S3 服务的连接地址
     */
    private String endpoint;

    /**
     * S3 存储的桶
     */
    private String bucket = "picobase";

    /**
     * S3 存储区域
     */
    private String region;

    /*
     * S3 存储的 accessKey
     */
    private String accessKey;

    /*
     * S3 存储的 secretKey
     */
    private String secretKey;

    /**
     * 访问资源时的 url 形式
     * <p>
     * true：如果将 forcePathStyle 设为 true，S3 客户端将使用传统的路径格式访问 S3，即 URL 中会包含 Bucket 名称作为路径的一部分。例如，https://s3.amazonaws.com/my-bucket/example.jpg。
     * <p>
     * false：如果将 forcePathStyle 设为 false ，S3 客户端将使用虚拟主机格式访问 S3，即 URL 中会使用子域名来访问 Bucket。例如，https://my-bucket.s3.amazonaws.com/example.jpg。
     */
    private Boolean forcePathStyle = true;

    /**
     * @return 是否开启 S3 存储
     */
    public Boolean getEnable() {
        return enable;
    }

    /**
     * @param enable 是否开启 S3 存储
     * @return 对象自身
     */
    public S3Config setEnable(Boolean enable) {
        this.enable = enable;
        return this;
    }

    /**
     * @return S3 的连接地址
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint S3 的连接地址 格式: https://s3.amazonaws.com:8090
     * @return 对象自身
     */
    public S3Config setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * @return S3 存储的桶
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * @param bucket s3 存储的桶
     * @return 对象自身
     */
    public S3Config setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    /**
     * @return S3 存储区域
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param region S3 存储区域
     * @return 对象自身
     */
    public S3Config setRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * @return S3 存储的 accessKey
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * @param accessKey S3 存储的 accessKey
     * @return 对象自身
     */
    public S3Config setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    /**
     * @return S3 存储的 secretKey
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey S3 存储的 secretKey
     * @return 对象自身
     */
    public S3Config setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    /**
     * 访问资源时的 url 形式 true：
     * <p>
     * 如果将 forcePathStyle 设置为
     * <p>
     * true：S3 客户端将使用传统的路径格式访问 S3，即 URL 中会包含 Bucket 名称作为路径的一部分。例如，https://s3.amazonaws.com/my-bucket/example.jpg。
     * <p>
     * false： S3 客户端将使用虚拟主机格式访问 S3，即 URL 中会使用子域名来访问 Bucket。例如，https://my-bucket.s3.amazonaws.com/example.jpg。
     * <p>
     *
     * @return 访问资源时的 url 形式
     */
    public Boolean getForcePathStyle() {
        return forcePathStyle;
    }

    /**
     * 访问资源时的 url 形式 true：
     * <p>
     * 如果将 forcePathStyle 设置为
     * <p>
     * true：S3 客户端将使用传统的路径格式访问 S3，即 URL 中会包含 Bucket 名称作为路径的一部分。例如，https://s3.amazonaws.com/my-bucket/example.jpg。
     * <p>
     * false： S3 客户端将使用虚拟主机格式访问 S3，即 URL 中会使用子域名来访问 Bucket。例如，https://my-bucket.s3.amazonaws.com/example.jpg。
     * <p>
     *
     * @param forcePathStyle true or false
     * @return 对象自身
     */
    public S3Config setForcePathStyle(Boolean forcePathStyle) {
        this.forcePathStyle = forcePathStyle;
        return this;
    }

    @Override
    public String toString() {
        return "S3Config{" +
                "enable=" + enable +
                ", endpoint='" + endpoint + '\'' +
                ", bucket='" + bucket + '\'' +
                ", region='" + region + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", forcePathStyle=" + forcePathStyle +
                '}';
    }
}
