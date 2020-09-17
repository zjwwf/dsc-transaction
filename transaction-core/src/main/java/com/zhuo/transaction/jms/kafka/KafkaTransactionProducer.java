package com.zhuo.transaction.jms.kafka;

import com.zhuo.transaction.MqMsg;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.common.commonEnum.TransactionTypeEnum;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.ObjectMapperUtils;
import com.zhuo.transaction.context.TcServiceContext;
import com.zhuo.transaction.jms.AbstractTransactionProducer;
import com.zhuo.transaction.utils.TransactionRepositoryUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * describe: kafka发生者
 *
 * @author zhuojing
 * @date 2020/09/17
 */
public class KafkaTransactionProducer extends AbstractTransactionProducer {

    private static Logger logger = LoggerFactory.getLogger(KafkaTransactionProducer.class);
    private String namesrvAddr;
    private Map<Object,Object> config = null;
    private KafkaProducer<String, String> kafkaProducer = null;

    @Override
    public void init(){
        super.init();
        if(StringUtils.isBlank(namesrvAddr)){
            throw new TransactionException("kafka servers is empty");
        }
        Properties p = new Properties();
        //kafka地址，多个地址用逗号分割
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, namesrvAddr);
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //自定义配置
        if(config != null && config.size() > 0){
            for(Map.Entry<Object,Object> entry : config.entrySet()){
                p.put(entry.getKey(),entry.getValue());
            }
        }
        kafkaProducer = new KafkaProducer<>(p);
    }


    @Override
    public void sendTcMsg(TcServiceContext tcServiceContext, Transaction transaction) {
        try {
            String sendMsg = ObjectMapperUtils.toJsonString(transaction);
            MqMsg mqMsg = ObjectMapperUtils.parseJson(transaction.getBody(), MqMsg.class);
            List<String> participantServiceList = mqMsg.getParticipantService();
            //判断参与者是否启动
            if(!participantStartOrNot(participantServiceList)){
                //读取一次zookeeper，再次确认
                if(!super.participantStartOrNot(participantServiceList)){
                    throw new TransactionException("TcParticipant service is not started");
                }
            }
            //执行事务方法
            tcServiceContext.proceed();
            //写入事务消息表
            transaction.setTransactionType(TransactionTypeEnum.mq_kafka.getCode());
            for(String participantService : participantServiceList){
                String topic = super.TOPIC+"_"+participantService;
                ProducerRecord<String,String> record = new ProducerRecord<String, String>(topic,sendMsg);
                kafkaProducer.send(record);
            }
            //发送消息
            transaction.setStatus(TransactionMsgStatusEnum.code_4.getCode());
            TransactionRepositoryUtils.create(transaction);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            fail(e,transaction);
        }
    }

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public Map<Object,Object> getConfig() {
        return config;
    }

    public void setConfig(Map<Object,Object> config) {
        this.config = config;
    }
}
