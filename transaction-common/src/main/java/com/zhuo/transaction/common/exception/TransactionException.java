package com.zhuo.transaction.common.exception;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/20
 */
public class TransactionException extends RuntimeException {
    public TransactionException(String msg){
        super(msg);
    }
    public TransactionException(Throwable e){
        super(e);
    }
}
