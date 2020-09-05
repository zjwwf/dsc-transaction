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
            int rc = transactionRepository.create(tc);
            if (rc == 0) {
                logger.error("create transaction table fail");
                throw new TransactionException("create transaction table fail");
            }
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
            Transaction oldtc = transactionRepository.getById(id);
            try {
                if (oldtc == null) {
                    logger.error("update transaction msg is empty");
                    throw new TransactionException("update transaction msg is empty");
                } else {
                    int rc = transactionRepository.updateStatus(id, statusCode);
                    if (rc == 0) {
                        logger.error("update transaction table fail");
                        throw new TransactionException("update transaction table fail");
                    }
                }
            } catch (Exception e) {
                logger.error("transaction table update fail," + e.getMessage(), e);
            }
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
                int rc = transactionRepository.addTryTime(id);
                if (rc == 0) {
                    logger.error("transaction msg add tryTime fail");
                    throw new TransactionException("transaction msg add tryTime fail");
                }
            } catch (Exception e) {
                logger.error("transaction table update fail," + e.getMessage(), e);
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

}
