package com.zhuo.transaction.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/22
 */
public class BeanFactory {

    private static ConcurrentHashMap<String,Object> cacheMap = new ConcurrentHashMap<>();


    public static void put(String key,Object value){
        cacheMap.put(key,value);
    }

    public static Object get(String key){
        return cacheMap.get(key);
    }

    public static Object get(Class clazz){
        Set<Map.Entry<String, Object>> entries = cacheMap.entrySet();
        for(Map.Entry<String, Object> entry : entries){
            if(entry.getClass() == clazz){
                return clazz;
            }
        }
        return null;
    }
}
