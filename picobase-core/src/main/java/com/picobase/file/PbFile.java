package com.picobase.file;

import cn.hutool.core.io.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.regex.Pattern;

import static cn.hutool.core.util.RandomUtil.randomString;
import static com.picobase.persistence.dbx.DbxUtil.snakeCase;

/**
 * @author liuziqi
 * @description PbFile
 * @date 2024/3/5 13:39
 **/

public class PbFile {

    public static final String ORIGINAL_FILENAME = "original-filename";

    private static final Pattern EXT_INVALID_CHARS_REGEX = Pattern.compile("[^\\w\\.\\*\\-\\+\\=\\#]+");

    private String name;

    private String originalName;

    private long size;

    private String contentType;

    private InputStream content;

    private long lastModifiedTime;


    private PbFile(String name, String originalName, long size, String contentType, InputStream content, long lastModifiedTime) {
        this.name = name;
        this.originalName = originalName;
        this.size = size;
        this.contentType = contentType;
        this.content = content;
        this.lastModifiedTime = lastModifiedTime;
    }

    public PbFile() {
        
    }

    private static String normalizeName(String originalName) {
        String originalFilename = Optional.ofNullable(originalName).orElse("");
        // extension
        String originalExt = FileUtil.extName(originalFilename);
        String cleanExt = EXT_INVALID_CHARS_REGEX.matcher(originalExt).replaceAll("");
        if (cleanExt.isEmpty()) {
            // TODO try to detect the extension from the file content
        }
        cleanExt = originalExt;

        // name
        String cleanName = snakeCase(FileUtil.mainName(originalFilename));
        int length = cleanName.length();
        if (length < 3) {
            // the name is too short so we concatenate an additional random part
            cleanName += randomString(10);
        } else if (length > 100) {
            // keep only the first 100 characters (it is multibyte safe after Snakecase)
            cleanName = cleanName.substring(0, 100);
        }

        return String.format("%s_%s.%s", cleanName, randomString(10), cleanExt);
    }

    public static PbFile newFileFromMultipart(String name, String originalName, long size, String contentType, InputStream content) throws IOException {
        return new PFileBuilder()
                .name(normalizeName(originalName))
                .originalName(originalName)
                .size(size)
                .contentType(contentType)
                .content(content)
                .lastModifiedTime(System.currentTimeMillis()).build();
    }


    public String getName() {
        return name;
    }

    public PbFile setName(String name) {
        this.name = name;
        return this;
    }

    public String getOriginalName() {
        return originalName;
    }

    public PbFile setOriginalName(String originalName) {
        this.originalName = originalName;
        return this;
    }

    public long getSize() {
        return size;
    }

    public PbFile setSize(long size) {
        this.size = size;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public PbFile setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public InputStream getContent() {
        return content;
    }

    public PbFile setContent(InputStream content) {
        this.content = content;
        return this;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public PbFile setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
        return this;
    }


    public static class PFileBuilder {
        private String name;

        private String originalName;

        private long size;

        private String contentType;

        private InputStream content;

        private long lastModifiedTime;


        public PFileBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PFileBuilder originalName(String originalName) {
            this.originalName = originalName;
            return this;
        }

        public PFileBuilder size(long size) {
            this.size = size;
            return this;
        }

        public PFileBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public PFileBuilder content(InputStream inputStream) {
            this.content = inputStream;
            return this;
        }

        public PFileBuilder lastModifiedTime(long lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
            return this;
        }

        public PbFile build() {
            return new PbFile(name, originalName, size, contentType, content, lastModifiedTime);
        }


    }
}

