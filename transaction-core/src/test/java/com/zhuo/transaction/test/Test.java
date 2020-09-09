package com.zhuo.transaction.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.common.utils.ObjectMapperUtils;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public class Test {


    public static void jsonTest(){
        Transaction tc = new Transaction();
        tc.setBody("aaa");
        String str = ObjectMapperUtils.toJsonString(tc);
        System.out.println(str);
        Transaction transaction = ObjectMapperUtils.parseJson(str, Transaction.class);
        System.out.println(transaction.getBody());
    }
    public static void main(String[] args){
//        jsonTest();
    }
}
