package com.zhuo.transaction.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/20
 */
public class TcParticipantAspect {

    @Pointcut("@annotation(com.zhuo.transaction.api.TcParticipant)")
    public void TcParticipantAspect() {

    }

    @Around("TcServiceAspect()")
    public Object interceptCompensableMethod(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("interceptCompensableMethod");
        return pjp.proceed();
    }
}
