package com.zhuo.transaction.api;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/29
 */
public @interface TccTransaction {

    String cancalMethod() default "";
    String confirmMethod() default "";
    boolean async() default true;
}
