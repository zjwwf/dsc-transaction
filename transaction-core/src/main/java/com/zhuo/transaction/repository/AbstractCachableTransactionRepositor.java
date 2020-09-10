package com.zhuo.transaction.repository;

import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.TransactionRepository;
import com.zhuo.transaction.common.exception.TransactionException;
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

    protected Integer queryListNum = 10;
    @Override
    public void create(Transaction transaction) {
        if(transaction == null){
            throw new TransactionException("TransactionRepository create transaction is empty");
        }
        if(transaction.getCreateTime() == null){
            transaction.setCreateTime(new Date());
        }
        if(transaction.getUpdateTime() == null){
            transaction.setUpdateTime(new Date());
        }
        doCreate(transaction);
    }

    @Override
    public void updateStatus(String transactionId,int statusCode){
        if(StringUtils.isBlank(transactionId)){
            throw new TransactionException("TransactionRepository updateStatus transactionId is empty");
        }
        doUpdateStatus(transactionId,statusCode);
    }

    @Override
    public void delete(String transactionId) {
        if(StringUtils.isBlank(transactionId)){
            throw new TransactionException("TransactionRepository delete transactionId is empty");
        }
        doDelete(transactionId);
    }

    @Override
    public void addTryTime(String transactionId) {
        if(StringUtils.isBlank(transactionId)){
            throw new TransactionException("TransactionRepository updateStatus addTryTime is empty");
        }
        doAddTryTime(transactionId);
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

    @Override
    public boolean exist(String transactionId) {
        if(StringUtils.isNotBlank(transactionId)){
            return doexist(transactionId);
        }
        return false;
    }

    protected abstract void doCreate(Transaction transaction);

    protected abstract void doUpdateStatus(String transactionId,int statusCode);

    protected abstract void doDelete(String transactionId);

    protected abstract void doAddTryTime(String transactionId);

    protected abstract Transaction doGetById(String transactionId);

    protected abstract List<Transaction> doGetFailTranMsgList();

    protected abstract boolean doexist(String transactionId);

    public void setQueryListNum(Integer queryListNum){
        this.queryListNum = queryListNum;
    }

}
