package com.picobase.persistence.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface PbRowMapper<T> {

    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
