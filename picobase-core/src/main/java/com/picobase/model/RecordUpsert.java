package com.picobase.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.context.PbHolder;
import com.picobase.context.model.PbRequest;
import com.picobase.exception.BadRequestException;
import com.picobase.file.PbFile;
import com.picobase.file.PbFileSystem;
import com.picobase.interceptor.InterceptorFunc;
import com.picobase.interceptor.Interceptors;
import com.picobase.json.PbJsonTemplate;
import com.picobase.log.PbLog;
import com.picobase.logic.mapper.CollectionMapper;
import com.picobase.logic.mapper.RecordMapper;
import com.picobase.model.schema.SchemaField;
import com.picobase.model.schema.fieldoptions.FileOptions;
import com.picobase.model.validators.RecordDataValidator;
import com.picobase.persistence.resolver.ListUtil;
import com.picobase.persistence.resolver.ResultCouple;
import com.picobase.util.PbConstants;
import com.picobase.util.TypeSafe;
import com.picobase.validator.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.picobase.file.PbFileSystem.THUMB_PREFIX;
import static com.picobase.model.validators.Validators.uniqueId;
import static com.picobase.persistence.resolver.ListUtil.subtractList;
import static com.picobase.persistence.resolver.ListUtil.toUniqueStringList;
import static com.picobase.util.PbConstants.*;
import static com.picobase.util.PbConstants.FieldName.*;
import static com.picobase.util.PbConstants.FieldType.Email;
import static com.picobase.util.PbConstants.FieldType.File;
import static com.picobase.validator.Err.newError;
import static com.picobase.validator.Validation.*;


/**
 * @author : clown
 * @date : 2024-03-04 15:11
 **/
public class RecordUpsert implements Validatable {
    private String id;
    private boolean manageAccess;
    private RecordModel record;

    // auth collection fields
    // ---
    private String username;
    private String email;
    private boolean emailVisibility;
    private boolean verified;
    private String password;
    private String passwordConfirm;
    private String oldPassword;
    // ---

    private Map<String, Object> data;
    private List<String> filesToDelete;
    private Map<String, List<PbFile>> filesToUpload;

    private PbFileSystem pbFileSystem = PbManager.getPbFileSystem();

    private PbRequest request = PbHolder.getRequest();

    private RecordMapper recordMapper = PbUtil.findMapper(RecordModel.class);
    private CollectionMapper collectionMapper = PbUtil.findMapper(CollectionModel.class);
    private PbJsonTemplate jsonTemplate = PbManager.getPbJsonTemplate();

    private static final PbLog log = PbManager.getLog();

    public RecordUpsert(RecordModel record) {
        this.record = record;
        this.filesToDelete = new ArrayList<>();
        this.filesToUpload = new HashMap<>();
        this.loadFormDefaults();
    }

    private void loadFormDefaults() {
        this.id = this.record.getId();
        if (this.record.getCollection().isAuth()) {
            this.username = this.record.username();
            this.email = this.record.email();
            this.emailVisibility = this.record.emailVisibility();
            this.verified = this.record.verified();
        }
        this.data = new HashMap<>(this.record.getCollection().getSchema().getFields().size());
        for (SchemaField field : this.record.getCollection().getSchema().getFields()) {
            this.data.put(field.getName(), this.record.get(field.getName()));
        }
    }

    /**
     * // LoadRequest extracts the json or multipart/form-data request data
     * // and lods it into the form.
     * //
     * // File upload is supported only via multipart/form-data.
     */
    public void loadRequest() {
        Map<String, Object> requestData;
        Map<String, List<PbFile>> filesToUpload = null;
        if (isJsonContentType()) {
            requestData = PbUtil.createObjFromRequest(Map.class).get();
        } else if (isMultipartFormContentType()) {
            //form 提交，可能带有文件

            MultipartFormData multipartFormData = extractMultipartFormData();
            requestData = multipartFormData.getData();
            filesToUpload = multipartFormData.getFilesToUpload();
        } else {
            throw new BadRequestException("unsupported request content-type");
        }

        filesToUpload.forEach(this::addFiles);

        this.loadData(requestData);


    }

