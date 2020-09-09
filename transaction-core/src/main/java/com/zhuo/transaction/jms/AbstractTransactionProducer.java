package com.zhuo.transaction.jms;

import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.context.TcServiceContext;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public abstract class AbstractTransactionProducer {

    protected final Integer sendMsgTime = 5000;

    protected final String GROUP_ID = "dsc_transaction_group_id";
    protected final String TOPIC = "dsc_transaction_topic_id";
    /**
     * 发送消息
     * @param tc
     * @param transaction
     */
    public abstract void sendTcMsg(TcServiceContext tcServiceContext, Transaction transaction);

}
