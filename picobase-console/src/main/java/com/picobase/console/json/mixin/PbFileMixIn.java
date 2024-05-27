package com.picobase.console.json.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.picobase.file.PbFile;

import java.io.InputStream;

public abstract class PbFileMixIn {
    @JsonIgnore
    private InputStream content;

    @JsonProperty(PbFile.ORIGINAL_FILENAME)
    private String originalName;
}
