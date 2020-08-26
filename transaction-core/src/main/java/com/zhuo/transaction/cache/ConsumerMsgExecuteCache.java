package com.zhuo.transaction.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * describe: 缓存已经执行的msg
 *
 * @author zhuojing
 * @date 2020/08/21
 */
public class ConsumerMsgExecuteCache {

    private static ConcurrentHashMap<String, String> cacheMap = new ConcurrentHashMap<>();

    public static void set(String key,String value){
        cacheMap.put(key,value);
    }
    public static String get(String key){
        return cacheMap.get(key);
    }
}
