package com.zhuo.transaction.jms.kafka;

import com.zhuo.transaction.MqMsg;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.cache.ConsumerMsgExecuteCache;
import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.common.utils.Contants;
import com.zhuo.transaction.common.utils.ObjectMapperUtils;
import com.zhuo.transaction.utils.CommonUtils;
import com.zhuo.transaction.utils.TransactionRepositoryUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/17
 */
public class ConsumerMessageThread implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(ConsumerMessageThread.class);
    private KafkaConsumer<String, String> kafkaConsumer = null;
    private long st = 0;

    public ConsumerMessageThread(KafkaConsumer<String, String> kafkaConsumer){
        this.kafkaConsumer = kafkaConsumer;
        st = System.currentTimeMillis();
    }
    @Override
    public void run() {
        while (true) {
            try {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
                if(records.count() == 0){
                    Thread.currentThread().sleep(500);
                }else{
                    for (ConsumerRecord<String, String> record : records) {

                        if(record.timestamp() < st){
                            continue;
                        }
                        String transactionId = null;
                        try {
                            Transaction transaction = ObjectMapperUtils.parseJson(record.value(), Transaction.class);
                            if (transaction == null) {
                                continue;
                            }
                            transactionId = transaction.getId();
                            //之前已经执行过，多条msg有可能其中一条已经成功执行，其他失败，进行消息的重发
                            if (ConsumerMsgExecuteCache.get(transaction.getId()) != null) {
                                continue;
                            }
                            MqMsg mqmsg = ObjectMapperUtils.parseJson(transaction.getBody(), MqMsg.class);
                            for (Map.Entry<String, List<MqMsg.MethodParam>> entry : mqmsg.getMap().entrySet()) {
                                String methodkey = entry.getKey();
                                List<MqMsg.MethodParam> value = entry.getValue();
                                CommonUtils.executeMethod(methodkey, value);
                                ConsumerMsgExecuteCache.set(transaction.getId(), transaction.getId());
                            }
                            Integer status = TransactionRepositoryUtils.getStatusById(transactionId);
                            if (status != null && status == TransactionMsgStatusEnum.code_4.getCode()) {
                                TransactionRepositoryUtils.updateStatus(transactionId, TransactionMsgStatusEnum.code_2.getCode());
                            }
                        }catch (Exception e){
                            logger.error(e.getMessage(),e);
                            TransactionRepositoryUtils.updateStatus(transactionId, TransactionMsgStatusEnum.code_3.getCode());
                        }
                    }
                }
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }

        }
    }
}
