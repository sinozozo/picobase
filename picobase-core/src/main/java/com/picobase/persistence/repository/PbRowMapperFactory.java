package com.picobase.persistence.repository;

public interface PbRowMapperFactory {

    <T> PbRowMapper<T> getPbRowMapper(Class<T> modelClass);
}
