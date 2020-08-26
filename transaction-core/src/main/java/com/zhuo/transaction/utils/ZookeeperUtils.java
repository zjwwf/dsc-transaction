package com.zhuo.transaction.utils;

import com.zhuo.transaction.cache.BeanFactory;
import com.zhuo.transaction.common.commonEnum.ZkNodeTypeEnum;
import com.zhuo.transaction.common.utils.Contants;
import com.zhuo.transaction.jms.rocketmq.RocketMqAbstractTransactionConsumer;
import com.zhuo.transaction.support.FactoryBuilder;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * describe: Zookeeper工具类
 *
 * @author zhuojing
 * @date 2019/09/12
 */
public class ZookeeperUtils {

    private static Logger LOG = LoggerFactory.getLogger(ZookeeperUtils.class);

    private static String zkHost;

    public static ZooKeeper zookeeper = null;

    public static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void connectZookeeper() throws Exception{
        if(zookeeper == null) {
            zookeeper = new ZooKeeper(zkHost, 50000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    System.out.println("-----");
                    System.out.println(watchedEvent.getState());
                    System.out.println("-----------");
                }
            });
        }
        registerChildrenWatcher(Contants.BASE_ZOOKEEPER_SERVICE_DIR.substring(1,Contants.BASE_ZOOKEEPER_SERVICE_DIR.length()-1));
