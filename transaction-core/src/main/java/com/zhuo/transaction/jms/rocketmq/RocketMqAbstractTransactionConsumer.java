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
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
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
    private String namesrvAddr;
    private String basePackage;
    private String zkServer;
    private String serviceName;

    @Override
    public void start() {
        if(StringUtils.isBlank(zkServer) || StringUtils.isBlank(serviceName)){
            throw new TransactionException("参与者没有配置zk地址");
        }
        try {
            zookeeperInit();
            String path = null;
            if(StringUtils.isNotBlank(this.basePackage)){
                path = ReflectionUtils.getClassPath("/" + basePackage);
            }else{
                path = ReflectionUtils.getClassPath();
            }
            List<String> classList = ReflectionUtils.getClassesList(path);
            for(String clazz : classList){
                List<String> linklist = ReflectionUtils.gethasAnnotationMethod(clazz, TcParticipant.class);
                for(String s : linklist){
                    ConsumerMethodInfoCache.put(s,s);
                }
            }
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
            consumer.registerMessageListener(new ConsumerMessageListener(System.currentTimeMillis()));
            // 07开启
            consumer.start();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }

    private void zookeeperInit(){
        if(ZookeeperUtils.zookeeper == null){
            ZookeeperUtils.init(zkServer);
        }
        if(!ZookeeperUtils.hasExist(Contants.BASE_ZOOKEEPER_DIR.substring(0,Contants.BASE_ZOOKEEPER_DIR.length()-1))){
            ZookeeperUtils.createNode(Contants.BASE_ZOOKEEPER_DIR.substring(0,Contants.BASE_ZOOKEEPER_DIR.length()-1),"",ZkNodeTypeEnum.zkNodeType_1.getCode());
        }
        if(!ZookeeperUtils.hasExist(Contants.BASE_ZOOKEEPER_SERVICE_DIR.substring(0,Contants.BASE_ZOOKEEPER_SERVICE_DIR.length()-1))){
            ZookeeperUtils.createNode(Contants.BASE_ZOOKEEPER_SERVICE_DIR.substring(0,Contants.BASE_ZOOKEEPER_SERVICE_DIR.length()-1),"",ZkNodeTypeEnum.zkNodeType_1.getCode());
        }
        ZookeeperUtils.createNode(Contants.BASE_ZOOKEEPER_SERVICE_DIR+serviceName,serviceName, ZkNodeTypeEnum.zkNodeType_3.getCode());
    }

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getZkServer() {
        return zkServer;
    }

    public void setZkServer(String zkServer) {
        this.zkServer = zkServer;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
