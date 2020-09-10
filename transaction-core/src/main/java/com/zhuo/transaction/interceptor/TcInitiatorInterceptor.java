package com.zhuo.transaction.interceptor;

import com.zhuo.transaction.InvocationContext;
import com.zhuo.transaction.Participant;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.common.utils.ReflectionUtils;
import com.zhuo.transaction.common.utils.UuidUtils;
import com.zhuo.transaction.context.TcInitiatorContext;
import com.zhuo.transaction.context.TcServiceContext;
import com.zhuo.transaction.jms.AbstractTransactionProducer;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public class TcInitiatorInterceptor {

//    private TransactionManager tc = null;

//    public void init(){
////        this.tc = new TransactionManager();
//    }

    public Object interceptCompensableMethod(ProceedingJoinPoint pjp, AbstractTransactionProducer transactionProducer) throws Throwable {
//        if(tc == null){
//           throw new TransactionException("transactionManager is empty");
//        }
//        tc.clear();
        //构造TcServiceContext
        TcServiceContext tcServiceContext = new TcServiceContext(pjp);
        Method method = tcServiceContext.getMethod();
        Class targetClass = ReflectionUtils.getDeclaringType(pjp.getTarget().getClass(), method.getName(), method.getParameterTypes());
        InvocationContext paramInfoInvocation = new InvocationContext(targetClass,method.getDeclaringClass(),
                tcServiceContext.getTcInitiator().paramMethod(),
                method.getParameterTypes(), pjp.getArgs());
        Participant participant  = new Participant(paramInfoInvocation);
        participant.buildParamInfo(tcServiceContext);
//        this.tc.setTcServiceContext(tcServiceContext);
        //构造TcInitiatorContext
        TcInitiatorContext tcInitiatorContext = new TcInitiatorContext();
        tcInitiatorContext.buildParamContent(tcServiceContext.getMethod(),tcServiceContext.getPjp());
//        this.tc.setTcInitiatorContext(tcInitiatorContext);
        //发送消息
        transactionProducer.sendTcMsg(tcServiceContext,getTransaction(tcServiceContext,tcInitiatorContext));
        return null;
    }

    private Transaction getTransaction(TcServiceContext tcServiceContext,TcInitiatorContext tcInitiatorContext){
        Transaction transaction = new Transaction();
        transaction.setBody(tcServiceContext.getParamInfo());
        transaction.setId(UuidUtils.getId());
        transaction.setStatus(TransactionMsgStatusEnum.code_1.getCode());
        transaction.setCreateTime(new Date());
        if(tcInitiatorContext != null){
            transaction.setCancalMethod(tcInitiatorContext.getCancalMethod());
            transaction.setCancalMethodParam(tcInitiatorContext.getCancalMethodParam());
        }
        return transaction;
    }
}
