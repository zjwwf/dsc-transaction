package com.zhuo.transaction.common.utils;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/22
 */
public class Contants {

    /**
     * zookeeper 事务参与者根目录路径
     */
    public final static String BASE_ZOOKEEPER_DIR = "/dsc-transaction/";
    /**
     * zookeeper 事务参与者节点路径
     */
    public final static String BASE_ZOOKEEPER_SERVICE_DIR = BASE_ZOOKEEPER_DIR+"service/";
    /**
     * rocketmq 消费者重试次数
     */
    public final static Integer CONSUMER_RECONSUMETIMES = 2;
}
