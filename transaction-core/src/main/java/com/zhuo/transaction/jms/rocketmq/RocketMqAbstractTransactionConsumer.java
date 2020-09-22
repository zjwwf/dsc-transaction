package com.zhuo.transaction.jms.rocketmq;

import com.zhuo.transaction.api.TcParticipant;
import com.zhuo.transaction.cache.ConsumerMethodInfoCache;
import com.zhuo.transaction.common.commonEnum.ZkNodeTypeEnum;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.Contants;
import com.zhuo.transaction.common.utils.ReflectionUtils;
import com.zhuo.transaction.jms.AbstractTransactionConsumer;
import com.zhuo.transaction.utils.ZookeeperUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/20
 */
public class RocketMqAbstractTransactionConsumer extends AbstractTransactionConsumer {

    private static Logger logger = LoggerFactory.getLogger(RocketMqAbstractTransactionConsumer.class);

    public RocketMqAbstractTransactionConsumer(String namesrvAddr){
        this.namesrvAddr = namesrvAddr;
    }
    @Override
    public void start() {

        super.start();
        try {
            //启动mq消费者
            //01默认的消息消费FF者
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(super.GROUP_ID+"_"+serviceName);
            //广播模式
//            consumer.setMessageModel(MessageModel.BROADCASTING);
            //02注册
            consumer.setNamesrvAddr(namesrvAddr);
            //03设置获取原则
//            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            //04订阅
            consumer.subscribe(super.TOPIC+"_"+serviceName,"*");
            //05注册监听器
            consumer.registerMessageListener(new ConsumerMessageListener());
            // 07开启
            consumer.start();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }




}
