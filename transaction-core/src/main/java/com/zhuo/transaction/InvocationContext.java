package com.zhuo.transaction;

import java.io.Serializable;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/18
 */
public class InvocationContext implements Serializable {

    private static final long serialVersionUID = -7969140711432461165L;
    private Class targetClass;
    private String methodName;
    private Class[] parameterTypes;
    private Object[] args;

    private Class realClass;

    public InvocationContext() {
    }

    public InvocationContext(Class targetClass, Class realClass,String methodName, Class[] parameterTypes, Object... args) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.targetClass = targetClass;
        this.args = args;
        this.realClass = realClass;
    }

    public Object[] getArgs() {
        return args;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public Class getRealClass() {
        return realClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }
}
