package com.picobase.model;

import static com.picobase.util.PbConstants.TableName.EXTERNAL_AUTHS;

public class ExternalAuthModel extends BaseModel implements Model {

    private String collectionId;

    private String recordId;

    private String provider;

    private String providerId;


    public String getCollectionId() {
        return collectionId;
    }

    public ExternalAuthModel setCollectionId(String collectionId) {
        this.collectionId = collectionId;
        return this;
    }

    public String getRecordId() {
        return recordId;
    }

    public ExternalAuthModel setRecordId(String recordId) {
        this.recordId = recordId;
        return this;
    }

    public String getProvider() {
        return provider;
    }

    public ExternalAuthModel setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public String getProviderId() {
        return providerId;
    }

    public ExternalAuthModel setProviderId(String providerId) {
        this.providerId = providerId;
        return this;
    }

    @Override
    public String tableName() {
        return EXTERNAL_AUTHS;
    }
}
