package com.picobase.console.config;

public class S3Config {


    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private boolean forcePathStyle = true;

    public String getEndpoint() {
        return endpoint;
    }

    public S3Config setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public S3Config setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public S3Config setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public S3Config setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public S3Config setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public boolean isForcePathStyle() {
        return forcePathStyle;
    }

    public S3Config setForcePathStyle(boolean forcePathStyle) {
        this.forcePathStyle = forcePathStyle;
        return this;
    }

    @Override
    public String toString() {
        return "S3Config{" +
                "endpoint='" + endpoint + '\'' +
                ", region='" + region + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", bucket='" + bucketName + '\'' +
                ", forcePathStyle=" + forcePathStyle +
                '}';
    }
}
