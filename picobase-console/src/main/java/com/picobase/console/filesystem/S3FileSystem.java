package com.picobase.console.filesystem;

import cn.hutool.core.io.FileUtil;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.picobase.console.config.PbConsoleConfig;
import com.picobase.file.PbFile;
import com.picobase.file.PbFileSystem;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import static com.picobase.file.PbFile.ORIGINAL_FILENAME;


/**
 * @author liuziqi
 * @description S3FileSystem
 * @date 2024/3/4 15:18
 **/
public class S3FileSystem extends AbstractFileSystem {

    private final Object lock = new Object();

    private AmazonS3 s3Client;
    private PbConsoleConfig config;

    public S3FileSystem(PbConsoleConfig config) {
        this.config = config;
    }

    @Override
    public PbFileSystem init() throws Exception {
        synchronized (lock) {
            s3Client = createS3Client();
        }
        return this;
    }

    private AmazonS3 createS3Client() {
        return AmazonS3Client.builder()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(config.getS3Config().getEndpoint(), config.getS3Config().getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(config.getS3Config().getAccessKey(), config.getS3Config().getSecretKey())))
                .withChunkedEncodingDisabled(true)
                .withPathStyleAccessEnabled(config.getS3Config().isForcePathStyle())
                .build();
    }

    @Override
    public boolean exists(String fileKey) {
        try {
            s3Client.getObjectMetadata(config.getS3Config().getBucketName(), convertFileKey(fileKey));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void uploadFile(PbFile file, String fileKey) throws Exception {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setLastModified(new Date(file.getLastModifiedTime()));
        objectMetadata.setUserMetadata(Map.of(ORIGINAL_FILENAME, file.getOriginalName()));

        try (InputStream inputStream = file.getContent()) {
            s3Client.putObject(new PutObjectRequest(
                    config.getS3Config().getBucketName(),
                    convertFileKey(fileKey),
                    inputStream,
                    objectMetadata));
        }
    }

    @Override
    public void delete(String fileKey) throws Exception {
        s3Client.deleteObject(new DeleteObjectRequest(config.getS3Config().getBucketName(), convertFileKey(fileKey)));
    }

    @Override
    public void deletePrefix(String prefix) throws Exception {
        if (prefix == null || prefix.isEmpty()) {
            throw new RuntimeException("Prefix mustn't be empty.");
        }

        ObjectListing prefixObjectList = s3Client.listObjects(config.getS3Config().getBucketName(), convertFileKey(prefix));
        String[] prefixFileKeys = prefixObjectList.getObjectSummaries().stream().map(S3ObjectSummary::getKey).toArray(String[]::new);
        if (prefixFileKeys.length > 0) {
            s3Client.deleteObjects(new DeleteObjectsRequest(config.getS3Config().getBucketName()).withKeys(prefixFileKeys));
        }
    }

    @Override
    public PbFile getFile(String fileKey) throws Exception {
        S3Object object = s3Client.getObject(new GetObjectRequest(config.getS3Config().getBucketName(), convertFileKey(fileKey)));

        return new PbFile.PFileBuilder()
                .name(FileUtil.getName(fileKey))
                .originalName(object.getObjectMetadata().getUserMetaDataOf(ORIGINAL_FILENAME))
                .size(object.getObjectMetadata().getContentLength())
                .contentType(object.getObjectMetadata().getContentType())
                .content(object.getObjectContent())
                .lastModifiedTime(object.getObjectMetadata().getLastModified().getTime()).build();
    }

    private String convertFileKey(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            throw new RuntimeException("fileKey mustn't be empty.");
        }
        return fileKey.replace("\\", "/");
    }
}
