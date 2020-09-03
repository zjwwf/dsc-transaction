package com.zhuo.transaction.jms.rocketmq;

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
        TcServiceContext serviceContext = (TcServiceContext) arg;
        try {
            if(ProducerExecuteCache.get(serviceContext.getTransactionId()) == null) {
                serviceContext.proceed();
                System.out.println("send success,msg:" + new String(msg.getBody()));
                ProducerExecuteCache.put(serviceContext.getTransactionId(),serviceContext.getTransactionId());
            }
            TransactionRepositoryUtils.updateStatus(serviceContext.getTransactionId(), TransactionMsgStatusEnum.code_2.getCode());
            return LocalTransactionState.COMMIT_MESSAGE;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            TransactionRepositoryUtils.updateStatus(serviceContext.getTransactionId(), TransactionMsgStatusEnum.code_3.getCode());
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    /**
     * 对于LocalTransactionState.UNKNOW 的重新检查,todo
     * @param msg
     * @return
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        //根据 msg.getTransactionId() 查询消息表，判断是否执行成功
//        Integer status = localTrans.get(msg.getTransactionId());
//        System.out.println("checkLocalTransaction"+msg.getTransactionId()+"-->"+status);
//        if (null != status) {
//            switch (status) {
//                case 0:
//                    return LocalTransactionState.UNKNOW;
//                case 1:
//                    return LocalTransactionState.COMMIT_MESSAGE;
//                case 2:
//                    return LocalTransactionState.ROLLBACK_MESSAGE;
//                 default:
//                    break;
//            }
//        }
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }
}
