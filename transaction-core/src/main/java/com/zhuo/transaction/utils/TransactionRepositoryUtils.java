package com.zhuo.transaction.utils;

import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.TransactionRepository;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.support.FactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/31
 */
public class TransactionRepositoryUtils {

    private static Logger logger = LoggerFactory.getLogger(TransactionRepositoryUtils.class);

    /**
     * 新增
     * @param tc
     */
    public static void create(Transaction tc){
        if(checkRepository()) {
            if (tc == null || StringUtils.isBlank(tc.getId())) {
                logger.error("create transaction table fail，transaction is empty");
                throw new TransactionException("create transaction table fail，transaction is empty");
            }
            TransactionRepository transactionRepository = FactoryBuilder.getSpringSingeltonBean(TransactionRepository.class);
            transactionRepository.create(tc);
        }
    }
    /**
     * 事务表消息状态更新
     * @param id
     * @param statusCode
     */
    public static void updateStatus(String id,int statusCode){
        if(checkRepository()) {
            TransactionRepository transactionRepository = FactoryBuilder.getSpringSingeltonBean(TransactionRepository.class);
            if (StringUtils.isBlank(id)) {
                logger.error("update transaction table fail，transactionId is empty");
                throw new TransactionException("update transaction table fail，transactionId is empty");
            }
            transactionRepository.updateStatus(id, statusCode);
        }

    }

    /**
     *  新增重试次数
     * @param id
     */
    public static void addTryTime(String id){
        if(checkRepository()) {
            TransactionRepository transactionRepository = FactoryBuilder.getSpringSingeltonBean(TransactionRepository.class);
            if (StringUtils.isBlank(id)) {
                throw new TransactionException("transaction msg add tryTime fail，transactionId is empty");
            }
            try {
                transactionRepository.addTryTime(id);
            } catch (Exception e) {
                logger.error("transaction table tryTime fail," + e.getMessage(), e);
                throw new TransactionException("transaction msg add tryTime fail");
            }
        }
    }

    /**
     * 根据id查找
     * @param id
     * @return
     */
    public static Transaction getById(String id){
        if(checkRepository()) {
            TransactionRepository transactionRepository = FactoryBuilder.getSpringSingeltonBean(TransactionRepository.class);
            return transactionRepository.getById(id);
        }
        return null;
    }

    /**
     * 根据id查找
     * @param id
     * @return
     */
    public static boolean exist(String id){
        if(checkRepository()) {
            TransactionRepository transactionRepository = FactoryBuilder.getSpringSingeltonBean(TransactionRepository.class);
            return transactionRepository.exist(id);
        }
        return false;
    }

    private static boolean checkRepository(){
        TransactionRepository transactionRepository = FactoryBuilder.getSpringSingeltonBean(TransactionRepository.class);
        if(transactionRepository == null){
            logger.info("no configuration TransactionRepository");
            return false;
        }
        return true;
    }

    /**
     *  获取出现异常的事务消息（发送在一个小时之前）
     * @return
     */
    public static List<Transaction> getFailTranMsgList() {
        if(checkRepository()) {
            TransactionRepository transactionRepository = FactoryBuilder.getSpringSingeltonBean(TransactionRepository.class);
            return transactionRepository.getFailTranMsgList();
        }
        return null;
    }

    public static void delete(String id){
        if(checkRepository()) {
            TransactionRepository transactionRepository = FactoryBuilder.getSpringSingeltonBean(TransactionRepository.class);
            transactionRepository.delete(id);
        }
    }

    public static Integer getStatusById(String transactionId){
        if(checkRepository()) {
            TransactionRepository transactionRepository = FactoryBuilder.getSpringSingeltonBean(TransactionRepository.class);
            return transactionRepository.getStatusById(transactionId);
        }
        return null;
    }

}
