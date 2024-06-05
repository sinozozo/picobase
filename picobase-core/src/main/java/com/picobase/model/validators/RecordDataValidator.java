package com.picobase.model.validators;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.picobase.PbManager;
import com.picobase.exception.ForbiddenException;
import com.picobase.file.PbFile;
import com.picobase.logic.mapper.RecordMapper;
import com.picobase.model.CollectionModel;
import com.picobase.model.RecordModel;
import com.picobase.model.schema.SchemaField;
import com.picobase.model.schema.fieldoptions.*;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.resolver.ListUtil;
import com.picobase.util.PbConstants;
import com.picobase.validator.Err;
import com.picobase.validator.Errors;
import com.picobase.validator.Is;
import com.picobase.validator.RuleFunc;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.picobase.validator.Err.newError;
import static com.picobase.validator.ThresholdRule.max;
import static com.picobase.validator.ThresholdRule.min;
import static com.picobase.validator.Validation.required;

public class RecordDataValidator {

    private final RecordMapper recordMapper;
    private final RecordModel record;
    private final Map<String, List<PbFile>> uploadedFiles;
    List<String> emptyJsonValues = CollUtil.newArrayList("null", "\"\"", "[]", "{}");
    Err requiredErr = newError("validation_required", "Missing required value");

    public RecordDataValidator(RecordMapper recordMapper, RecordModel record, Map<String, List<PbFile>> uploadedFiles) {
        this.recordMapper = recordMapper;
        this.record = record;
        this.uploadedFiles = uploadedFiles;
    }


    /**
     * // Validate validates the provided `data` by checking it against
     * // the validator record constraints and schema.
     */
    public Errors validate(Map<String, Object> data) {
        Map<String, SchemaField> keyedSchema = this.record.getCollection().getSchema().asMap();
        if (CollUtil.isEmpty(keyedSchema)) {
            return null; // no fields to check
        }

        if (CollUtil.isEmpty(data)) {
            throw new ForbiddenException(newError("validation_empty_data", "No data to validate").message());
        }

        Errors errs = new Errors();

        // check for unknown fields
        for (String key : data.keySet()) {
            if (!keyedSchema.containsKey(key)) {
                errs.put(key, newError("validation_unknown_field", "Unknown field"));
            }
        }

        if (null != errs.filter()) {
            return errs;
        }

        for (Map.Entry<String, SchemaField> entry : keyedSchema.entrySet()) {
            String key = entry.getKey();
            SchemaField field = entry.getValue();

            // normalize value to emulate the same behavior
            // when fetching or persisting the record model
            Object value = field.prepareValue(data.get(key));

            // check required constraint
            if (field.isRequired() && null != required.validate(value)) {
                errs.put(key, newError("validation_required", "Missing required value"));
                continue;
            }

            // validate field value by its field type
            Err err = checkFieldValue(field, value);
            if (err != null) {
                errs.put(key, err);
            }
        }

        if (errs.isEmpty()) {
            return null;
        }
        return errs;
    }

    public Err checkFieldValue(SchemaField field, Object value) {
        switch (field.getType()) {
            case PbConstants.FieldType.Text -> {
                return checkTextValue(field, value);
            }
            case PbConstants.FieldType.Number -> {
                return checkNumberValue(field, value);
            }
            case PbConstants.FieldType.Bool -> {
                return checkBoolValue(field, value);
            }
            case PbConstants.FieldType.Email -> {
                return checkEmailValue(field, value);
            }
            case PbConstants.FieldType.Url -> {
                return checkUrlValue(field, value);
            }
            case PbConstants.FieldType.Editor -> {
                return checkEditorValue(field, value);
            }
            case PbConstants.FieldType.Date -> {
                return checkDateValue(field, value);
            }
            case PbConstants.FieldType.Select -> {
                return checkSelectValue(field, value);
            }
            case PbConstants.FieldType.Json -> {
                return checkJsonValue(field, value);
            }
            case PbConstants.FieldType.File -> {
                return checkFileValue(field, value);
            }
            case PbConstants.FieldType.Relation -> {
                return checkRelationValue(field, value);
            }
        }
        return null;
    }


