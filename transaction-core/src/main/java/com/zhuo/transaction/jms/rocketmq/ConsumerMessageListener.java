package com.zhuo.transaction.jms.rocketmq;

import com.zhuo.transaction.MqMsg;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.cache.ConsumerMethodInfoCache;
import com.zhuo.transaction.cache.ConsumerMsgExecuteCache;
import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.common.commonEnum.TransactionTypeEnum;
import com.zhuo.transaction.common.exception.JsonParseException;
import com.zhuo.transaction.common.utils.Contants;
import com.zhuo.transaction.common.utils.ObjectMapperUtils;
import com.zhuo.transaction.support.FactoryBuilder;
import com.zhuo.transaction.utils.CommonUtils;
import com.zhuo.transaction.utils.TransactionRepositoryUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/21
 */
public class ConsumerMessageListener implements MessageListenerConcurrently {

    private static Logger logger = LoggerFactory.getLogger(ConsumerMessageListener.class);
    private long st = 0L;
    public ConsumerMessageListener(){
        this.st = System.currentTimeMillis();
    }
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {

        for(MessageExt msg : list){
            if(msg.getStoreTimestamp() < st){
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
            String transactionId = null;
            try {
                String msgBody = new String(msg.getBody(),"utf-8");
                Transaction transaction = ObjectMapperUtils.parseJson(msgBody, Transaction.class);
                transactionId = transaction.getId();
                //之前已经执行过，多条msg有可能其中一条已经成功执行，其他失败，进行消息的重发
                if (ConsumerMsgExecuteCache.get(transaction.getId()) != null){
                    continue;
                }
                MqMsg mqmsg = ObjectMapperUtils.parseJson(transaction.getBody(), MqMsg.class);
                for (Map.Entry<String, List<MqMsg.MethodParam>> entry : mqmsg.getMap().entrySet()){
                    String methodkey = entry.getKey();
                    List<MqMsg.MethodParam> value = entry.getValue();
                    CommonUtils.executeMethod(methodkey,value);
                    ConsumerMsgExecuteCache.set(transaction.getId(),transaction.getId());
                }
//                Integer status = TransactionRepositoryUtils.getStatusById(transactionId);
//                if(status != null && status == TransactionMsgStatusEnum.code_4.getCode()){
//                    TransactionRepositoryUtils.updateStatus(transactionId, TransactionMsgStatusEnum.code_2.getCode());
//                }
                TransactionRepositoryUtils.addInitiatorSuccessNum(transactionId);
            }catch (JsonParseException e){
                logger.error(e.getMessage(),e);
                continue;
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                if(msg.getReconsumeTimes() == Contants.CONSUMER_RECONSUMETIMES){
                    TransactionRepositoryUtils.updateStatus(transactionId, TransactionMsgStatusEnum.code_3.getCode());
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }


}
