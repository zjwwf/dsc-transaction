package com.zhuo.transaction.repository;

import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.TransactionRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/27
 */
public abstract class AbstractCachableTransactionRepositor implements TransactionRepository {

    @Override
    public int create(Transaction transaction) {
        if(transaction == null){
            return 0;
        }
        if(transaction.getCreateTime() == null){
            transaction.setCreateTime(new Date());
        }
        if(transaction.getUpdateTime() == null){
            transaction.setUpdateTime(new Date());
        }
        return doCreate(transaction);
    }

    @Override
    public int updateStatus(String transactionId,int statusCode){
        if(StringUtils.isBlank(transactionId)){
            return 0;
        }
        return doUpdateStatus(transactionId,statusCode);
    }

    @Override
    public int delete(String transactionId) {
        if(StringUtils.isBlank(transactionId)){
            return 0;
        }
        return doDelete(transactionId);
    }

    @Override
    public int addTryTime(String transactionId) {
        if(StringUtils.isBlank(transactionId)){
            return 0;
        }
        return doAddTryTime(transactionId);
    }

    @Override
    public Transaction getById(String transactionId) {
        if(StringUtils.isBlank(transactionId)){
            return null;
        }
        return doGetById(transactionId);
    }

    @Override
    public List<Transaction> getFailTranMsgList() {
        return doGetFailTranMsgList();
    }

    @Override
    public List<Transaction> getSuccessTranMsgList() {
        return null;
    }

    protected abstract int doCreate(Transaction transaction);

    protected abstract int doUpdateStatus(String transactionId,int statusCode);

    protected abstract int doDelete(String transactionId);

    protected abstract int doAddTryTime(String transactionId);

    protected abstract Transaction doGetById(String transactionId);

    protected abstract List<Transaction> doGetFailTranMsgList();

}
