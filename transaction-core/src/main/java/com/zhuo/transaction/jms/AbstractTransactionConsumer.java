package com.zhuo.transaction.jms;

import com.zhuo.transaction.TransactionManager;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/20
 */
public abstract class AbstractTransactionConsumer {

    protected final String GROUP_ID = "dsc_transaction_group_id";
    protected final String TOPIC = "dsc_transaction_topic_id";
    /**
     * 启动消费者消费
     *
     */
    public abstract void start();
}
