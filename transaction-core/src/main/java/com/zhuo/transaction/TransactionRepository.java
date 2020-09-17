package com.zhuo.transaction;

import java.util.List;

/**
 * describe: 事务消息结果存储接口
 *
 * @author zhuojing
 * @date 2020/08/27
 */
public interface TransactionRepository {

    /**
     * 初始化方法
     */
    void init();
    /**
     * 新增
     * @param transaction
     */
    void create(Transaction transaction);

    /**
     *  更新事务状态
     * @param transactionId
     * @param statusCode
     */
    void updateStatus(String transactionId,int statusCode);

    /**
     * 删除
     * @param transactionId
     */
    void delete(String transactionId);

    /**
     * 添加重试次数
     * @param transactionId
     */
    void addTryTime(String transactionId);

    /**
     * 根据id查找
     * @param transactionId
     * @return
     */
    Transaction getById(String transactionId);

    /**
     *  获取出现异常的事务消息（发送在一个小时之前）
     * @return
     */
    List<Transaction> getFailTranMsgList();

    /**
     *  获取已经完成事务消息
     * @return
     */
    List<Transaction> getSuccessTranMsgList();

    boolean exist(String transactionId);
}
