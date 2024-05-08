package com.picobase.fun;


import com.picobase.model.CollectionModel;

import java.util.Optional;

@FunctionalInterface
public interface PbCollFetchFun {
    Optional<CollectionModel> findByIdOrName(String idOrName);
}
