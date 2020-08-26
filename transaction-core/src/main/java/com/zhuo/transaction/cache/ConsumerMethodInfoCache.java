package com.zhuo.transaction.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * describe: 参与者执行方法全类名缓存
 *
 * @author zhuojing
 * @date 2020/08/21
 */
public class ConsumerMethodInfoCache {

    private final static Map<String,String> cacheMap = new HashMap<>();

    public static void put(String key,String value){
        cacheMap.put(key,value);
    }

    public static String get(String key){
        return cacheMap.get(key);
    }
}
