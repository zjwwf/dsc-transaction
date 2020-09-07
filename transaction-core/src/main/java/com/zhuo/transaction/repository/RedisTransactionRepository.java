package com.zhuo.transaction.repository;

import com.zhuo.transaction.Transaction;

import java.util.List;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/05
 */
public class RedisTransactionRepository extends AbstractCachableTransactionRepositor {
    @Override
    protected int doCreate(Transaction transaction) {
        return 0;
    }

    @Override
    protected int doUpdateStatus(String transactionId, int statusCode) {
        return 0;
    }

    @Override
    protected int doDelete(String transactionId) {
        return 0;
    }

    @Override
    protected int doAddTryTime(String transactionId) {
        return 0;
    }

    @Override
    protected Transaction doGetById(String transactionId) {
        return null;
    }

    @Override
    protected List<Transaction> doGetFailTranMsgList() {
        return null;
    }

    @Override
    public void init() {

    }
}
