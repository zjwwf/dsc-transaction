package com.zhuo.transaction.common.exception;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public class MapperException extends RuntimeException {

    public MapperException(String msg){
        super(msg);
    }
    public MapperException(Throwable e){
        super(e);
    }
}
