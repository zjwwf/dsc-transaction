package com.zhuo.transaction.jms;

import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.cache.ParticipantServiceCache;
import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.Contants;
import com.zhuo.transaction.context.TcServiceContext;
import com.zhuo.transaction.utils.TransactionRepositoryUtils;
import com.zhuo.transaction.utils.ZookeeperUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public abstract class AbstractTransactionProducer {

    protected final Integer sendMsgTime = 5000;

    protected final String GROUP_ID = "dsc_transaction_group_id";
    protected final String TOPIC = "dsc_transaction_topic_id";
    private String zkServer = null;
    protected String namesrvAddr;
    /**
     * 发送消息
     * @param
     * @param transaction
     */
    public abstract void sendTcMsg(TcServiceContext tcServiceContext, Transaction transaction);

    public void init(){
        if(StringUtils.isBlank(namesrvAddr)){
            throw new TransactionException("TransactionProducer namesrvAddr is empty");
        }
        if(StringUtils.isBlank(zkServer)){
            throw new TransactionException("TransactionProducer zkServer is empty");
        }
        //zk
        if(ZookeeperUtils.zookeeper == null){
            ZookeeperUtils.init(zkServer);
        }
        ZookeeperUtils.registerChildrenWatcher(Contants.BASE_ZOOKEEPER_SERVICE_DIR.substring(1,Contants.BASE_ZOOKEEPER_SERVICE_DIR.length()-1));
        ZookeeperUtils.cacheParticipantService();
    }
    /**
     * 判断参与者是否启动
     * @param participantServiceList
     * @return
     */
    protected boolean participantStartOrNot(List<String> participantServiceList){
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

    protected void fail(Exception e,Transaction transaction){
        if(TransactionRepositoryUtils.exist(transaction.getId())){
            TransactionRepositoryUtils.updateStatus(transaction.getId(), TransactionMsgStatusEnum.code_3.getCode());
        }
        throw new TransactionException(e.getMessage());
    }
    public void setZkServer(String zkServer) {
        this.zkServer = zkServer;
    }


    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }
}
