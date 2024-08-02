package com.picobase.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.exception.PbException;
import com.picobase.model.schema.Schema;
import com.picobase.model.schema.SchemaField;
import com.picobase.persistence.resolver.ListUtil;
import com.picobase.secure.BCrypt;
import com.picobase.util.TypeSafe;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.picobase.error.PbErrorCode.CODE_14000;
import static com.picobase.error.PbErrorCode.CODE_14001;
import static com.picobase.util.PbConstants.FieldName.*;
import static com.picobase.util.PbConstants.FieldType.File;
import static com.picobase.util.PbConstants.*;


//@JsonSerialize(using = RecordJsonSerialize.class)
public class RecordModel extends BaseModel implements ColumnValueMapper {

    private CollectionModel collection;
    private boolean exportUnknown; // whether to export unknown fields
    private boolean ignoreEmailVisibility; // whether to ignore the emailVisibility flag for auth collections
    private boolean loaded;
    private Map<String, Object> originalData; // the original (aka. first loaded) model data
    private Store<Object> expand; // expanded relations
    private Store<Object> data; // any custom data in addition to the base model fields
    private ConcurrentHashMap<String, Object> publicData; // all public export data
    private boolean alreadyExported = false;

    /**
     * 默认 新建时 为new 对象， 数据库加载后 变为 false
     */
    private boolean isNew = true;

    public boolean isNew() {
        return isNew;
    }

    public BaseModel setNew(boolean aNew) {
        isNew = aNew;
        return this;
    }


    public RecordModel(CollectionModel collection) {
        this.collection = collection;
        this.data = new Store<>();
    }


    @Override
    public String tableName() {
        return this.collection.getName();
    }


    /**
     * Load bulk loads the provided data into the current Record model.
     */
    public void load(Map<String, Object> resultMap) {
        if (!loaded) {
            loaded = true;
            originalData = resultMap;
        }

        resultMap.keySet().forEach(key -> this.set(key, resultMap.get(key)));
    }

    /**
     * Set sets the provided key-value data pair for the current Record model.
     * <p>
     * If the record collection has field with name matching the provided "key",
     * the value will be further normalized according to the field rules.
     */
    public void set(String key, Object value) {
        switch (key) {
            case Id -> this.setId(TypeSafe.anyToString(value));
            case Created -> this.setCreated(TypeSafe.anyToLocalDateTime(value));
            case Updated -> this.setUpdated(TypeSafe.anyToLocalDateTime(value));
            case Expand -> this.setExpand(toStringMap(value));
            default -> {
                var v = value;


                SchemaField field = this.collection.getSchema().getFieldByName(key);
                if (field != null) {
                    v = field.prepareValue(v);
                } else if (this.collection.isAuth()) {
                    v = normalizeAuthField(key, value);
                }

                if (this.data == null) {
                    this.data = new Store<>();
                }

                this.data.set(key, v);
            }

        }
    }

