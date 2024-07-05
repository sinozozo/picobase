package com.picobase.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 批量注解鉴权：只要满足其中一个注解即可通过验证
 *
 * <p> 可标注在方法、类上（效果等同于标注在此类的所有方法上）
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
