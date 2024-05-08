/*
 * Copyright 2020-2099 sa-token.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.picobase.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 批量注解鉴权：只要满足其中一个注解即可通过验证
 *
 * <p> 可标注在方法、类上（效果等同于标注在此类的所有方法上）
 *
 * @author click33
 * @since 1.35.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PbCheckOr {

    /**
     * 设定 @PbCheckLogin，参考 {@link PbCheckLogin}
     *
     * @return /
     */
    PbCheckLogin[] login() default {};

    /**
     * 设定 @PbCheckPermission，参考 {@link PbCheckPermission}
     *
     * @return /
     */
    PbCheckPermission[] permission() default {};

    /**
     * 设定 @PbCheckRole，参考 {@link PbCheckRole}
     *
     * @return /
     */
    PbCheckRole[] role() default {};

    /**
     * 设定 @PbCheckSafe，参考 {@link PbCheckSafe}
     *
     * @return /
     */
    PbCheckSafe[] safe() default {};

    /**
     * 设定 @PbCheckBasic，参考 {@link PbCheckBasic}
     *
     * @return /
     */
    PbCheckBasic[] basic() default {};

    /**
     * 设定 @PbCheckDisable，参考 {@link PbCheckDisable}
     *
     * @return /
     */
    PbCheckDisable[] disable() default {};

}
