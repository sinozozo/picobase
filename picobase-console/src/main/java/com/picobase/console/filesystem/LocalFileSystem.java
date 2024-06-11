package com.picobase.console.filesystem;

import cn.hutool.core.io.FileUtil;
import com.picobase.console.config.PbConsoleConfig;
import com.picobase.file.PbFile;
import com.picobase.file.PbFileSystem;
import com.picobase.json.PbJsonTemplate;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author liuziqi
 * @description LocalFileSystem
 * @date 2024/3/4 15:26
 **/
public class LocalFileSystem extends AbstractFileSystem {


    private static final String ATTRS_SUFFIX = ".attrs";

    private final Object lock = new Object();
    private final PbJsonTemplate jsonTemplate;
    private PbConsoleConfig config;

    public LocalFileSystem(PbConsoleConfig config, PbJsonTemplate jsonTemplate) {
        this.config = config;
        this.jsonTemplate = jsonTemplate;
    }


    @Override
    public PbFileSystem init() throws Exception {
        synchronized (lock) {
            Path dataPath = Paths.get(config.getDataDirPath());
            if (Files.notExists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        }
        return this;
    }

    @Override
    public boolean exists(String fileKey) {
        return Files.exists(Paths.get(config.getDataDirPath(), fileKey));
    }

    @Override
    public void uploadFile(PbFile file, String fileKey) throws Exception {
        Path filePath = this.getFullFilePath(fileKey);

        try (InputStream contentStream = file.getContent()) {
            Files.createDirectories(filePath.getParent());
            Files.copy(contentStream, filePath);
            Path attrsFilePath = this.getFullAttrsFilePath(fileKey);
            Files.writeString(attrsFilePath, this.toAttrs(file));
        }
    }

    @Override
    public void delete(String fileKey) throws Exception {
        Path filePath = this.getFullFilePath(fileKey);
        Files.deleteIfExists(filePath);

        Path attrsFilePath = this.getFullAttrsFilePath(fileKey);
        Files.deleteIfExists(attrsFilePath);
    }

    @Override
    public void deletePrefix(String prefix) throws Exception {
        if (prefix == null || prefix.isEmpty()) {
            throw new RuntimeException("Prefix mustn't be empty.");
        }

        FileUtil.del(this.getFullFilePath(prefix));
    }

    @Override
    public PbFile getFile(String fileKey) throws Exception {
        Path filePath = this.getFullFilePath(fileKey);
        Path attrsFilePath = this.getFullAttrsFilePath(fileKey);

        PbFile pbFile = this.toPbFile(Files.readString(attrsFilePath));
        pbFile.setContent(Files.newInputStream(filePath));
        return pbFile;
    }

    private Path getFullFilePath(String fileKey) {
        return Paths.get(config.getDataDirPath(), fileKey);
    }

    private Path getFullAttrsFilePath(String fileKey) {
        return Paths.get(config.getDataDirPath(), getAttrsFileKey(fileKey));
    }

    private String getAttrsFileKey(String fileKey) {
        return fileKey + ATTRS_SUFFIX;
    }

    private String toAttrs(PbFile pbFile) {
        return jsonTemplate.toJsonString(pbFile);
    }

    private PbFile toPbFile(String attrs) {
        return jsonTemplate.parseJsonToObject(attrs, PbFile.class);
    }


}
