package com.picobase.console.mapper;

import com.picobase.model.AdminModel;
import com.picobase.persistence.mapper.AbstractMapper;
import com.picobase.persistence.repository.PbRowMapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

public abstract class AbstractBeanPropertyRowMapper<T> extends AbstractMapper<T> {

    public PbRowMapper<T> getPbRowMapper(){
        return (rs,rowNum)->{
            BeanPropertyRowMapper<T> mapper = new BeanPropertyRowMapper<>(getModelClass());
            return mapper.mapRow(rs, rowNum);
        };
    }

}