//        countDownLatch.await();
        LOG.info("zookeeper connection success");
    }
    public static void registerChildrenWatcher(String path) {
        try {
            if(BeanFactory.get("participantServiceWatch") == null){
                BeanFactory.put("participantServiceWatch",new ParticipantServiceWatch());
            }
            zookeeper.getChildren(Contants.BASE_ZOOKEEPER_SERVICE_DIR.substring(0,Contants.BASE_ZOOKEEPER_SERVICE_DIR.length()-1),(Watcher) BeanFactory.get("participantServiceWatch"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void init(String zkHost) {
        try{
            ZookeeperUtils.zkHost = zkHost;

            connectZookeeper();
        }catch (KeeperException.SessionExpiredException e){
            LOG.error("会话超时");
            try{
                zookeeper.close();
                creatAgain();
            }catch (Exception er){
                LOG.error(e.getMessage(),e);
            }
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
            LOG.error("Zookeeper无法链接，Ip："+zkHost);
            LOG.error(e.getMessage(),e);
        }

    }

    /**
     * 创建节点
     * ZooDefs.Ids:
     * OPEN_ACL_UNSAFE  : 完全开放的ACL，任何连接的客户端都可以操作该属性znode
     * CREATOR_ALL_ACL : 只有创建者才有ACL权限
     * READ_ACL_UNSAFE：只能读取ACL
     * @param path
     * @param data
     * @param flag: 1:持久节点，2：持久顺序型，3：临时型，4：临时顺序型
     * @throws Exception
     */
    public static Boolean createNode(String path,String data,Integer flag){
        try{
            Boolean r = hasExist(path);
            if(r) {
                return false;
            }
            if(flag == null || flag.intValue() == ZkNodeTypeEnum.zkNodeType_3.getCode()) {
                zookeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            } else if (flag.intValue() == ZkNodeTypeEnum.zkNodeType_1.getCode()) {
                zookeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else if(flag.intValue() == ZkNodeTypeEnum.zkNodeType_2.getCode()) {
                zookeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
            } else {
                zookeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            }

        }catch (KeeperException.SessionExpiredException e){
            LOG.error("会话超时");
            try{
                zookeeper.close();
                creatAgain();
            }catch (Exception er){
                LOG.error(e.getMessage(),e);
            }
            e.printStackTrace();
        }catch (Exception e){
            LOG.error(e.getMessage(),e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 创建临时节点
     * @param path
     * @param data
     * @return
     */
    public static Boolean createNode(String path,String data){
        return createNode(path,data, ZkNodeTypeEnum.zkNodeType_3.getCode());
    }

    /**
     * 更新节点数据
     * @param path
     * @throws InterruptedException
     * @throws KeeperException
     */
    public static Boolean updateNode(String path,String data){
        try{
            zookeeper.setData(path,data.getBytes(),-1);
        }catch (KeeperException.SessionExpiredException e){
            LOG.error("会话超时");
            try{
                zookeeper.close();
                creatAgain();
                zookeeper.setData(path,data.getBytes(),-1);
            }catch (Exception er){
                LOG.error(e.getMessage(),e);
            }
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.getMessage(),e);
            return false;
        }
        return true;
    }
    /**
     * 删除节点
     * @param path
     * @throws InterruptedException
     * @throws KeeperException
     */
    public static Boolean deleteNode(String path){
        try{
            zookeeper.delete(path, -1);
        }catch (KeeperException.SessionExpiredException e){
            LOG.error("会话超时");
            try{
                zookeeper.close();
                creatAgain();
                zookeeper.delete(path, -1);
            }catch (Exception er){
                LOG.error(e.getMessage(),e);
            }
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.getMessage(),e);
            return false;
        }
        return true;
    }
    /**
     * 获取创建时间
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static String getCTime(String path) {
        try{
            Stat stat = zookeeper.exists(path, false);
            return String.valueOf(stat.getCtime());
        }catch (KeeperException.SessionExpiredException e){

            LOG.error("会话超时");
            try{
                zookeeper.close();
                creatAgain();
                Stat stat = zookeeper.exists(path, false);
                return String.valueOf(stat.getCtime());
            }catch (Exception er){
                LOG.error(e.getMessage(),e);
            }
            e.printStackTrace();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.getMessage(),e);
            return null;
        }
    }
    /**
     * 获取某个路径下孩子的数量
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static Integer getChildrenNum(String path){
        int childenNum = 0;
        try{
            childenNum = zookeeper.getChildren(path, false).size();
        }catch (KeeperException.SessionExpiredException e){
            LOG.error("会话超时");
            try{
                zookeeper.close();
                creatAgain();
                childenNum = zookeeper.getChildren(path, false).size();
            }catch (Exception er){
                LOG.error(e.getMessage(),e);
            }
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.getMessage(),e);
        }
        return childenNum;
    }
    public static List<String> getChildren(String path){
        List<String> list = new ArrayList<>();
        try{
            list = zookeeper.getChildren(path, false);
        }catch (KeeperException.SessionExpiredException e){
            LOG.error("会话超时");
            try{
                zookeeper.close();
                creatAgain();
                list = zookeeper.getChildren(path, false);
            }catch (Exception er){
                LOG.error(e.getMessage(),e);
            }
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.getMessage(),e);
        }
        return list;
    }
    /**
     * 获取某个路径下数据
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static String getData(String path, Watcher watcher){
        try{
            byte[] data = zookeeper.getData(path, watcher, null);
            return new String(data);
        }catch (KeeperException.SessionExpiredException e){
            LOG.error("会话超时");
            try{
                zookeeper.close();
                creatAgain();
                byte[] data = zookeeper.getData(path, watcher, null);
                return new String(data);
            }catch (Exception er){
                LOG.error(e.getMessage(),e);
            }
            e.printStackTrace();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.getMessage(),e);
            return null;
        }
    }

    /**
     * 判断节点是否存在
     * @param path
     * @return
     */
    public static Boolean hasExist(String path){
        Stat stat = null;
        try{
            stat = zookeeper.exists(path, true);
        }catch (KeeperException.SessionExpiredException e){
            LOG.error("会话超时");
            try{
                zookeeper.close();
                creatAgain();
                stat = zookeeper.exists(path, true);
            }catch (Exception er){
                LOG.error(e.getMessage(),e);
            }
            e.printStackTrace();
        }catch (Exception e){
            LOG.error(e.getMessage(),e);
            e.printStackTrace();
        }
        if(stat == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 重新建立由于会话超时消失的临时节点
     */
    private static void creatAgain(){
        try {
            zookeeper = null;
            connectZookeeper();
        }catch (Exception e){
            LOG.error(e.getMessage(),e);
        }
        Object target = FactoryBuilder.factoryOf(RocketMqAbstractTransactionConsumer.class, RocketMqAbstractTransactionConsumer.class).getInstance();
        if(target != null){
            RocketMqAbstractTransactionConsumer consumer = (RocketMqAbstractTransactionConsumer)target;
            System.out.println(consumer.getServiceName());
        }
//        String shardName = SystemUtils.getSystemEnv("SERARCH_INDEX");
//        ZookeeperUtils.createNode(Contant.PROJECT_EXITS_NODE,shardName,ZkNodeTypeEnum.zkNodeType_4.getCode());
    }
}