    private boolean isJsonContentType() {
        return request.getContentType().startsWith(APPLICATION_JSON_VALUE);
    }

    private boolean isMultipartFormContentType() {
        return request.getContentType().startsWith(MULTIPART_FORM_DATA_VALUE);
    }

    private MultipartFormData extractMultipartFormData() {


        MultipartFormData mfd = new MultipartFormData();


        String[] arraybleFieldTypes = ArraybleFieldTypes();


        // 先处理 data
        Map<String, String[]> paramMap = request.getParamMap();

        paramMap.forEach((key, values) -> {

            if (ArrayUtil.isEmpty(values)) {
                mfd.getData().put(key, null);
                return;
            }

            // special case for multipart json encoded fields
            if (key.equals(MultipartJsonKey)) {
                for (String value : values) {
                    mfd.getData().putAll(jsonTemplate.parseJsonToObject(value, Map.class));
                }
                return;
            }

            SchemaField field = this.record.getCollection().getSchema().getFieldByName(key);
            if (field != null && ArrayUtil.contains(arraybleFieldTypes, field.getType())) {
                mfd.getData().put(key, values);
            } else {
                mfd.getData().put(key, values[0]);
            }


        });

        // load uploaded files (if any)
        for (SchemaField sf : this.getRecord().getCollection().getSchema().getFields()) {
            if (!Objects.equals(File, sf.getType())) {
                continue; // not a file field
            }

            String key = sf.getName();
            ResultCouple<List<PbFile>> resultCouple = findUploadedFiles(key);
            if (resultCouple.getError() != null || resultCouple.getResult().isEmpty()) {
                // skip invalid or missing file(s)
                continue;
            }

            mfd.getFilesToUpload().put(key, resultCouple.getResult());

        }


        return mfd;
    }

    private ResultCouple<List<PbFile>> findUploadedFiles(String key) {
        List<PbFile> uploadedFiles = PbHolder.getRequest().getPart(key);

        if (Objects.isNull(uploadedFiles)) {
            return new ResultCouple(Collections.EMPTY_LIST);
        }
        return new ResultCouple(uploadedFiles);
    }

    private void addFiles(String key, List<PbFile> files) {
        SchemaField field = record.getCollection().getSchema().getFieldByName(key);
        if (Objects.isNull(field) || !Objects.equals(File, field.getType())) {
            throw new BadRequestException("invalid field key");
        }

        FileOptions options = (FileOptions) field.getOptions();

        if (CollUtil.isEmpty(files)) {
            return; // nothing to upload
        }

        if (Objects.isNull(filesToUpload)) {
            filesToUpload = new HashMap();
        }

        List<String> oldNames = toUniqueStringList(data.get(key));

        if (options.getMaxSelect() == 1) {
            // mark previous file(s) for deletion before replacing
            if (!oldNames.isEmpty()) {
                filesToDelete.addAll(oldNames);
                filesToDelete = toUniqueStringList(filesToDelete);
            }

            // replace
            filesToUpload.put(key, List.of(files.get(0)));
            data.put(key, field.prepareValue(files.get(0).getName()));
        } else {
            // append
            List<PbFile> multipartFiles = Optional.ofNullable(filesToUpload.get(key)).orElse(new ArrayList<>());
            multipartFiles.addAll(files);
            filesToUpload.put(key, multipartFiles);
            oldNames.addAll(files.stream().map(f -> f.getName()).collect(Collectors.toList()));
            data.put(key, field.prepareValue(oldNames));
        }
    }

