package com.zhuo.transaction.jms.kafka;

import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.zhuo.transaction.jms.AbstractTransactionConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/17
 */
public class KafkaTransactionConsumer extends AbstractTransactionConsumer {

    private static Logger logger = LoggerFactory.getLogger(KafkaTransactionConsumer.class);
    private Map<Object,Object> config = null;
    @Override
    public void start() {
        super.start();
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, super.namesrvAddr);
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, super.GROUP_ID+"_"+serviceName);
        //自定义配置
        if(config != null && config.size() > 0){
            for(Map.Entry<Object,Object> entry : config.entrySet()){
                p.put(entry.getKey(),entry.getValue());
            }
        }
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(p);
        // 订阅消息
        kafkaConsumer.subscribe(Collections.singletonList(super.TOPIC+"_"+serviceName));
        ConsumerMessageThread consumerMessageThread = new ConsumerMessageThread(kafkaConsumer);
        Thread thread = new Thread(consumerMessageThread);
        thread.start();
    }

    public void setConfig(Map<Object, Object> config) {
        this.config = config;
    }
}
