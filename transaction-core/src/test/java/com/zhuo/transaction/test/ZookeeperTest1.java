package com.zhuo.transaction.test;

import com.zhuo.transaction.utils.ZookeeperUtils;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/24
 */
public class ZookeeperTest1 {

    public static void main(String args[]){
        ZookeeperUtils.init("127.0.0.1:2181");
        while (true){
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
