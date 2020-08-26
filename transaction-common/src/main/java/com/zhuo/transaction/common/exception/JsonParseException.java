package com.zhuo.transaction.common.exception;

/**
 * describe: json解析错误
 *
 * @author zhuojing
 * @date 2020/08/21
 */
public class JsonParseException extends RuntimeException {
    public JsonParseException(String msg){
        super(msg);
    }
    public JsonParseException(Throwable e){
        super(e);
    }
}