    /**
     * LoadData loads and normalizes the provided regular record data fields into the form
     *
     * @param requestData
     */
    private void loadData(Map<String, Object> requestData) {
        // load base system fields
        if (Objects.nonNull(requestData.get(Id))) {
            id = TypeSafe.anyToString(requestData.get(Id));
        }

        // load auth system fields
        if (record.getCollection().isAuth()) {
            if (Objects.nonNull(requestData.get(Username))) {
                username = TypeSafe.anyToString(requestData.get(Username));
            }
            if (Objects.nonNull(requestData.get(Email))) {
                email = TypeSafe.anyToString(requestData.get(Email));
            }
            if (Objects.nonNull(requestData.get(EmailVisibility))) {
                emailVisibility = TypeSafe.anyToBool(requestData.get(EmailVisibility));
            }
            if (Objects.nonNull(requestData.get(Verified))) {
                verified = TypeSafe.anyToBool(requestData.get(Verified));
            }
            if (Objects.nonNull(requestData.get(Password))) {
                password = TypeSafe.anyToString(requestData.get(Password));
            }
            if (Objects.nonNull(requestData.get(PasswordConfirm))) {
                passwordConfirm = TypeSafe.anyToString(requestData.get(PasswordConfirm));
            }
            if (Objects.nonNull(requestData.get(OldPassword))) {
                oldPassword = TypeSafe.anyToString(requestData.get(OldPassword));
            }
        }

        // replace modifiers (if any)  requestData = record.replaceModifers(requestData);
        requestData = record.replaceModifers(requestData);

        // create a shallow copy of form.data
        var extendedData = new HashMap<>(this.data);

        // extend form.data with the request data
        extendedData.putAll(requestData);

        for (SchemaField field : record.getCollection().getSchema().getFields()) {
            String key = field.getName();
            var value = field.prepareValue(extendedData.get(key));

            if (!Objects.equals(File, field.getType())) {
                data.put(key, value);
                continue;
            }

            // TODO 新增记录，无需维护历史文件，此处做优化
            if (record.isNew()) {
                continue;
            }

            // -----------------------------------------------------------
            // Delete previously uploaded file(s)
            // -----------------------------------------------------------
            List<String> oldNames = toUniqueStringList(record.get(key));
            List<String> submittedNames = toUniqueStringList(value);

            // ensure that all submitted names are existing to prevent accidental files deletions
            if (submittedNames.size() > oldNames.size() || !subtractList(submittedNames, oldNames).isEmpty()) {
                throw new BadRequestException("validation_unknown_filenames The field contains unknown filenames");
            }

            // if empty value was set, mark all previously uploaded files for deletion
            // otherwise check for "deleted" (aka. unsubmitted) file names
            if (submittedNames.isEmpty() && !oldNames.isEmpty()) {
                removeFiles(key, new ArrayList());
            } else if (oldNames.size() > 0) {
                List<String> toDelete = oldNames.stream().filter(n -> !submittedNames.contains(n)).collect(Collectors.toList());
                if (!toDelete.isEmpty()) {
                    removeFiles(key, toDelete);
                }
            }

            // allow file key reasignments for file names sorting
            // (only if all submitted values already exists)
            if (!submittedNames.isEmpty() && subtractList(submittedNames, oldNames).isEmpty()) {
                data.put(key, submittedNames);
            }
        }
    }

    private void removeFiles(String key, List<String> toDelete) {
        SchemaField field = record.getCollection().getSchema().getFieldByName(key);
        if (Objects.isNull(field) || !Objects.equals(File, field.getType())) {
            throw new BadRequestException("invalid field key");
        }

        List<String> existing = ListUtil.toUniqueStringList(data.get(key));

        // mark all files for deletion
        if (Objects.isNull(toDelete)) {
            toDelete = new ArrayList();
        }

        if (toDelete.isEmpty()) {
            toDelete.addAll(existing);
        }

        // check for existing files
        Iterator<String> existingIterator = existing.iterator();
        while (existingIterator.hasNext()) {
            String exist = existingIterator.next();
            if (toDelete.contains(exist)) {
                filesToDelete.add(exist);
            }
            existingIterator.remove();
        }

        // check for newly uploaded files
        List<String> finalToDelete = toDelete;
        filesToUpload.put(key, Optional.ofNullable(filesToUpload.get(key)).orElse(new ArrayList<>()).stream().filter(f -> !finalToDelete.contains(f.getName())).collect(Collectors.toList()));

        data.put(key, field.prepareValue(existing));
    }


    private final Pattern usernameRegex = Pattern.compile("^[\\w][\\w\\.\\-]*$");


