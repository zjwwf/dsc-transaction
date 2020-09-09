package com.zhuo.transaction.repository;

import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.UuidUtils;
import com.zhuo.transaction.serializer.KryoPoolSerializer;
import com.zhuo.transaction.serializer.ObjectSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/08
 */
public class JedisClient {

    private static Logger logger = LoggerFactory.getLogger(JdbcTransactionRepository.class);
    private ObjectSerializer serializer = null;
    private String host;
    private Integer maxIdle = 10;
    private Integer maxTotal = 100;
    private Long maxWaitMillis = 3000L;
    private JedisPool pool = null;

    public void init(){
        pool =  new JedisPool(getPoolConfig(),host);
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

    public void set(String key,Object value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.set(key.getBytes(),serializer.serialize(value));
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JedisClient set error,"+e.getMessage());
        }finally {
            closeJedis(jedis);
        }
    }

    public Object get(String key){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            byte[] value = jedis.get(key.getBytes());
            if(value == null){
                return null;
            }
            return serializer.deserialize(value);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JedisClient get error,"+e.getMessage());
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

    public static void main(String[] args){
        JedisClient jedisClient = new JedisClient();
        jedisClient.setHost("127.0.0.1");
        jedisClient.init();
//        jedisClient.testZadd();
        List test = jedisClient.zrevrange("test", 0, 9);
        System.out.println(test.size());
    }
}
