package com.zhuo.transaction.repository;

import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.UuidUtils;
import com.zhuo.transaction.serializer.KryoPoolSerializer;
import com.zhuo.transaction.serializer.ObjectSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.security.SecureRandom;
import java.util.*;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/08
 */
public class JedisClient {

    private static Logger logger = LoggerFactory.getLogger(JdbcTransactionRepository.class);
    private ObjectSerializer<Object> serializer = null;
    private String host;
    private Integer port;
    private String passWord;
    private Integer maxIdle = 10;
    private Integer maxTotal = 100;
    private Long maxWaitMillis = 3000L;
    private JedisPool pool = null;

    public JedisClient(String host){
        this.host = host;
    }
    public JedisClient(JedisPool jedisPool){
        this.pool = jedisPool;
    }
    public void init(){
        if(pool == null) {
            if (port != null && StringUtils.isNotBlank(passWord)) {
                pool = new JedisPool(getPoolConfig(), host, port, 5000, passWord);
            } else if (port != null && StringUtils.isBlank(passWord)) {
                pool = new JedisPool(getPoolConfig(), host, port);
            } else {
                pool = new JedisPool(getPoolConfig(), host);
            }
        }
        serializer = new KryoPoolSerializer();
    }
    public void destroy(){
        if(pool != null){
            pool.destroy();
        }
    }
    private JedisPoolConfig getPoolConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        return jedisPoolConfig;
    }

    public void hset(String key,String field,Object value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.hset(key.getBytes(),field.getBytes(),serializer.serialize(value));
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JedisClient hset error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }
    public void hmset(String key, Map<byte[],byte[]> value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.hmset(key.getBytes(),value);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            e.printStackTrace();
            throw new TransactionException("JedisClient hset error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }

    public void hincrBy(String key,String field,Long value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.hincrBy(key.getBytes(),field.getBytes(),value);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JedisClient hset error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }

    public Object hget(String key,String field){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            byte[] value = jedis.hget(key.getBytes(),field.getBytes());
            if(value == null){
                return null;
            }
            return serializer.deserialize(value);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JedisClient hget error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }
    public String hgetStr(String key,String field){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.hget(key,field);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JedisClient hgetStr error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }
    public void hmsetStr(String key, Map<String,String> value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.hmset(key,value);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            e.printStackTrace();
            throw new TransactionException("JedisClient hmsetStr error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }
    public void del(String key){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.del(key.getBytes());
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JedisClient set error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }


    public void zadd(String key,String member,double score){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.zadd(key,score,member);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JedisClient zadd error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }

    public List<String> zrevrange(String key,int start,int end){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Set<String> set = jedis.zrevrange(key, start, end);
            return new ArrayList<>(set);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return null;
        }finally {
            closeJedis(jedis);
        }
    }
    public List<String> zrange(String key,int start,int end){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Set<String> set = jedis.zrange(key, start, end);
            return new ArrayList<>(set);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return null;
        }finally {
            closeJedis(jedis);
        }
    }

    public void zrem(String key,String member){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.zrem(key, member);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JedisClient zrem error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }
    public long zcard(String key){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zcard(key);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JedisClient zrem error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }

    public void testZadd(){
        Jedis jedis = null;
        try {
            SecureRandom random = new SecureRandom();
            jedis = pool.getResource();
            for(int i = 0 ; i<1000000 ; i++){
                long st = System.currentTimeMillis();

                jedis.zadd("test",random.nextInt(99999999),UuidUtils.getId());
                System.out.println(System.currentTimeMillis()-st+"--"+i);
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }finally {
            closeJedis(jedis);
        }
    }

    private void closeJedis(Jedis jedis){
        if(jedis != null){
            try {
                jedis.close();
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public void setMaxWaitMillis(Long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public JedisPool getPool() {
        return pool;
    }

    public static void main(String[] args){
        JedisClient jedisClient = new JedisClient("127.0.0.1");
        jedisClient.init();
        Object status = jedisClient.hget("dsc-transaction-sz8l2vkniuzcqvltxxc02i5q", "initiator_success_num");
        System.out.println(status);
    }
}
