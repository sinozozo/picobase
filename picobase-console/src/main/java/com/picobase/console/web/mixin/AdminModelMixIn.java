package com.picobase.console.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AdminModelMixIn {
    @JsonIgnore
    private String passwordHash;
}
