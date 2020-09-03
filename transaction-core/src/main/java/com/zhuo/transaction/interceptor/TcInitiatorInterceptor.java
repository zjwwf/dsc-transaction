package com.zhuo.transaction.interceptor;

import com.zhuo.transaction.InvocationContext;
import com.zhuo.transaction.Participant;
import com.zhuo.transaction.TransactionManager;
import com.zhuo.transaction.common.utils.ReflectionUtils;
import com.zhuo.transaction.context.TcInitiatorContext;
import com.zhuo.transaction.context.TcServiceContext;
import com.zhuo.transaction.jms.AbstractTransactionProducer;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public class TcInitiatorInterceptor {

    private TransactionManager tc = null;

    public void init(){
        this.tc = new TransactionManager();
    }

    public Object interceptCompensableMethod(ProceedingJoinPoint pjp, AbstractTransactionProducer transactionProducer) throws Throwable {
        TcServiceContext tcServiceContext = new TcServiceContext(pjp);
        Method method = tcServiceContext.getMethod();
        Class targetClass = ReflectionUtils.getDeclaringType(pjp.getTarget().getClass(), method.getName(), method.getParameterTypes());

        InvocationContext paramInfoInvocation = new InvocationContext(targetClass,method.getDeclaringClass(),
                tcServiceContext.getTcInitiator().paramMethod(),
                method.getParameterTypes(), pjp.getArgs());
        Participant participant  = new Participant(paramInfoInvocation);
        participant.buildParamInfo(tcServiceContext);
        this.tc.setTcServiceContext(tcServiceContext);
        transactionProducer.sendTcMsg(tc,tc.getTransaction());

        TcInitiatorContext tcInitiatorContext = new TcInitiatorContext();
        tcInitiatorContext.buildParamContent(tcServiceContext.getMethod(),tcServiceContext.getPjp());
        this.tc.setTcInitiatorContext(tcInitiatorContext);
        return null;
    }
}
