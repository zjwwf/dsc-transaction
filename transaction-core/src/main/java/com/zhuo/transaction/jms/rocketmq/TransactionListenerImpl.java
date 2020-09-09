package com.zhuo.transaction.jms.rocketmq;

import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.cache.ProducerExecuteCache;
import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.context.TcServiceContext;
import com.zhuo.transaction.utils.TransactionRepositoryUtils;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/18
 */
public class TransactionListenerImpl implements TransactionListener {

    private static Logger logger = LoggerFactory.getLogger(TransactionListenerImpl.class);

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        Transaction transaction = (Transaction) arg;
        try {
            TransactionRepositoryUtils.updateStatus(transaction.getId(), TransactionMsgStatusEnum.code_2.getCode());
            return LocalTransactionState.COMMIT_MESSAGE;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            TransactionRepositoryUtils.updateStatus(transaction.getId(), TransactionMsgStatusEnum.code_3.getCode());
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    /**
     * rocketmq 本地事务出现异常，删除mq消息
     * @param msg
     * @return
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }
}
