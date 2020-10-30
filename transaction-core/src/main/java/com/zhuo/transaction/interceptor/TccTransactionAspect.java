package com.zhuo.transaction.interceptor;

import com.zhuo.transaction.api.TccTransaction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/29
 */
@Aspect
public class TccTransactionAspect {

    private TccTransactionInterceptor tccTransactionInterceptor;
    private ExecutorService executorService  = null;

    public TccTransactionAspect(int n){
        init(n);
    }
    public TccTransactionAspect(){
        init(10);
    }
    private void init(int n){
        this.tccTransactionInterceptor = new TccTransactionInterceptor();
        executorService = Executors.newFixedThreadPool(n);
    }

    @Pointcut("@annotation(com.zhuo.transaction.api.TccTransaction)")
    public  void tccTransactionPointcut(){ }

    @Around("tccTransactionPointcut()")
    public void interceptMethod(ProceedingJoinPoint pjp){

        tccTransactionInterceptor.interceptMethod(pjp,executorService);
    }
}
