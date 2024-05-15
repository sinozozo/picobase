package com.picobase.model.schema.fieldoptions;


import com.picobase.model.schema.MultiValuer;
import com.picobase.validator.Errors;

import java.util.List;

import static com.picobase.util.PbConstants.ThumbSizeRegex;
import static com.picobase.validator.Validation.*;

public class FileOptions implements FieldOptions, MultiValuer {
    private List<String> mimeTypes;
    private List<String> thumbs;
    private int maxSelect;
    private int maxSize;


    /**
     * 不为该字段生成 get set 方法。
     */
    //@JsonProperty("protected")
    private boolean isProtected;

    /**
     * 用于将form 形式提交过来的 protected 映射到 isProtected上
     *
     * @param isProtected
     */
    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public boolean getProtected() {
        return this.isProtected;
    }


    //@JsonIgnore
    @Override
    public boolean isMultiple() {
        return this.maxSelect > 1;
    }


    @Override
    public Errors validate() {
        return validateObject(this,
                field(FileOptions::getMaxSelect, required, min(1)),
                field(FileOptions::getMaxSize, required, min(1)),
                field(FileOptions::getThumbs, each(notIn("0x0", "0x0t", "0x0b", "0x0f"), match(ThumbSizeRegex)))
        );
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    public FileOptions setMimeTypes(List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
        return this;
    }

    public List<String> getThumbs() {
        return thumbs;
    }

    public FileOptions setThumbs(List<String> thumbs) {
        this.thumbs = thumbs;
        return this;
    }

    public int getMaxSelect() {
        return maxSelect;
    }

    public FileOptions setMaxSelect(int maxSelect) {
        this.maxSelect = maxSelect;
        return this;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public FileOptions setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

}