    @Override
    public Errors validate() {
        // base form fields validator
        var baseFieldsRules = new ArrayList<>();
        baseFieldsRules.add(field(RecordUpsert::getId, when(this.record.isNew(), length(DEFAULT_ID_LENGTH, DEFAULT_ID_LENGTH), match(ID_REGEX_P), by(uniqueId(this.record.tableName())))
                .otherwise(in(this.record.getId()))));

        // auth fields validators
        if (this.record.getCollection().isAuth()) {
            baseFieldsRules.add(field(RecordUpsert::getUsername
                    // require only on update, because on create we fallback to auto generated username
                    , when(!this.record.isNew(), required), length(3, 150), match(usernameRegex), by(checkUniqueUsername())));

            baseFieldsRules.add(field(RecordUpsert::getEmail, when(this.record.getCollection().authOptions().isRequireEmail(), required)
                    // don't allow direct email change (or unset) if the form doesn't have manage access permissions
                    // (aka. allow only admin or authorized auth models to directly update the field)
                    , when(!this.record.isNew() && this.manageAccess, in(this.record.email())), length(1, 255), Is.EmailFormat, by(checkEmailDomain()), by(checkUniqueEmail())));

            baseFieldsRules.add(field(RecordUpsert::isVerified
                    // don't allow changing verified if the form doesn't have manage access permissions
                    // (aka. allow only admin or authorized auth models to directly change the field)
                    , when(!this.manageAccess, in(this.record.verified()))));

            baseFieldsRules.add(field(RecordUpsert::getPassword, when((this.record.isNew() || StrUtil.isNotBlank(this.passwordConfirm) || StrUtil.isNotBlank(this.oldPassword)), required), length(this.record.getCollection().authOptions().getMinPasswordLength(), 72)));

            baseFieldsRules.add(field(RecordUpsert::getPasswordConfirm, when((this.record.isNew() || StrUtil.isNotBlank(this.password) || StrUtil.isNotBlank(this.oldPassword)), required), by(compare(this.password))));

            baseFieldsRules.add(field(RecordUpsert::getOldPassword
                    // require old password only on update when:
                    // - form.manageAccess is not set
                    // - changing the existing password
                    , when(!this.record.isNew() && !this.manageAccess && (StrUtil.isNotBlank(this.password) || StrUtil.isNotBlank(this.passwordConfirm)), required, by(checkOldPassword()))));
        }

        Errors err = validateObject(this, baseFieldsRules.toArray(new FieldRules[0]));
        if (null != err) {
            return err;
        }

        return new RecordDataValidator(recordMapper, this.record, this.filesToUpload).validate(this.data);
    }


    private RuleFunc checkOldPassword() {
        return (value) -> {
            String v = (String) value;
            if (StrUtil.isEmpty(v)) {
                return null; // nothing to check
            }

            if (!this.record.validatePassword(v)) {
                return newError("validation_invalid_old_password", "Missing or invalid old password.");
            }

            return null;
        };
    }


    private RuleFunc checkUniqueEmail() {
        return (value) -> {
            String v = (String) value;
            if (StrUtil.isEmpty(v)) {
                return null;
            }

            boolean isUnique = recordMapper.isRecordValueUnique(this.record.getCollection().getId(), PbConstants.FieldName.Email, v, this.record.getId());
            if (!isUnique) {
                return newError("validation_invalid_email", "The email is invalid or already in use.");
            }

            return null;
        };
    }

    private RuleFunc checkEmailDomain() {
        return (value) -> {
            String val = (String) value;
            if (val == null || val.isEmpty()) {
                return null; // nothing to check
            }

            String domain = val.substring(val.lastIndexOf("@") + 1);
            List<String> only = this.record.getCollection().authOptions().getOnlyEmailDomains();
            List<String> except = this.record.getCollection().authOptions().getExceptEmailDomains();

            // only domains check
            if (only != null && only.size() > 0 && !ListUtil.existInSlice(domain, only)) {
                return newError("validation_email_domain_not_allowed", "Email domain is not allowed.");
            }

            // except domains check
            if (except != null && except.size() > 0 && !ListUtil.existInSlice(domain, except)) {
                return newError("validation_email_domain_not_allowed", "Email domain is not allowed.");
            }

            return null;
        };
    }