    public Err checkTextValue(SchemaField field, Object value) {
        String val = Convert.toStr(value);
        if (StrUtil.isEmpty(val)) {
            return null; // nothing to check (skip zero-defaults)
        }

        field.initOptions();
        TextOptions options = (TextOptions) field.getOptions();

        // note: casted to []rune to count multi-byte chars as one
        int length = val.codePointCount(0, val.length());

        if (options.getMin() != null && length < options.getMin()) {
            return newError("validation_min_text_constraint", String.format("Must be at least %d character(s)", options.getMin()));
        }

        if (options.getMax() != null && length > options.getMax()) {
            return newError("validation_max_text_constraint", String.format("Must be less than %d character(s)", options.getMax()));
        }

        if (StrUtil.isNotEmpty(options.getPattern())) {
            if (ReUtil.isMatch(options.getPattern(), val)) {
                return newError("validation_invalid_format", "Invalid value format");
            }
        }
        return null;
    }


    public Err checkNumberValue(SchemaField field, Object value) {
        double val = Convert.toDouble(value);
        if (val == 0) {
            return null; // nothing to check (skip zero-defaults)
        }

        field.initOptions();
        NumberOptions options = (NumberOptions) field.getOptions();


        if (options.isNoDecimal() && val != (double) (long) val) {
            return newError("validation_no_decimal_constraint", "Decimal numbers are not allowed");
        }

        if (options.getMin() != null && val < options.getMin()) {
            return newError("validation_min_number_constraint", String.format("Must be larger than %d", options.getMin()));
        }

        if (options.getMax() != null && val > options.getMax()) {
            return newError("validation_max_number_constraint", String.format("Must be less than %d", options.getMax()));
        }
        return null;
    }

    public Err checkBoolValue(SchemaField field, Object value) {
        return null;
    }

    public Err checkEmailValue(SchemaField field, Object value) {
        String val = Convert.toStr(value);
        if (val == null || val.isEmpty()) {
            return null; // nothing to check
        }

        if (Is.EmailFormat.validate(val) != null) {
            return newError("validation_invalid_email", "Must be a valid email");
        }

        field.initOptions();
        EmailOptions options = (EmailOptions) field.getOptions();
        String domain = val.substring(val.lastIndexOf("@") + 1);

        // only domains check
        if (CollUtil.isNotEmpty(options.getOnlyDomains()) && !ListUtil.existInSlice(domain, options.getOnlyDomains())) {
            return newError("validation_email_domain_not_allowed", "Email domain is not allowed");
        }

        // except domains check
        if (CollUtil.isNotEmpty(options.getExceptDomains()) && ListUtil.existInSlice(domain, options.getExceptDomains())) {
            return newError("validation_email_domain_not_allowed", "Email domain is not allowed");
        }
        return null;
    }

    public Err checkUrlValue(SchemaField field, Object value) {
        var val = Convert.toStr(value);
        if (StrUtil.isEmpty(val)) {
            return null; // nothing to check
        }

        if (Is.URL.validate(val) != null) {
            return newError("validation_invalid_url", "Must be a valid url");
        }

        field.initOptions();
        UrlOptions options = (UrlOptions) field.getOptions();

        // extract host/domain
        URL u = URLUtil.toUrlForHttp(val);
        String host = u.getHost();

        // only domains check
        if (CollUtil.isNotEmpty(options.getOnlyDomains()) && !ListUtil.existInSlice(host, options.getOnlyDomains())) {
            return newError("validation_url_domain_not_allowed", "Url domain is not allowed");
        }

        // except domains check
        if (CollUtil.isNotEmpty(options.getExceptDomains()) && !ListUtil.existInSlice(host, options.getExceptDomains())) {
            return newError("validation_url_domain_not_allowed", "Url domain is not allowed");
        }
        return null;
    }

    public Err checkEditorValue(SchemaField field, Object value) {
        return null;
    }

    public Err checkDateValue(SchemaField field, Object value) {
        LocalDateTime val = Convert.toLocalDateTime(value);
        if (null == val) {
            if (field.isRequired()) {
                return requiredErr;
            }
            return null; // nothing to check
        }

        field.initOptions();
        DateOptions options = (DateOptions) field.getOptions();

        if (null != options.getMin()) {
            Err err = min(options.getMin()).validate(val);
            if (null != err) {
                return err;
            }
        }

        if (null != options.getMax()) {
            Err err = max(options.getMax()).validate(val);
            if (null != err) {
                return err;
            }
        }

        return null;
    }

    public Err checkSelectValue(SchemaField field, Object value) {
        List<String> normalizedVal = ListUtil.toUniqueStringList(value);
        if (normalizedVal.isEmpty()) {
            if (field.isRequired()) {
                return requiredErr;
            }
            return null; // nothing to check
        }

        field.initOptions();
        SelectOptions options = (SelectOptions) field.getOptions();

        // check max selected items
        if (normalizedVal.size() > options.getMaxSelect()) {
            return newError("validation_too_many_values", String.format("Select no more than %d", options.getMaxSelect()));
        }

        // check against the allowed values
        for (String val : normalizedVal) {
            if (!ListUtil.existInSlice(val, options.getValues())) {
                return newError("validation_invalid_value", "Invalid value " + val);
            }
        }

        return null;
    }

