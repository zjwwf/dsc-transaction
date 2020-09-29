package com.zhuo.transaction.jms;

import com.zhuo.transaction.api.TcParticipant;
import com.zhuo.transaction.cache.ConsumerMethodInfoCache;
import com.zhuo.transaction.common.commonEnum.ZkNodeTypeEnum;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.Contants;
import com.zhuo.transaction.common.utils.ReflectionUtils;
import com.zhuo.transaction.utils.ZookeeperUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/20
 */
public abstract class AbstractTransactionConsumer {

    private static Logger logger = LoggerFactory.getLogger(AbstractTransactionConsumer.class);
    protected final String GROUP_ID = "dsc_transaction_group_id";
    protected final String TOPIC = "dsc_transaction_topic_id";
    protected String namesrvAddr;
    protected String basePackage;
    protected String zkServer;
    protected String serviceName;
    /**
     * 启动消费者消费
     *
     */
    public void start(){
        if(StringUtils.isBlank(namesrvAddr)){
            throw new TransactionException("TransactionConsumer namesrvAddr is empty");
        }
        if(StringUtils.isBlank(zkServer) || StringUtils.isBlank(serviceName)){
            throw new TransactionException("TransactionConsumer zkServer is empty");
        }
        try {
            //添加zookeeper节点
            zookeeperInit();
            //扫描使用过事务参与者的类，换在缓存，若指定包则在指定包下扫描，否则扫描全部
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
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }
    private void zookeeperInit(){
        if(ZookeeperUtils.zookeeper == null){
            ZookeeperUtils.init(zkServer);
        }
        if(!ZookeeperUtils.hasExist(Contants.BASE_ZOOKEEPER_DIR.substring(0,Contants.BASE_ZOOKEEPER_DIR.length()-1))){
            ZookeeperUtils.createNode(Contants.BASE_ZOOKEEPER_DIR.substring(0,Contants.BASE_ZOOKEEPER_DIR.length()-1),"", ZkNodeTypeEnum.zkNodeType_1.getCode());
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
