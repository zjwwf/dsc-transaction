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
        if(transaction.getStatus() != null) {
            value.put("status".getBytes(), serializer.serialize(transaction.getStatus()));
        }
        if(StringUtils.isNotBlank(transaction.getCancalMethod())) {
            value.put("cancal_method".getBytes(), serializer.serialize(transaction.getCancalMethod()));
        }
        if(transaction.getCancalMethodParam() != null) {
            value.put("cancal_method_param".getBytes(), serializer.serialize(transaction.getCancalMethodParam()));
        }
        if(StringUtils.isNotBlank(transaction.getConfirmMethod())) {
            value.put("confirm_method".getBytes(), serializer.serialize(transaction.getConfirmMethod()));
        }
        if(transaction.getConfirmMethodParam() != null) {
            value.put("confirm_method_param".getBytes(), serializer.serialize(transaction.getConfirmMethodParam()));
        }
        if(transaction.getTransactionType() != null) {
            value.put("transaction_type".getBytes(), serializer.serialize(transaction.getTransactionType()));
        }
        if(transaction.getCreateTime() != null) {
            value.put("create_time".getBytes(), serializer.serialize(transaction.getCreateTime()));
        }
        if(transaction.getUpdateTime() != null) {
            value.put("update_time".getBytes(), serializer.serialize(transaction.getUpdateTime()));
        }
        jedisClient.hmset(keyPrefix+transaction.getId(),value);
        Map<String,String> strvalue = new HashMap<>();
        strvalue.put("try_time", transaction.getTryTime() == null ? "0" : transaction.getTryTime().toString());
        strvalue.put("initiator_num", transaction.getInitiatorNum() == null ? "0" : transaction.getInitiatorNum().toString());
        strvalue.put("initiator_success_num", transaction.getInitiatorSuccessNum() == null ? "0":transaction.getInitiatorNum().toString());
        jedisClient.hmsetStr(keyPrefix+transaction.getId(),strvalue);
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
            jedisClient.hset(keyPrefix + transactionId,"update_time",new Date());
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
            jedisClient.hincrBy(keyPrefix + transactionId, "try_time",1L);
            jedisClient.hset(keyPrefix + transactionId,"update_time",new Date());
        }
    }

    @Override
    protected Transaction doGetById(String transactionId) {
        return buildTransaction(Arrays.asList("id","body","try_time","status","cancal_method","cancal_method_param"
                ,"confirm_method","confirm_method_param","transaction_type","initiator_num","initiator_success_num","create_time","update_time")
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
                Transaction transaction = buildTransaction(Arrays.asList("id","status","cancal_method","cancal_method_param","update_time","initiator_num","initiator_success_num"),id);
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

    @Override
    protected void doAddInitiatorSuccessNum(String transactionId) {
        Object o = jedisClient.hget(keyPrefix + transactionId,"id");
        if(o != null){
            jedisClient.hincrBy(keyPrefix + transactionId, "initiator_success_num",1L);
            jedisClient.hset(keyPrefix + transactionId,"update_time",new Date());
        }
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
                case "try_time":
                    String tryTime = jedisClient.hgetStr(keyPrefix + transactionId, "try_time");
                    if(StringUtils.isNumeric(tryTime)){
                        transaction.setTryTime(Integer.valueOf(tryTime));
                    }
                    break;
                case "status":
                    Object status = jedisClient.hget(keyPrefix + transactionId, "status");
                    if(status instanceof Integer){
                        transaction.setStatus((Integer) status);
                    }
                    break;
                case "cancal_method":
                    Object cancalMethod = jedisClient.hget(keyPrefix + transactionId, "cancal_method");
                    if(cancalMethod instanceof String){
                        transaction.setCancalMethod((String) cancalMethod);
                    }
                    break;
                case "cancal_method_param":
                    Object cancalMethodParam = jedisClient.hget(keyPrefix + transactionId, "cancal_method_param");
                    if(cancalMethodParam instanceof Object[]){
                        transaction.setCancalMethodParam((Object[]) cancalMethodParam);
                    }
                    break;
                case "confirm_method":
                    Object confirmMethod = jedisClient.hget(keyPrefix + transactionId, "confirm_method");
                    if(confirmMethod instanceof String){
                        transaction.setConfirmMethod((String) confirmMethod);
                    }
                    break;
                case "confirm_method_param":
                    Object confirmMethodParam = jedisClient.hget(keyPrefix + transactionId, "confirm_method_param");
                    if(confirmMethodParam instanceof Object[]){
                        transaction.setConfirmMethodParam((Object[]) confirmMethodParam);
                    }
                    break;
                case "transaction_type":
                    Object transactionType = jedisClient.hget(keyPrefix + transactionId, "transaction_type");
                    if(transactionType instanceof Integer){
                        transaction.setTransactionType((Integer) transactionType);
                    }
                    break;
                case "create_time":
                    Object createTime = jedisClient.hget(keyPrefix + transactionId, "create_time");
                    if(createTime instanceof Date){
                        transaction.setCreateTime((Date) createTime);
                    }
                    break;
                case "update_time":
                    Object updateTime = jedisClient.hget(keyPrefix + transactionId, "update_time");
                    if(updateTime instanceof Date){
                        transaction.setUpdateTime((Date) updateTime);
                    }
                    break;
                case "initiator_num":
                    String initiatorNum = jedisClient.hgetStr(keyPrefix + transactionId, "initiator_num");
                    if(StringUtils.isNumeric(initiatorNum)){
                        transaction.setInitiatorNum(Integer.valueOf(initiatorNum));
                    }
                     break;
                case "initiator_success_num":
                    String initiatorSuccessNum = jedisClient.hgetStr(keyPrefix + transactionId, "initiator_success_num");
                    if(StringUtils.isNumeric(initiatorSuccessNum)){
                        transaction.setInitiatorSuccessNum(Integer.valueOf(initiatorSuccessNum));
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
