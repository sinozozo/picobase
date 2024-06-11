package com.picobase.file;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author liuziqi
 * @description PbFileSystem
 * @date 2024/3/4 15:18
 **/
public interface PbFileSystem {

    Pattern THUMB_SIZE_REGEX = Pattern.compile("^(\\d+)x(\\d+)(t|b|f)?$");

    String[] INLINE_SERVE_CONTENT_TYPES = new String[]{
            // image
            "image/png", "image/jpg", "image/jpeg", "image/gif", "image/webp", "image/x-icon", "image/bmp",
            // video
            "video/webm", "video/mp4", "video/3gpp", "video/quicktime", "video/x-ms-wmv",
            // audio
            "audio/basic", "audio/aiff", "audio/mpeg", "audio/midi", "audio/mp3", "audio/wave",
            "audio/wav", "audio/x-wav", "audio/x-mpeg", "audio/x-m4a", "audio/aac",
            // document
            "application/pdf", "application/x-pdf",
    };

    Map<String, String> IMAGE_CONTENT_TYPE_FORMAT = Map.of("image/png", "png", "image/jpg", "jpg", "image/jpeg", "jpg", "image/gif", "gif");

    // manualExtensionContentTypes is a map of file extensions to content types.
    Map<String, String> MANUAL_EXTENSION_CONTENT_TYPES = Map.of("svg", "image/svg+xml", "css", "text/css");

    // forceAttachmentParam is the name of the request query parameter to
    // force "Content-Disposition: attachment" header.
    String FORCE_ATTACHMENT_PARAM = "download";

    String THUMB_PREFIX = "thumbs_";


    PbFileSystem init() throws Exception;

    boolean exists(String fileKey);

    void uploadFile(PbFile file, String fileKey) throws Exception;

    void delete(String fileKey) throws Exception;

    void deletePrefix(String prefix) throws Exception;

    PbFile getFile(String fileKey) throws Exception;

    void createThumb(String originalKey, String thumbKey, String thumbSize) throws Exception;

    void serve(String servedPath, String servedName) throws Exception;

}
