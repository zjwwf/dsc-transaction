package com.zhuo.transaction;

import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.ObjectMapperUtils;
import com.zhuo.transaction.context.TcServiceContext;
import com.zhuo.transaction.support.FactoryBuilder;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/18
 */
public final class Terminator {

    public Terminator() {

    }
    public static Object invoke(InvocationContext invocationContext) {
        if (StringUtils.isNotEmpty(invocationContext.getMethodName())) {
            try {
                Object target = FactoryBuilder.factoryOf(invocationContext.getTargetClass(),invocationContext.getRealClass()).getInstance();
                Method method = null;
                method = target.getClass().getMethod(invocationContext.getMethodName(), invocationContext.getParameterTypes());
                return method.invoke(target, invocationContext.getArgs());
            } catch (Exception e) {
                throw new TransactionException(e);
            }
        }
        return null;
    }
}
