package com.zhuo.transaction.test;

import com.zhuo.transaction.common.commonEnum.ZkNodeTypeEnum;
import com.zhuo.transaction.common.utils.Contants;
import com.zhuo.transaction.utils.ZookeeperUtils;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/24
 */
public class ZookeeperTest2 {

    public static void main(String args[]){
        ZookeeperUtils.init("127.0.0.1:2181");
        ZookeeperUtils.createNode(Contants.BASE_ZOOKEEPER_SERVICE_DIR+"test222","tests", ZkNodeTypeEnum.zkNodeType_1.getCode());
        while (true){
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
