package com.zhuo.transaction.jms.rocketmq;

import com.zhuo.transaction.MqMsg;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.TransactionManager;
import com.zhuo.transaction.TransactionRepository;
import com.zhuo.transaction.cache.ParticipantServiceCache;
import com.zhuo.transaction.common.commonEnum.TransactionTypeEnum;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.Contants;
import com.zhuo.transaction.common.utils.ObjectMapperUtils;
import com.zhuo.transaction.jms.AbstractTransactionProducer;
import com.zhuo.transaction.utils.TransactionRepositoryUtils;
import com.zhuo.transaction.utils.ZookeeperUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public class RocketMqAbstractTransactionProducer extends AbstractTransactionProducer {


    private static Logger logger = LoggerFactory.getLogger(RocketMqAbstractTransactionConsumer.class);

    private String namesrvAddr;
    private TransactionListener transactionListener;
    private TransactionMQProducer producer;
    private ExecutorService executorService;
    private String zkServer = null;

    public RocketMqAbstractTransactionProducer(String namesrvAddr){
        this.namesrvAddr = namesrvAddr;
    }
    public void init() {
        if(StringUtils.isBlank(zkServer)){
            throw new TransactionException("参与者没有配置zk地址");
        }
        //zk
        if(ZookeeperUtils.zookeeper == null){
            ZookeeperUtils.init(zkServer);
        }
        ZookeeperUtils.registerChildrenWatcher(Contants.BASE_ZOOKEEPER_SERVICE_DIR.substring(1,Contants.BASE_ZOOKEEPER_SERVICE_DIR.length()-1));
        ZookeeperUtils.cacheParticipantService();
        transactionListener = new TransactionListenerImpl();
        List<String> children = ZookeeperUtils.getChildren(Contants.BASE_ZOOKEEPER_SERVICE_DIR.substring(0, Contants.BASE_ZOOKEEPER_SERVICE_DIR.length() - 1));
//        String groupId = children.stream().map(s -> super.GROUP_ID + "_" + s).collect(Collectors.joining("|"));
        producer = new TransactionMQProducer(super.GROUP_ID);
        producer.setSendMsgTimeout(super.sendMsgTime);
        producer.setNamesrvAddr(namesrvAddr);
        executorService = new ThreadPoolExecutor(5, 10, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("client-transaction-msg-check-thread");
                return thread;
            }
        });
        producer.setExecutorService(executorService);
        producer.setTransactionListener(transactionListener);
        try{
            producer.start();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }
    @Override
    public void sendTcMsg(TransactionManager tc, Transaction transaction) {
        //根据transaction 发送msg
        SendResult sendResult = null;
        try {
            String sendMsg = ObjectMapperUtils.toJsonString(transaction);
            MqMsg mqMsg = ObjectMapperUtils.parseJson(transaction.getBody(), MqMsg.class);
            List<String> participantServiceList = mqMsg.getParticipantService();
            //判断参与者是否启动
            if(!participantStartOrNot(participantServiceList)){
                //读取一次zookeeper，再次确认
                if(!participantStartOrNotAgain(participantServiceList)){
                    throw new TransactionException("参与者没有启动");
                }
            }
            //写入事务消息表
            transaction.setTransactionType(TransactionTypeEnum.mq_rocketmq.getCode());
            TransactionRepositoryUtils.create(transaction);
            tc.getTcServiceContext().setTransactionId(transaction.getId());
            for(String participantService : participantServiceList){
                Message msg = new Message(super.TOPIC+"_"+participantService,
                        sendMsg.getBytes(RemotingHelper.DEFAULT_CHARSET));
                producer.sendMessageInTransaction(msg, tc.getTcServiceContext());
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException(e.getMessage());
        }
    }

    /**
     *  取取zookeeper，判断参与者是否启动
     * @param participantServiceList
     * @return
     */
    private boolean participantStartOrNotAgain(List<String> participantServiceList) {
        ZookeeperUtils.cacheParticipantService();
        return participantStartOrNot(participantServiceList);
    }

    /**
     * 判断参与者是否启动
     * @param participantServiceList
     * @return
     */
    private boolean participantStartOrNot(List<String> participantServiceList){
        boolean r = true;
        if(participantServiceList == null || participantServiceList.size() == 0){
            r = false;
        }
        for(String p : participantServiceList){
            if(ParticipantServiceCache.get(p) == null){
                r = false;
                break;
            }
        }

        return r;
    }
    public void setZkServer(String zkServer) {
        this.zkServer = zkServer;
    }

    public void stop(){
        producer.shutdown();
    }
}
