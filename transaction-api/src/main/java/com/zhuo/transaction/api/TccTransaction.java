package com.zhuo.transaction.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TccTransaction {

    String cancalMethod() default "";
    String confirmMethod() default "";
    boolean async() default true;
}
