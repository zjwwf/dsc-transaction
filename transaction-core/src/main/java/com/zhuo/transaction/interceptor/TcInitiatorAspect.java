package com.zhuo.transaction.interceptor;

import com.zhuo.transaction.jms.AbstractTransactionProducer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/18
 */
@Aspect
public class TcInitiatorAspect {

    private TcInitiatorInterceptor tcInitiatorInterceptor;

    private AbstractTransactionProducer transactionProducer;

    public TcInitiatorInterceptor getTcInitiatorInterceptor() {
        return tcInitiatorInterceptor;
    }

    public void setTcInitiatorInterceptor(TcInitiatorInterceptor tcInitiatorInterceptor) {
        this.tcInitiatorInterceptor = tcInitiatorInterceptor;
    }

    public AbstractTransactionProducer getTransactionProducer() {
        return transactionProducer;
    }

    public void setTransactionProducer(AbstractTransactionProducer transactionProducer) {
        this.transactionProducer = transactionProducer;
    }

    @Pointcut("@annotation(com.zhuo.transaction.api.TcInitiator)")
    public void tcInitiatorPointcut() {

    }

    @Around("tcInitiatorPointcut()")
    public Object interceptMethod(ProceedingJoinPoint pjp) throws Throwable {
        return tcInitiatorInterceptor.interceptMethod(pjp,transactionProducer);
    }
}
