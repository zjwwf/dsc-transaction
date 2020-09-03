package com.zhuo.transaction.context;

import com.zhuo.transaction.api.TcInitiator;
import com.zhuo.transaction.common.exception.TransactionException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public class TcServiceContext {

    ProceedingJoinPoint pjp = null;
    Method method = null;
    TcInitiator tcInitiator;
    //调用方参数
    private String paramInfo;
    private String transactionId;

    public TcServiceContext(ProceedingJoinPoint pjp){
        this.pjp = pjp;
        this.method = getMethod();
        this.tcInitiator = method.getAnnotation(TcInitiator.class);
    }


    public Method getMethod() {
        Method method = ((MethodSignature) (pjp.getSignature())).getMethod();

        if (method.getAnnotation(TcInitiator.class) == null) {
            try {
                method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new TransactionException("系统出错");
            }
        }
        return method;
    }

    public ProceedingJoinPoint getPjp() {
        return pjp;
    }

    public TcInitiator getTcInitiator() {
        return tcInitiator;
    }

    public void setParamInfo(String paramInfo) {
        this.paramInfo = paramInfo;
    }

    public String getParamInfo() {
        return paramInfo;
    }

    public Object proceed() {
        try {
            return this.pjp.proceed();
        } catch (Throwable throwable) {
            throw  new TransactionException("方法调用出错");
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