    public Err checkJsonValue(SchemaField field, Object value) {


        String raw;
        try {
            raw = PbManager.getPbJsonTemplate().toJsonString(value);
        } catch (Exception e) {
            return newError("validation_invalid_json", "Must be a valid json value");
        }

        field.initOptions();
        JsonOptions options = (JsonOptions) field.getOptions();
        if (raw.length() > options.getMaxSize()) {
            return newError("validation_json_size_limit", String.format("The maximum allowed JSON size is %d bytes", options.getMaxSize()));
        }

        String rawStr = raw.trim();
        if (field.isRequired() && ListUtil.existInSlice(rawStr, emptyJsonValues)) {
            return requiredErr;
        }

        return null;
    }

    public Err checkFileValue(SchemaField field, Object value) {
        List<String> names = ListUtil.toUniqueStringList(value);
        if (names.isEmpty() && field.isRequired()) {
            return requiredErr;
        }

        FileOptions options = (FileOptions) field.getOptions();

        if (names.size() > options.getMaxSelect()) {
            return newError("validation_too_many_values", String.format("Select no more than %d", options.getMaxSelect()));
        }

        if (CollUtil.isEmpty(uploadedFiles)) {
            return null;
        }

        // extract the uploaded files
        List<PbFile> files = new ArrayList<>();
        for (PbFile file : uploadedFiles.get(field.getName())) {
            if (ListUtil.existInSlice(file.getName(), names)) {
                files.add(file);
            }
        }

        for (PbFile file : files) {
            // check size
            Err sizeError = uploadedFileSize(options.getMaxSize()).apply(file);
            if (sizeError != null) {
                return sizeError;
            }

            // check type
            if (!options.getMimeTypes().isEmpty()) {
                Err mimeTypeError = uploadedFileMimeType(options.getMimeTypes()).apply(file);
                if (mimeTypeError != null) {
                    return mimeTypeError;
                }
            }
        }

        return null;
    }

    private RuleFunc uploadedFileMimeType(List<String> validTypes) {
        return (Object value) -> {
            PbFile v = (PbFile) value;
            if (v == null) {
                return null; // nothing to validate
            }

            Err baseErr = newError("validation_invalid_mime_type",
                    String.format("Failed to upload %s due to unsupported file type.", v.getOriginalName()));

            if (CollUtil.isEmpty(validTypes)) {
                return baseErr;
            }


            String mimeType = v.getContentType();
            for (String t : validTypes) {
                if (t.contains(mimeType)) {
                    return null; // valid
                }
            }
            return newError("validation_invalid_mime_type", String.format("%s mime type must be one of: %s.", v.getName(), String.join(", ", validTypes)));
        };
    }


    private RuleFunc uploadedFileSize(int maxBytes) {
        return (Object value) -> {
            PbFile v = (PbFile) value;
            if (v == null) {
                return null; // nothing to validate
            }

            if (v.getSize() > maxBytes) {
                return newError("validation_file_size_limit", String.format("Failed to upload %s - the maximum allowed file size is %d bytes.", v.getOriginalName(), maxBytes));
            }

            return null;
        };
    }

    public Err checkRelationValue(SchemaField field, Object value) {
        List<String> ids = ListUtil.toUniqueStringList(value);
        if (ids.isEmpty()) {
            if (field.isRequired()) {
                return requiredErr;
            }
            return null; // nothing to check
        }

        RelationOptions options = (RelationOptions) field.getOptions();

        if (options.getMinSelect() != null && ids.size() < options.getMinSelect()) {
            return newError("validation_not_enough_values", String.format("Select at least %d", options.getMinSelect()));
        }

        if (options.getMaxSelect() != null && ids.size() > options.getMaxSelect()) {
            return newError("validation_too_many_values", String.format("Select no more than %d", options.getMaxSelect()));
        }

        // check if the related records exist
        Optional<CollectionModel> relCollectionOpt = recordMapper.findCollectionByNameOrId(options.getCollectionId());
        CollectionModel relCollection = relCollectionOpt.orElse(null);

        long total = recordMapper.recordQuery(relCollection).select("count(*)").andWhere(Expression.in("id", ids)).count();
        if (total != ids.size()) {
            return newError("validation_missing_rel_records", "Failed to find all relation records with the provided ids");
        }

        return null;
    }

}
