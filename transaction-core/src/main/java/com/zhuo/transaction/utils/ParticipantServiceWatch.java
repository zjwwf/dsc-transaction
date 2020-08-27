package com.zhuo.transaction.utils;

import com.zhuo.transaction.cache.ParticipantServiceCache;
import com.zhuo.transaction.common.utils.Contants;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * describe: zookeeper事务参与者节点监控
 *
 * @author zhuojing
 * @date 2019/09/16
 */
public class ParticipantServiceWatch implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        ZookeeperUtils.cacheParticipantService();
        ZookeeperUtils.registerChildrenWatcher(Contants.BASE_ZOOKEEPER_SERVICE_DIR.substring(1,Contants.BASE_ZOOKEEPER_SERVICE_DIR.length()-1));
    }
}
