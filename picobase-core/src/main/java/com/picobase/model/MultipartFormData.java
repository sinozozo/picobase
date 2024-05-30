package com.picobase.model;

import com.picobase.file.PbFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartFormData {
    private Map<String, Object> data = new HashMap<>();
    private Map<String, List<PbFile>> filesToUpload = new HashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public MultipartFormData setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public Map<String, List<PbFile>> getFilesToUpload() {
        return filesToUpload;
    }

    public MultipartFormData setFilesToUpload(Map<String, List<PbFile>> filesToUpload) {
        this.filesToUpload = filesToUpload;
        return this;
    }
}
