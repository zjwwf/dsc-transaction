package com.zhuo.transaction.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * describe: 参与者服务在线缓存
 *
 * @author zhuojing
 * @date 2020/08/24
 */
public class ParticipantServiceCache {
    private static ConcurrentHashMap<String, String> cacheMap = new ConcurrentHashMap<>();

    public static void set(String key,String value){
        cacheMap.put(key,value);
    }
    public static String get(String key){
        return cacheMap.get(key);
    }
    public static void clear(){
        cacheMap.clear();
    }

    public static List<String> values(){
        return new ArrayList<>(cacheMap.values());
    }

}
