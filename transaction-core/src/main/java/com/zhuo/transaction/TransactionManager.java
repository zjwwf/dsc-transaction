package com.zhuo.transaction;

import com.zhuo.transaction.common.utils.UuidUtils;
import com.zhuo.transaction.context.TcServiceContext;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public class TransactionManager {

    private TcServiceContext tcServiceContext;


    public void setTcServiceContext(TcServiceContext tcServiceContext) {
        this.tcServiceContext = tcServiceContext;
    }

    public TcServiceContext getTcServiceContext() {
        return tcServiceContext;
    }

    public Object doMethod(){
        return tcServiceContext.proceed();
    }

    /**
     * 获取本次事务信息
     * @return todo
     */
    public Transaction getTransaction(){
        Transaction transaction = new Transaction();
        transaction.setBody(this.tcServiceContext.getParamInfo());
        transaction.setId(UuidUtils.getId());
        return transaction;
    }
}
