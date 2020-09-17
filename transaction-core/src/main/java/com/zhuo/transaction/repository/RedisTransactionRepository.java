package com.zhuo.transaction.repository;

import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.DateUtils;
import com.zhuo.transaction.serializer.KryoPoolSerializer;
import com.zhuo.transaction.serializer.ObjectSerializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
    private ObjectSerializer<Object> serializer = new KryoPoolSerializer();

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
        Map<byte[],byte[]> value = new HashMap<>();
        if(StringUtils.isNotBlank(transaction.getId())) {
            value.put("id".getBytes(), serializer.serialize(transaction.getId()));
        }
        if(StringUtils.isNotBlank(transaction.getBody())) {
            value.put("body".getBytes(), serializer.serialize(transaction.getBody()));
        }
        if(transaction.getTryTime() != null) {
            value.put("tryTime".getBytes(), serializer.serialize(transaction.getTryTime()));
        }
        if(transaction.getStatus() != null) {
            value.put("status".getBytes(), serializer.serialize(transaction.getStatus()));
        }
        if(StringUtils.isNotBlank(transaction.getCancalMethod())) {
            value.put("cancalMethod".getBytes(), serializer.serialize(transaction.getCancalMethod()));
        }
        if(transaction.getCancalMethodParam() != null) {
            value.put("cancalMethodParam".getBytes(), serializer.serialize(transaction.getCancalMethodParam()));
        }
        if(StringUtils.isNotBlank(transaction.getConfirmMethod())) {
            value.put("confirmMethod".getBytes(), serializer.serialize(transaction.getConfirmMethod()));
        }
        if(transaction.getConfirmMethodParam() != null) {
            value.put("confirmMethodParam".getBytes(), serializer.serialize(transaction.getConfirmMethodParam()));
        }
        if(transaction.getTransactionType() != null) {
            value.put("transactionType".getBytes(), serializer.serialize(transaction.getTransactionType()));
        }
        if(transaction.getCreateTime() != null) {
            value.put("createTime".getBytes(), serializer.serialize(transaction.getCreateTime()));
        }
        if(transaction.getUpdateTime() != null) {
            value.put("updateTime".getBytes(), serializer.serialize(transaction.getUpdateTime()));
        }
        jedisClient.hmset(keyPrefix+transaction.getId(),value);
        if(transaction.getStatus()!= null && (transaction.getStatus() == TransactionMsgStatusEnum.code_3.getCode() || transaction.getStatus() == TransactionMsgStatusEnum.code_4.getCode())){
            jedisClient.zadd(errorKey,transaction.getId(),getScore());
        }
    }

    @Override
    protected void doUpdateStatus(String transactionId, int statusCode) {
        if(statusCode == TransactionMsgStatusEnum.code_3.getCode() || statusCode == TransactionMsgStatusEnum.code_4.getCode()){
            if(jedisClient.zcard(errorKey) > maxErrorNum){
                throw new TransactionException("redis transaction error num is greater than "+maxErrorNum);
            }
            jedisClient.zadd(errorKey,transactionId,getScore());
        }
        if(statusCode == TransactionMsgStatusEnum.code_2.getCode()){
            jedisClient.zrem(errorKey,transactionId);
        }
        Object o = jedisClient.hget(keyPrefix + transactionId,"id");
        if(o != null){
            jedisClient.hset(keyPrefix + transactionId,"status",statusCode);
            jedisClient.hset(keyPrefix + transactionId,"updateTime",new Date());
        }
    }

    @Override
    protected void doDelete(String transactionId) {
        jedisClient.del(keyPrefix + transactionId);
    }

    @Override
    protected void doAddTryTime(String transactionId) {
        Object o = jedisClient.hget(keyPrefix + transactionId,"id");
        if(o != null){
            Object tryTime = jedisClient.hget(keyPrefix + transactionId, "tryTime");
            if(tryTime != null && tryTime instanceof  Integer){
                jedisClient.hset(keyPrefix + transactionId, "tryTime",((Integer)tryTime)+1);
            }else{
                jedisClient.hset(keyPrefix + transactionId, "tryTime",1);
            }
            jedisClient.hset(keyPrefix + transactionId,"updateTime",new Date());
        }
    }

    @Override
    protected Transaction doGetById(String transactionId) {
        return buildTransaction(Arrays.asList("id","body","tryTime","status","cancalMethod","cancalMethodParam"
                ,"confirmMethod","confirmMethodParam","transactionType","createTime","updateTime")
                ,transactionId);
    }

    /**
     *  这边只查出定时任务需要的字段,跟mysql有所区别
     * @return
     */
    @Override
    protected List<Transaction> doGetFailTranMsgList() {
        List<String> ids = jedisClient.zrange(errorKey, 0, super.queryListNum - 1);
        List<Transaction> list = new ArrayList<>();
        if(ids != null && ids.size() > 0){
            for(String id : ids){
                Transaction transaction = buildTransaction(Arrays.asList("id","cancalMethod","cancalMethodParam","updateTime"),id);
                if(transaction != null){
                    list.add(transaction);
                }
            }
        }
        return list;
    }

    @Override
    protected boolean doexist(String transactionId) {
        Object o = jedisClient.hget(keyPrefix + transactionId,"id");
        if(o != null){
            return true;
        }
        return false;
    }

    private Transaction buildTransaction(List<String> columns,String transactionId){
        if(columns == null || columns.size() == 0){
            return null;
        }
        Transaction transaction = new Transaction();
        for(String column : columns){
            switch (column){
                case "id":
                    Object id = jedisClient.hget(keyPrefix + transactionId, "id");
                    if(id instanceof String){
                        transaction.setId((String) id);
                    }
                    break;
                case "body":
                    Object body = jedisClient.hget(keyPrefix + transactionId, "body");
                    if(body instanceof String){
                        transaction.setBody((String) body);
                    }
                    break;
                case "tryTime":
                    Object tryTime = jedisClient.hget(keyPrefix + transactionId, "tryTime");
                    if(tryTime instanceof Integer){
                        transaction.setTryTime((Integer) tryTime);
                    }
                    break;
                case "status":
                    Object status = jedisClient.hget(keyPrefix + transactionId, "status");
                    if(status instanceof Integer){
                        transaction.setStatus((Integer) status);
                    }
                    break;
                case "cancalMethod":
                    Object cancalMethod = jedisClient.hget(keyPrefix + transactionId, "cancalMethod");
                    if(cancalMethod instanceof String){
                        transaction.setCancalMethod((String) cancalMethod);
                    }
                    break;
                case "cancalMethodParam":
                    Object cancalMethodParam = jedisClient.hget(keyPrefix + transactionId, "cancalMethodParam");
                    if(cancalMethodParam instanceof Object[]){
                        transaction.setCancalMethodParam((Object[]) cancalMethodParam);
                    }
                    break;
                case "confirmMethod":
                    Object confirmMethod = jedisClient.hget(keyPrefix + transactionId, "confirmMethod");
                    if(confirmMethod instanceof String){
                        transaction.setConfirmMethod((String) confirmMethod);
                    }
                    break;
                case "confirmMethodParam":
                    Object confirmMethodParam = jedisClient.hget(keyPrefix + transactionId, "confirmMethodParam");
                    if(confirmMethodParam instanceof Object[]){
                        transaction.setConfirmMethodParam((Object[]) confirmMethodParam);
                    }
                    break;
                case "transactionType":
                    Object transactionType = jedisClient.hget(keyPrefix + transactionId, "transactionType");
                    if(transactionType instanceof Integer){
                        transaction.setTransactionType((Integer) transactionType);
                    }
                    break;
                case "createTime":
                    Object createTime = jedisClient.hget(keyPrefix + transactionId, "createTime");
                    if(createTime instanceof Date){
                        transaction.setCreateTime((Date) createTime);
                    }
                    break;
                case "updateTime":
                    Object updateTime = jedisClient.hget(keyPrefix + transactionId, "updateTime");
                    if(updateTime instanceof Date){
                        transaction.setUpdateTime((Date) updateTime);
                    }
                    break;
                default:break;
            }
        }
        return transaction;
    }

    @Override
    public void init() {

    }

    @Override
    public Integer getStatusById(String transactionId) {
        if(StringUtils.isBlank(transactionId)){
            return null;
        }
        Object status = jedisClient.hget(keyPrefix + transactionId, "status");
        if(status instanceof Integer){
            return (Integer)status;
        }
        return null;
    }

    private double getScore(){
        Date date = DateUtils.getDateForYYYYMMDDHHMMSS("2020-01-01 00:00:00");
        if(date == null){
            throw new TransactionException("RedisTransactionRepository getScore error");
        }
        return System.currentTimeMillis()-date.getTime();
    }
}
