package com.zhuo.transaction.interceptor;

import com.zhuo.transaction.TccTransactionRunnable;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.api.TccTransaction;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.utils.CommonUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/29
 */
public class TccTransactionInterceptor {

    private static Logger logger = LoggerFactory.getLogger(TccTransactionInterceptor.class);
    public void interceptMethod(ProceedingJoinPoint pjp, ExecutorService executorService){
        String className = null;
        Object[] args = pjp.getArgs().clone();
        Method method = getMethod(pjp);
        TccTransaction tccTransAnn = method.getAnnotation(TccTransaction.class);
        String confirmMethod = tccTransAnn.confirmMethod();
        String cancalMethod =tccTransAnn.cancalMethod();
        try {
            className = method.getDeclaringClass().getName();
            //执行事务方法
            pjp.proceed();
            //执行confirmMethod 方法
            CommonUtils.executeMethod(className,confirmMethod,args);
        }catch (Throwable throwable) {
            logger.info(throwable.getMessage());
            try {
                if(tccTransAnn.async()){
                    //异步
                    executorService.execute(new TccTransactionRunnable(className,2,confirmMethod,cancalMethod,args));
                }else {
                    CommonUtils.executeMethod(className, cancalMethod, args);
                }
            }catch (Exception e){
                logger.info(e.getMessage(),e);
                //记录消息表
                CommonUtils.errorHandle(confirmMethod,cancalMethod,args);
            }
            throw new TransactionException(throwable.getMessage());
        }
    }

    public Method getMethod(ProceedingJoinPoint pjp) {
        Method method = ((MethodSignature) (pjp.getSignature())).getMethod();
        if (method.getAnnotation(TccTransaction.class) == null) {
            try {
                method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new TransactionException("系统出错");
            }
        }
        return method;
    }


}
