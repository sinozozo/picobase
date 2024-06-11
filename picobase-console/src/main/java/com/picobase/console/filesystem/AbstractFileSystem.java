package com.picobase.console.filesystem;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.picobase.context.PbHolder;
import com.picobase.context.model.PbRequest;
import com.picobase.context.model.PbResponse;
import com.picobase.exception.BadRequestException;
import com.picobase.file.PbFile;
import com.picobase.file.PbFileSystem;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;

import static com.picobase.persistence.resolver.ListUtil.existInArray;

public abstract class AbstractFileSystem implements PbFileSystem {

    // CreateThumb creates a new thumb image for the file at originalKey location.
    // The new thumb file is stored at thumbKey location.
    //
    // thumbSize is in the format:
    // - 0xH  (eg. 0x100)    - resize to H height preserving the aspect ratio
    // - Wx0  (eg. 300x0)    - resize to W width preserving the aspect ratio
    // - WxH  (eg. 300x100)  - resize and crop to WxH viewbox (from center)
    // - WxHt (eg. 300x100t) - resize and crop to WxH viewbox (from top)
    // - WxHb (eg. 300x100b) - resize and crop to WxH viewbox (from bottom)
    // - WxHf (eg. 300x100f) - fit inside a WxH viewbox (without cropping)
    public void createThumb(String originalKey, String thumbKey, String thumbSize) throws Exception {
        Matcher matcher = THUMB_SIZE_REGEX.matcher(thumbSize);
        if (!matcher.find() || matcher.groupCount() != 3) {
            throw new BadRequestException("Thumb size must be in WxH, WxHt, WxHb or WxHf format.");
        }

        int width = Integer.parseInt(matcher.group(1));
        int height = Integer.parseInt(matcher.group(2));
        String resizeType = matcher.group(3) != null ? matcher.group(3) : "";

        if (width == 0 && height == 0) {
            throw new BadRequestException("Thumb width and height cannot be zero at the same time.");
        }

        PbFile file = this.getFile(originalKey);
        BufferedImage originalImage = ImageIO.read(file.getContent());

        BufferedImage thumbImg;
        if (width == 0 || height == 0) {
            // force resize preserving aspect ratio
            thumbImg = Thumbnails.of(originalImage)
                    .forceSize(width == 0 ? height : width, height == 0 ? width : height)
                    .asBufferedImage();
        } else {
            switch (resizeType) {
                case "f" ->
                    // fit
                        thumbImg = Thumbnails.of(originalImage)
                                .forceSize(width, height)
                                .asBufferedImage();
                case "t", "b" -> {
                    // fill and crop from top
                    // fill and crop from bottom
                    thumbImg = Thumbnails.of(originalImage)
                            .size(width, height)
                            .keepAspectRatio(true)
                            .crop(Positions.CENTER)
                            .asBufferedImage();

                    int cropY = "t".equals(resizeType) ? 0 : thumbImg.getHeight() - height;
                    thumbImg = thumbImg.getSubimage(0, cropY, width, height);
                }
                default ->
                    // fill and crop from center
                        thumbImg = Thumbnails.of(originalImage)
                                .size(width, height)
                                .keepAspectRatio(true)
                                .crop(Positions.CENTER)
                                .asBufferedImage();
            }
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(thumbImg, IMAGE_CONTENT_TYPE_FORMAT.get(file.getContentType()), byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        this.uploadFile(new PbFile.PFileBuilder()
                .name(FileUtil.getName(thumbKey))
                .originalName(FileUtil.getName(thumbKey))
                .size(bytes.length)
                .contentType(file.getContentType())
                .content(new ByteArrayInputStream(bytes))
                .lastModifiedTime(System.currentTimeMillis()).build(), thumbKey);
    }

    // Serve serves the file at fileKey location to an HTTP response.
    //
    // If the `download` query parameter is used the file will be always served for
    // download no matter of its type (aka. with "Content-Disposition: attachment").
    public void serve(String servedPath, String servedName) throws Exception {
        PbRequest request = PbHolder.getRequest();
        PbResponse response = PbHolder.getResponse();
        PbFile file = this.getFile(servedPath);

        boolean forceAttachment = false;
        String rawForceAttachment = request.getParameter(FORCE_ATTACHMENT_PARAM);
        if (rawForceAttachment != null && !rawForceAttachment.isEmpty()) {
            forceAttachment = Convert.toBool(rawForceAttachment, Boolean.FALSE);
        }

        String disposition = "attachment";
        String realContentType = file.getContentType();
        if (!forceAttachment && existInArray(realContentType, INLINE_SERVE_CONTENT_TYPES)) {
            disposition = "inline";
        }

        // make an exception for specific content types and force a custom
        // content type to send in the response so that it can be loaded properly
        String extContentType = realContentType;
        String extension = FileUtil.extName(servedName);
        String manualExtensionContentType = MANUAL_EXTENSION_CONTENT_TYPES.get(extension);
        if (manualExtensionContentType != null && !manualExtensionContentType.equals(extContentType)) {
            extContentType = manualExtensionContentType;
        }

        response.setHeader("Content-Disposition", disposition + "; filename=\"" + servedName + "\"");
        response.setContentType(extContentType);
        response.setHeader("Content-Security-Policy", "default-src 'none'; media-src 'self'; style-src 'unsafe-inline'; sandbox");

        // set a default cache-control header
        // (valid for 30 days but the cache is allowed to reuse the file for any requests
        // that are made in the last day while revalidating the res in the background)
        response.setHeader("Cache-Control", "max-age=2592000, stale-while-revalidate=86400");

        response.setDateHeader("Last-Modified", file.getLastModifiedTime());

        try (InputStream contentStream = file.getContent()) {
            IoUtil.copy(contentStream, response.getOutputStream());
        }
    }
}