    public RuleFunc checkUniqueUsername() {
        return (value) -> {
            String v = (String) value;
            if (StrUtil.isEmpty(v)) {
                return null;
            }

            boolean isUnique = recordMapper.isRecordValueUnique(this.record.getCollection().getId(), PbConstants.FieldName.Username, v, this.record.getId());
            if (!isUnique) {
                return newError("validation_invalid_username", "The username is invalid or already in use.");
            }

            return null;
        };
    }


    private RuleFunc compare(String valueToCompare) {
        return value -> {
            String v = (String) value;

            if (!v.equals(valueToCompare)) {
                return newError("validation_values_mismatch", "Values don't match.");
            }

            return null;
        };
    }


    public String getId() {
        return id;
    }

    public RecordUpsert setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isManageAccess() {
        return manageAccess;
    }

    public RecordUpsert setManageAccess(boolean manageAccess) {
        this.manageAccess = manageAccess;
        return this;
    }

    public RecordModel getRecord() {
        return record;
    }

    public RecordUpsert setRecord(RecordModel record) {
        this.record = record;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public RecordUpsert setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public RecordUpsert setEmail(String email) {
        this.email = email;
        return this;
    }

    public boolean isEmailVisibility() {
        return emailVisibility;
    }

    public RecordUpsert setEmailVisibility(boolean emailVisibility) {
        this.emailVisibility = emailVisibility;
        return this;
    }

    public boolean isVerified() {
        return verified;
    }

    public RecordUpsert setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RecordUpsert setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public RecordUpsert setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
        return this;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public RecordUpsert setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public RecordUpsert setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public List<String> getFilesToDelete() {
        return filesToDelete;
    }

    public RecordUpsert setFilesToDelete(List<String> filesToDelete) {
        this.filesToDelete = filesToDelete;
        return this;
    }

    public Map<String, List<PbFile>> getFilesToUpload() {
        return filesToUpload;
    }

    public RecordUpsert setFilesToUpload(Map<String, List<PbFile>> filesToUpload) {
        this.filesToUpload = filesToUpload;
        return this;
    }

    /**
     * // DrySubmit performs a form submit within a transaction and reverts it.
     * // For actual record persistence, check the `form.Submit()` method.
     * //
     * // This method doesn't handle file uploads/deletes or trigger any app events!
     */
    public void drySubmit(Consumer<Object> action) {
        Errors err = this.validateAndFill();
        if (err != null) {
            throw new BadRequestException(err);
        }

        PbManager.getPbDatabaseOperate().runInTransaction(s -> {
            saveRecord();

            if (action != null) {
                action.accept(s);
            }

            return null;
        }, true);
    }

    /**
     * // Submit validates the form and upserts the form Record model.
     * //
     * // You can optionally provide a list of InterceptorFunc to further
     * // modify the form behavior before persisting it.
     */
    public RecordModel submit(InterceptorFunc<RecordModel, RecordModel>... funcs) {
        Errors err = this.validateAndFill();
        if (err != null) {
            throw new BadRequestException(err);
        }

        return Interceptors.run(this.record, r -> {
            this.record = r;
            if (!this.record.hasId()) {
                this.record.refreshId();
            }

            // upload new files (if any)
            processFilesToUpload();

            saveRecord();

            // delete old files (if any)
            processFilesToDelete();

            return this.record;
        }, funcs);
    }

    private void processFilesToDelete() {
        deleteFilesByNamesList(getFilesToDelete());
    }


    // SaveRecord persists the provided Record model in the database.
    // If record.IsNew() is true, the method will perform a creation, otherwise an update.
    // To explicitly mark a record for update you can use record.MarkAsNotNew().
    public void saveRecord() {
        RecordModel record = this.getRecord();
        if (record.getCollection().isAuth()) {
            if (StrUtil.isBlank(record.username())) {
                throw new BadRequestException("unable to save auth record without username");
            }

            // Cross-check that the auth record id is unique for all auth collections.
            // This is to make sure that the filter `@request.auth.id` always returns a unique id.
            List<CollectionModel> authCollections = collectionMapper.findCollectionsByType(PbConstants.CollectionType.Auth);
            if (CollUtil.isEmpty(authCollections)) {
                throw new BadRequestException("unable to fetch the auth collections for cross-id unique check");
            }

            for (CollectionModel collection : authCollections) {
                if (Objects.equals(record.getCollection().getId(), collection.getId())) {
                    continue; // skip current collection (sqlite will do the check for us)
                }

                boolean isUnique = recordMapper.isRecordValueUnique(collection.getId(), PbConstants.FieldName.Id, record.getId());
                if (!isUnique) {
                    throw new BadRequestException("the auth record ID must be unique across all auth collections");
                }
            }
        }

        if (record.isNew()) {
            recordMapper.createRecord(record);
        } else {
            recordMapper.updateRecord(record);
        }
    }


    private void processFilesToUpload() {
        var upsert = this;
        if (CollUtil.isEmpty(upsert.getFilesToUpload())) {
            return; // no parsed file fields
        }

        if (!upsert.getRecord().hasId()) {
            throw new BadRequestException("the record doesn't have an id");
        }

        List<PbFile> files = upsert.getFilesToUpload().values().stream().flatMap(List::stream).collect(Collectors.toList());
        List<String> uploaded = new ArrayList<>(files.size());
        for (PbFile file : files) {
            try {
                String path = upsert.getRecord().baseFilesPath() + "/" + file.getName();
                pbFileSystem.uploadFile(file, path);
                uploaded.add(file.getName());
            } catch (Exception e) {
                log.error("failed to upload file", e);
                // cleanup - try to delete the successfully uploaded files (if any)
                deleteFilesByNamesList(uploaded);
                throw new BadRequestException("failed to upload all files");
            }
        }
    }


    private void deleteFilesByNamesList(List<String> filenames) {
        if (CollUtil.isEmpty(filenames)) {
            return; // nothing to delete
        }

        if (!this.getRecord().hasId()) {
            throw new BadRequestException("the record doesn't have an id");
        }

        List<String> deleteErrors = new ArrayList<>(filenames.size());
        for (String filename : filenames) {
            try {
                pbFileSystem.delete(this.getRecord().baseFilesPath() + "/" + filename);
                // try to delete the related file thumbs (if any)
                pbFileSystem.deletePrefix(this.getRecord().baseFilesPath() + "/" + THUMB_PREFIX + filename + "/");
            } catch (Exception e) {
                log.error("failed to delete file", e);
                deleteErrors.add(filename);
            }
        }

        if (!deleteErrors.isEmpty()) {
            throw new BadRequestException("failed to delete all files");
        }
    }

    private Errors validateAndFill() {
        Errors err = this.validate();
        if (err != null) {
            return err;
        }
        boolean isNew = this.record.isNew();

        // custom insertion id can be set only on create
        if (isNew && StrUtil.isNotEmpty(this.id)) {
            this.record.setId(this.id);
        }
        // set auth fields
        if (this.record.getCollection().isAuth()) {
            // generate a default username during create (if missing)
            if (this.record.isNew() && StrUtil.isEmpty(this.username)) {
                String baseUsername = this.record.getCollection().getName() + RandomUtil.randomInt(5);
                this.username = recordMapper.suggestUniqueAuthRecordUsername(this.record.getCollection().getId(), baseUsername);
            }

            if (!StrUtil.isBlank(this.getUsername())) {
                record.set(PbConstants.FieldName.Username, this.username);
            }

            if (record.isNew() || this.isManageAccess()) {
                record.set(PbConstants.FieldName.Email, this.email);
            }

            record.set(PbConstants.FieldName.EmailVisibility, this.emailVisibility);

            if (this.isManageAccess()) {
                record.set(PbConstants.FieldName.Verified, this.verified);
            }

            if (!StrUtil.isBlank(this.password) && this.password.equals(this.passwordConfirm)) {
                record.setPassword(this.password);
            }


        }
        // bulk load the remaining form data
        record.load(this.getData());
        return null;
    }
}
