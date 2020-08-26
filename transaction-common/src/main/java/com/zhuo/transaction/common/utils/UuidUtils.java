package com.zhuo.transaction.common.utils;

import java.security.SecureRandom;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/20
 */
public class UuidUtils {

    protected static final SecureRandom random = new SecureRandom();

    public static String getId(){
        String id = null;
        synchronized (random){
            long r0 = random.nextLong();
            if(r0 < 0){
                r0 = -r0;
            }
            long r1 = random.nextLong();
            if(r1 < 0){
                r1 = -r1;
            }
            id = Long.toString(r0, 36) + Long.toString(r1, 36);
        }
        return id;
    }
}
