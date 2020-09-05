package com.zhuo.transaction;

import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.common.commonEnum.TransactionTypeEnum;
import com.zhuo.transaction.common.utils.UuidUtils;
import com.zhuo.transaction.context.TcInitiatorContext;
import com.zhuo.transaction.context.TcServiceContext;

import java.util.Date;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public class TransactionManager {

    private TcServiceContext tcServiceContext;

    private TcInitiatorContext tcInitiatorContext;

    public Object doMethod(){
        return tcServiceContext.proceed();
    }

    /**
     * 获取本次事务信息
     * @return
     */
    public Transaction getTransaction(){
        Transaction transaction = new Transaction();
        transaction.setBody(this.tcServiceContext.getParamInfo());
        transaction.setId(UuidUtils.getId());
        transaction.setStatus(TransactionMsgStatusEnum.code_1.getCode());
        transaction.setCreateTime(new Date());
        if(this.tcInitiatorContext != null){
            transaction.setCancalMethod(this.tcInitiatorContext.getCancalMethod());
            transaction.setCancalMethodParam(this.tcInitiatorContext.getCancalMethodParam());
        }
        return transaction;
    }

    public void setTcServiceContext(TcServiceContext tcServiceContext) {
        this.tcServiceContext = tcServiceContext;
    }

    public TcServiceContext getTcServiceContext() {
        return tcServiceContext;
    }

    public TcInitiatorContext getTcInitiatorContext() {
        return tcInitiatorContext;
    }

    public void setTcInitiatorContext(TcInitiatorContext tcInitiatorContext) {
        this.tcInitiatorContext = tcInitiatorContext;
    }

    public void clear(){
        if(this.tcServiceContext != null){
            this.tcServiceContext = null;
        }
        if(this.tcInitiatorContext != null) {
            this.tcInitiatorContext = null;
        }
    }
}
