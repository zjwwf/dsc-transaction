package com.zhuo.transaction.jms.rocketmq;

import com.zhuo.transaction.cache.ProducerExecuteCache;
import com.zhuo.transaction.context.TcServiceContext;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/18
 */
public class TransactionListenerImpl implements TransactionListener {

    TransactionListenerImpl(){

    }

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        TcServiceContext serviceContext = (TcServiceContext) arg;
        try {
            if(ProducerExecuteCache.get(serviceContext.getTransactionId()) == null) {
                serviceContext.proceed();
                System.out.println("send success,msg:" + msg.getBody());
                ProducerExecuteCache.put(serviceContext.getTransactionId(),serviceContext.getTransactionId());
            }
            return LocalTransactionState.COMMIT_MESSAGE;
        }catch (Exception e){
            e.printStackTrace();
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
