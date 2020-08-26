package com.zhuo.transaction.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * describe: 事务发起者注解
 *
 * @author zhuojing
 * @date 2020/08/18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TcInitiator {
    String paramMethod() default "";
}
