package com.zhuo.transaction.test;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;
import java.util.Properties;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/22
 */
public class kafkaProducerTest {

    public static void main(String[] args){
        Properties p = new Properties();
        //kafka地址，多个地址用逗号分割
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.91.133:9092");
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(p);
        String topic = "partopic";
        ProducerRecord<String,String> record = new ProducerRecord<String, String>(topic,"test");
        kafkaProducer.send(record);
        kafkaProducer.flush();
        ProducerRecord<String,String> record2 = new ProducerRecord<String, String>(topic,"test2");
        kafkaProducer.send(record2);

    }
}
