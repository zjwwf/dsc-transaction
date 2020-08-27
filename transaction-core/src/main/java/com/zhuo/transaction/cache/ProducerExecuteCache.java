package com.zhuo.transaction.cache;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * describe: 标记发送者已经执行的方法（参数者多个时，会发送多条消息，executeLocalTransaction会执行多次）
 *
 * @author zhuojing
 * @date 2020/08/26
 */
public class ProducerExecuteCache {
    private final static Integer MAP_NUM = 1000;
    private static AtomicInteger flag =new AtomicInteger(1);
    private final static ConcurrentHashMap<String,String> cacheMap1 = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String,String> cacheMap2 = new ConcurrentHashMap<>();

    public static void put(String key,String value){
        if(flag.get() == 1){
            if(cacheMap1.size() >= MAP_NUM / 2){
                cacheMap2.clear();
                cacheMap2.put(key,value);
                flag.set(2);
            }else{
                cacheMap1.put(key,value);
            }
        }else{
            if(cacheMap2.size() >= MAP_NUM / 2){
                cacheMap1.clear();
                cacheMap1.put(key,value);
                flag.set(1);
            }else{
                cacheMap2.put(key,value);
            }

        }
    }

    public static String get(String key){
        if(flag.get() == 1){
            String r = cacheMap1.get(key);
            if(StringUtils.isBlank(r)){
                r = cacheMap2.get(key);
            }
            return r;
        }else{
            String r = cacheMap2.get(key);
            if(StringUtils.isBlank(r)){
                r = cacheMap1.get(key);
            }
            return r;
        }
    }
}