    /**
     * 将 任意 map 和 String 转换成 map 类型
     */
    private Map<String, Object> toStringMap(Object obj) {
        Map<String, Object> m = TypeSafe.anyToMap(obj);
        if (m != null) {
            return m;
        }

        if (obj != null && obj instanceof String jsonstr) { // m 为null obj不为null， 尝试转换成map
            try {
                return PbManager.getPbJsonTemplate().parseJsonToMap(jsonstr);
            } catch (Throwable ignored) {
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    private Object normalizeAuthField(String key, Object value) {
        return switch (key) {
            case EmailVisibility, Verified -> TypeSafe.anyToBool(value);
            case LastResetSentAt, LastVerificationSentAt -> TypeSafe.anyToLocalDateTime(value);
            case Username, Email, TokenKey, PasswordHash -> TypeSafe.anyToString(value);
            default -> value;
        };
    }

    public Object get(String key) {
        switch (key) {
            case Id:
                return this.getId();
            case Created:
                return this.getCreated();
            case Updated:
                return this.getUpdated();
            default:
                Object v = null;
                if (this.data != null) {
                    v = this.data.get(key);
                }

                // normalize the field value in case it is missing or an incorrect type was set
                // to ensure that the DB will always have normalized columns value.
                var field = this.collection.getSchema().getFieldByName(key);
                if (field != null) {
                    v = field.prepareValue(v);
                } else if (this.collection.isAuth()) {
                    v = normalizeAuthField(key, v);
                }
                return v;

        }
    }

    /**
     * PublicExport exports only the record fields that are safe to be public.
     * <p>
     * For auth records, to force the export of the email field you need to set
     * `this.ignoreEmailVisibility(true)`.
     */
    public Map<String, Object> publicExport() {
        ConcurrentHashMap<String, Object> result = new ConcurrentHashMap<>(this.collection.getSchema().getFields().size() + 5);
        // export unknown data fields if allowed
        if (this.exportUnknown) {
            result.putAll(this.unknownData());
        }

        // export schema field values
        this.collection.getSchema().getFields().forEach(field -> {
            var v = this.get(field.getName());
            result.put(field.getName(), v == null ? "" : v);
        });


        // export some of the safe auth collection fields
        if (this.collection.isAuth()) {
            result.put(Verified, this.verified());
            result.put(Username, this.username());
            result.put(EmailVisibility, this.emailVisibility());
            if (this.ignoreEmailVisibility || this.emailVisibility()) {
                result.put(Email, this.email());
            }
        }

        // export base model fields
        result.put(Id, TypeSafe.anyToString(this.getId(), ""));
        if (!this.collection.isView() || this.getCreated() != null) {
            result.put(Created, this.getCreated() == null ? "" : this.getCreated());
        }
        if (!this.collection.isView() || this.getUpdated() != null) {
            result.put(Updated, this.getUpdated() == null ? "" : this.getUpdated());
        }

        // add helper collection reference fields
        result.put(CollectionId, TypeSafe.anyToString(this.collection.getId(), ""));
        result.put(CollectionName, TypeSafe.anyToString(this.collection.getName(), ""));

        // add expand (if set)
        if (expand != null && !this.expand.isEmpty()) {
            result.put(Expand, this.expand.getAll());
        }

        this.publicData = result;
        this.alreadyExported = true;
        return result;
    }

    public boolean emailVisibility() {
        return this.getBool(EmailVisibility);
    }

    public String username() {
        return this.getString(Username);
    }

    public String getString(String key) {
        return TypeSafe.anyToString(this.get(key));
    }

    public List<String> getStringList(String key) {
        return ListUtil.toUniqueStringList(this.get(key));
    }

    public boolean verified() {
        return this.getBool(Verified);
    }

    boolean getBool(String key) {
        return BooleanUtil.toBoolean(this.get(key).toString());
    }

    public String email() {
        return this.getString(Email);
    }

    /**
     * UnknownData returns a shallow copy ONLY of the unknown record fields data,
     * aka. fields that are neither one of the base and special system ones,
     * nor defined by the collection schema.
     */
    public Map<String, Object> unknownData() {
        if (this.data.isEmpty()) {
            return null;
        }
        return extractUnknownData(this.data.getAll());
    }

    private Map<String, Object> extractUnknownData(Map<String, Object> all) {
        Set<String> knownFields = new HashSet<>();
        knownFields.addAll(Arrays.asList(systemFieldNames));
        knownFields.addAll(Arrays.asList(baseModelFieldNames));
        this.collection.getSchema().getFields().stream()
                .map(SchemaField::getName)
                .forEach(knownFields::add);
        if (this.collection.isAuth()) {
            knownFields.addAll(Arrays.asList(authFieldNames));
        }

        Map<String, Object> result = new HashMap<>();
        all.forEach((k, v) -> {
            if (!knownFields.contains(k)) {
                result.put(k, v);
            }
        });
        return result;
    }

    public String baseFilesPath() {
        return String.format("%s/%s", this.collection.baseFilesPath(), this.getId());
    }

    /**
     * ReplaceModifers returns a new map with applied modifier
     * values based on the current record and the specified data.
     * <p>
     * The resolved modifier keys will be removed.
     * <p>
     * Multiple modifiers will be applied one after another,
     * while reusing the previous base key value result (.eg. 1; -5; +2 => -2).
     * <p>
     * Example usage:
     * <p>
     * newData := record.ReplaceModifers(data)
     * record:  {"field": 10}
     * data:    {"field+": 5}
     * newData: {"field": 15}
     */
    public Map<String, Object> replaceModifers(Map<String, Object> data) {
        Map<String, Object> clone = new HashMap<>(data);

        if (CollUtil.isEmpty(clone)) {
            return clone;
        }

        var ref = new Object() {
            Map<String, Object> recordDataCache = null;
        };
        // export recordData
        Supplier<Map<String, Object>> recordData = () -> {
            if (ref.recordDataCache == null) {
                ref.recordDataCache = this.schemaData();
            }
            return ref.recordDataCache;
        };


        List<String> modifiers = Schema.fieldValueModifiers();

        for (SchemaField field : collection.getSchema().getFields()) {
            String key = field.getName();

            for (String m : modifiers) {
                if (clone.containsKey(key + m)) {
                    var mv = clone.get(key + m);
                    if (!clone.containsKey(key)) {
                        // get base value from the merged data
                        clone.put(key, recordData.get().get(key));
                    }

                    clone.put(key, field.prepareValueWithModifier(clone.get(key), m, mv));
                    clone.remove(key + m);
                }
            }

            if (!Objects.equals(File, field.getType())) {
                continue;
            }

            // -----------------------------------------------------------
            // legacy file field modifiers (kept for backward compatibility)
            // -----------------------------------------------------------

            List<String> oldNames;
            if (clone.containsKey(key)) {
                oldNames = ListUtil.toUniqueStringList(clone.get(key));
            } else {
                // get oldNames from the model
                oldNames = ListUtil.toUniqueStringList(recordData.get().get(key));
            }

            // search for individual file name to delete (eg. "file.test.png = null")
            List<String> toDelete = new ArrayList<>();
            for (String name : oldNames) {
                String suffixedKey = key + "." + name;
                if (clone.containsKey(suffixedKey) && StrUtil.isEmptyIfStr(clone.get(suffixedKey))) {//包含key 且value 为 empty
                    //当前缀为 file.xxx = null ,表示集合中文件名为 xxx 的文件被清除。
                    toDelete.add(name);
                    clone.remove(suffixedKey);
                }
            }

            // search for individual file index to delete (eg. "file.0 = null")
            Pattern keyExp = Pattern.compile("^" + Pattern.quote(key) + "\\.\\d+$");
            for (String indexedKey : new HashSet<>(clone.keySet())) {
                if (keyExp.matcher(indexedKey).matches() && StrUtil.isEmptyIfStr(clone.get(indexedKey))) {
                    int index = Integer.parseInt(indexedKey.substring(key.length() + 1));
                    if (index < 0 || index >= oldNames.size()) {
                        continue;
                    }
                    toDelete.add(oldNames.get(index));
                    clone.remove(indexedKey);
                }
            }

            if (!toDelete.isEmpty()) {
                clone.put(key, field.prepareValue(ListUtil.subtractList(oldNames, toDelete)));
            }
        }

        return clone;
    }

    /**
     * SchemaData returns a shallow copy ONLY of the defined record schema fields data
     */
    public Map<String, Object> schemaData() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = this.data.getAll();

        for (SchemaField field : collection.getSchema().getFields()) {
            String fieldName = field.getName();
            Object fieldValue = data.get(fieldName);
            if (Objects.nonNull(fieldValue)) {
                result.put(fieldName, fieldValue);
            }
        }

        return result;
    }

    // SetPassword sets cryptographically secure string to the auth record "password" field.
    // This method also resets the "lastResetSentAt" and the "tokenKey" fields.
    //
    // Returns an error if the record is not from an auth collection or
    // an empty password is provided.
    public void setPassword(String password) {
        if (!collection.isAuth()) {
            throw new PbException(CODE_14000);
        }

        if ("".equals(password)) {
            throw new PbException(CODE_14001);
        }

        // hash the password
        String hashedPassword = BCrypt.hashpw(password);
        set(PasswordHash, hashedPassword);
        set(LastResetSentAt, null);

        // invalidate previously issued tokens
        refreshTokenKey();
    }

    public void refreshTokenKey() {
        setTokenKey(RandomUtil.randomString(50));
    }

    public void setTokenKey(String key) {
        if (!collection.isAuth()) {
            throw new PbException(CODE_14000);
        }

        set(TokenKey, key);
    }

    public SchemaField findFileFieldByFile(String filename) {
        for (SchemaField field : this.collection.getSchema().getFields()) {
            if (File.equals(field.getType())) {
                List<String> names = this.getStringList(field.getName());
                if (ListUtil.existInArray(filename, names.toArray(new String[0]))) {
                    return field;
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> columnValueMap() {
        var result = new HashMap<String, Object>(this.collection.getSchema().getFields().size() + 3);

        // export schema field values
        this.collection.getSchema().getFields().forEach(field -> result.put(field.getName(), this.getNormalizeDataValueForDB(field.getName())));

        // export auth collection fields
        if (this.collection.isAuth()) {
            Arrays.stream(authFieldNames).forEach(name -> result.put(name, this.getNormalizeDataValueForDB(name)));
        }

        // export base model fields;
        result.put(Id, this.getNormalizeDataValueForDB(Id));
        result.put(Created, this.getNormalizeDataValueForDB(Created));
        result.put(Updated, this.getNormalizeDataValueForDB(Updated));

        return result;
    }

    /**
     * @return return the "key" data value formatted for db storage.
     */
    private Object getNormalizeDataValueForDB(String key) {
        Object val;

        // normalize auth fields
        if (this.collection.isAuth()) {
            switch (key) {
                case EmailVisibility:
                case Verified:
                    return getBool(key);
                case LastResetSentAt:
                case LastVerificationSentAt:
                    return getDateTime(key);
                case Username:
                case Email:
                case TokenKey:
                case PasswordHash:
                    return getString(key);
            }
        }


        val = this.get(key);

        // TODO 考虑这里如果是map类型或者List类型的序列化在哪里做？ 这里或者数据库层面做一下处理
        if (val instanceof List || val instanceof Map) {
            val = PbManager.getPbJsonTemplate().toJsonString(val);
        }

        return val == null ? "" : val;
    }

    public LocalDateTime getDateTime(String key) {
        return (LocalDateTime) this.get(key);
    }


    public void withUnknownData(boolean b) {
        this.exportUnknown = b;
    }

    public void setExpand(Map<String, Object> expand) {

        if (this.expand == null) {
            this.expand = new Store<>();
        }
        this.expand.reset(expand);
    }

    /**
     * CleanCopy returns a copy of the current record model populated only
     * with its LATEST data state and everything else reset to the defaults.
     */
    public RecordModel cleanCopy() {
        var newRecord = new RecordModel(this.collection);
        newRecord.load(this.data.getAll());
        newRecord.setId(this.getId());
        newRecord.setCreated(this.getCreated());
        newRecord.setUpdated(this.getUpdated());
        return newRecord;
    }

    /**
     * returns a shallow copy of the current Record model expand data.
     */
    public Map<String, Object> expand() {
        if (this.expand == null) {
            this.expand = new Store<>();
        }
        return this.expand.getAll();
    }

    /**
     * MergeExpand merges recursively the provided expand data into
     * the current model's expand (if any).
     * <p>
     * Note that if an expanded prop with the same key is a slice (old or new expand)
     * then both old and new records will be merged into a new slice (aka. a :merge: [b,c] => [a,b,c]).
     * Otherwise the "old" expanded record will be replace with the "new" one (aka. a :merge: aNew => aNew).
     */
    public void mergeExpand(Map<String, Object> expand) {
        if (expand == null || expand.isEmpty()) {
            return;
        }

        // no old expand
        if (this.expand == null) {
            this.expand = new Store<>(expand);
            return;
        }

        var oldExpand = this.expand.getAll();

        for (String key : expand.keySet()) {
            var new_ = expand.get(key);
            if (!oldExpand.containsKey(key)) { // 不包含则放入
                oldExpand.put(key, new_);
                continue;
            }

            var old = oldExpand.get(key);
            boolean wasOldList = false;
            List<RecordModel> oldList = new ArrayList<>();
            if (old instanceof RecordModel v) {
                oldList.add(v);
            } else if (old instanceof List) {
                wasOldList = true;
                oldList = (List<RecordModel>) old;
            } else {
                // invalid old expand data -> assign directly the new
                // (no matter whether new is valid or not)
                oldExpand.put(key, new_);
                continue;
            }

            boolean wasNewList = false;
            List<RecordModel> newList = new ArrayList<>();
            if (new_ instanceof RecordModel v) {
                newList.add(v);
            } else if (new_ instanceof List) {
                wasNewList = true;
                newList = (List<RecordModel>) new_;
            } else {
                // invalid new expand data -> skip
                continue;
            }

            Map<String, RecordModel> oldIndexed = new HashMap<>(oldList.size());
            oldList.forEach(oldRecord -> oldIndexed.put(oldRecord.getId(), oldRecord));

            for (RecordModel newRecord : newList) {
                var oldRecord = oldIndexed.get(newRecord.getId());
                if (oldRecord != null) {
                    // note: there is no need to update oldSlice since oldRecord is a reference
                    oldRecord.mergeExpand(newRecord.expand());
                } else {
                    // missing new entry
                    oldList.add(newRecord);
                }
            }

            if (wasOldList || wasNewList || oldList.isEmpty()) {
                oldExpand.put(key, oldList);
            } else {
                oldExpand.put(key, oldList.get(0));
            }

        }
        this.expand.reset(oldExpand);
    }

    /**
     * PasswordHash returns the "passwordHash" auth record data value.
     */
    public String passwordHash() {
        return this.getString(PasswordHash);
    }

    public boolean validatePassword(String password) {

        if (!this.collection.isAuth()) {
            return false;
        }
        return BCrypt.checkpw(password, this.passwordHash());
    }
    

    @Override
    public String toString() {
        return "RecordModel{" +
                "collection=" + collection +
                ", exportUnknown=" + exportUnknown +
                ", ignoreEmailVisibility=" + ignoreEmailVisibility +
                ", loaded=" + loaded +
                ", originalData=" + originalData +
                ", expand=" + expand +
                ", data=" + data +
                '}';
    }

    public CollectionModel getCollection() {
        return collection;
    }

    public boolean isIgnoreEmailVisibility() {
        return ignoreEmailVisibility;
    }

    public RecordModel setIgnoreEmailVisibility(boolean ignoreEmailVisibility) {
        this.ignoreEmailVisibility = ignoreEmailVisibility;
        return this;
    }

    public boolean isAlreadyExported() {
        return alreadyExported;
    }

    public ConcurrentHashMap<String, Object> getPublicData() {
        return publicData;
    }

    public Map<String, Object> getOriginalData() {
        return originalData;
    }


    public static List<RecordModel> newRecordsFromNullStringMaps(CollectionModel collection, List<Map<String, Object>> rows) {//TODO 考虑移除
        List<RecordModel> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(newRecordFromNullStringMap(collection, row));
        }
        return result;
    }

    public static RecordModel newRecordFromNullStringMap(CollectionModel collection, Map<String, Object> data) {
        Map<String, Object> resultMap = new HashMap<>();

        // Load schema fields
        for (SchemaField field : collection.getSchema().getFields()) {
            resultMap.put(field.getName(), nullStringMapValue(data, field.getName()));
        }

        // Load base model fields
        for (String name : baseModelFieldNames) {
            resultMap.put(name, nullStringMapValue(data, name));
        }

        // Load auth fields
        if (collection.isAuth()) {
            for (String name : authFieldNames) {
                resultMap.put(name, nullStringMapValue(data, name));
            }
        }

        RecordModel record = new RecordModel(collection);
        record.load(resultMap);
        record.setNew(false);// 这里标记数据为非New


        return record;
    }

    public static Object nullStringMapValue(Map<String, Object> data, String key) {//TODO 考虑移除
        return data.get(key);
    }
}
