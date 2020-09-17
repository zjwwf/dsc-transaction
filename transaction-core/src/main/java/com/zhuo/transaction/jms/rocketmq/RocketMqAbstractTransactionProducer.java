package com.zhuo.transaction.jms.rocketmq;

import com.zhuo.transaction.MqMsg;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.common.commonEnum.TransactionTypeEnum;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.ObjectMapperUtils;
import com.zhuo.transaction.context.TcServiceContext;
import com.zhuo.transaction.jms.AbstractTransactionProducer;
import com.zhuo.transaction.utils.TransactionRepositoryUtils;
import com.zhuo.transaction.utils.ZookeeperUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * describe: rocketmq 发送者
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


    public RocketMqAbstractTransactionProducer(String namesrvAddr){
        this.namesrvAddr = namesrvAddr;
    }
    @Override
    public void init() {
        super.init();
        transactionListener = new TransactionListenerImpl();
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
    public void sendTcMsg(TcServiceContext tcServiceContext, Transaction transaction) {
        //根据transaction 发送msg
        SendResult sendResult = null;
        try {
            String sendMsg = ObjectMapperUtils.toJsonString(transaction);
            MqMsg mqMsg = ObjectMapperUtils.parseJson(transaction.getBody(), MqMsg.class);
            List<String> participantServiceList = mqMsg.getParticipantService();
            //判断参与者是否启动
            if(!super.participantStartOrNot(participantServiceList)){
                //读取一次zookeeper，再次确认
                if(!participantStartOrNotAgain(participantServiceList)){
                    throw new TransactionException("TcParticipant service is not started");
                }
            }
            //执行事务方法
//            if(ProducerExecuteCache.get(transaction.getId()) == null) {
            tcServiceContext.proceed();
//                ProducerExecuteCache.put(transaction.getId(),transaction.getId());
//            }
            //写入事务消息表
            transaction.setTransactionType(TransactionTypeEnum.mq_rocketmq.getCode());
            TransactionRepositoryUtils.create(transaction);
            for(String participantService : participantServiceList){
                Message msg = new Message(super.TOPIC+"_"+participantService,
                        sendMsg.getBytes(RemotingHelper.DEFAULT_CHARSET));
                producer.sendMessageInTransaction(msg, transaction);
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            fail(e,transaction);
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




    public void stop(){
        producer.shutdown();
    }
}
