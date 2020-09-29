package com.zhuo.transaction.test;

import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.zhuo.transaction.jms.kafka.ConsumerMessageThread;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/22
 */
public class kafkaConsumerTest {
    public static void main(String[] args){
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.91.133:9092");
//        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, "test_group_id");
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(p);
        // 订阅消息
        kafkaConsumer.subscribe(Collections.singletonList("partopic"));
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("111111111111");
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(500));
                    for (ConsumerRecord<String, String> record : records) {

                        System.out.println(record.value());
                    }
                }
            }
        });
        thread.start();
    }
}
