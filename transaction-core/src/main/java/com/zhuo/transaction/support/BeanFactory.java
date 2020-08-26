package com.zhuo.transaction.support;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/18
 */
public interface BeanFactory {
    <T> T getBean(Class<T> var1);

    <T> boolean isFactoryOf(Class<T> clazz);
}
