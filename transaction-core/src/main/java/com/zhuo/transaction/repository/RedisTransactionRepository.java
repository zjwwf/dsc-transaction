package com.zhuo.transaction.repository;

import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * describe: 事务消息 redis存储
 *
 * @author zhuojing
 * @date 2020/09/05
 */
public class RedisTransactionRepository extends AbstractCachableTransactionRepositor {

    private static Logger logger = LoggerFactory.getLogger(JdbcTransactionRepository.class);
    /**
     * 允许存储出现异常事务消息数量
     */
    private long maxErrorNum = 2000000;
    private JedisClient jedisClient = null;
    private final static String keyPrefix = "dsc-transaction-";
    private final static String errorKey= "dsc-transaction-error";

    public RedisTransactionRepository(JedisClient jedisClient){
        this.jedisClient = jedisClient;
    }
    public RedisTransactionRepository(JedisClient jedisClient,Long maxErrorNum){
        this.maxErrorNum = maxErrorNum;
        this.jedisClient = jedisClient;
    }
    @Override
    protected void doCreate(Transaction transaction) {
        if(jedisClient.zcard(errorKey) > maxErrorNum){
            throw new TransactionException("redis transaction error num is greater than "+maxErrorNum);
        }
        jedisClient.set(keyPrefix+transaction.getId(),transaction);
        if(transaction.getStatus()!= null && transaction.getStatus().intValue() == TransactionMsgStatusEnum.code_3.getCode()){
            jedisClient.zadd(errorKey,transaction.getId(),getScore());
        }
    }

    @Override
    protected void doUpdateStatus(String transactionId, int statusCode) {
        if(statusCode == TransactionMsgStatusEnum.code_3.getCode()){
            if(jedisClient.zcard(errorKey) > maxErrorNum){
                throw new TransactionException("redis transaction error num is greater than "+maxErrorNum);
            }
            jedisClient.zadd(errorKey,transactionId,getScore());
        }
        if(statusCode == TransactionMsgStatusEnum.code_2.getCode()){
            jedisClient.zrem(errorKey,transactionId);
        }
        Object o = jedisClient.get(keyPrefix + transactionId);
        if(o instanceof  Transaction){
            Transaction transaction = (Transaction) o;
            transaction.setStatus(statusCode);
            transaction.setUpdateTime(new Date());
            jedisClient.set(keyPrefix + transactionId,transaction);
        }
    }

    @Override
    protected void doDelete(String transactionId) {
        jedisClient.del(keyPrefix + transactionId);
    }

    @Override
    protected void doAddTryTime(String transactionId) {
        Object o = jedisClient.get(keyPrefix + transactionId);
        if(o instanceof  Transaction){
            Transaction transaction = (Transaction) o;
            int num = 0;
            if(transaction.getTryTime() != null){
                num = transaction.getTryTime();
            }
            transaction.setTryTime(num+1);
            transaction.setUpdateTime(new Date());
            jedisClient.set(keyPrefix + transactionId,transaction);
        }
    }

    @Override
    protected Transaction doGetById(String transactionId) {
        Object o = jedisClient.get(keyPrefix + transactionId);
        if(o instanceof  Transaction){
            return (Transaction)o;
        }
        return null;
    }

    @Override
    protected List<Transaction> doGetFailTranMsgList() {
        List<String> ids = jedisClient.zrevrange(errorKey, 0, super.queryListNum - 1);
        List<Transaction> list = new ArrayList<>();
        if(ids != null && ids.size() > 0){
            for(String id : ids){
                Object o = jedisClient.get(keyPrefix + id);
                if(o instanceof  Transaction){
                    list.add((Transaction)o);
                }
            }
        }
        return list;
    }

    @Override
    public void init() {

    }

    private double getScore(){
        Date date = DateUtils.getDateForYYYYMMDDHHMMSS("2020-01-01 00:00:00");
        if(date == null){
            throw new TransactionException("RedisTransactionRepository getScore error");
        }
        return System.currentTimeMillis()-date.getTime();
    }
}
