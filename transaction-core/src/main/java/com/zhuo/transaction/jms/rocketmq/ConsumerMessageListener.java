package com.zhuo.transaction.jms.rocketmq;

import com.zhuo.transaction.MqMsg;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.cache.ConsumerMethodInfoCache;
import com.zhuo.transaction.cache.ConsumerMsgExecuteCache;
import com.zhuo.transaction.common.exception.JsonParseException;
import com.zhuo.transaction.common.utils.ObjectMapperUtils;
import com.zhuo.transaction.support.FactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/21
 */
public class ConsumerMessageListener implements MessageListenerConcurrently {

    private static Logger logger = LoggerFactory.getLogger(ConsumerMessageListener.class);
    private long st = 0L;
    public ConsumerMessageListener(){

    }
    public ConsumerMessageListener(long st){
        this.st = st;
    }
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {

        for(MessageExt msg : list){
            if(msg.getStoreTimestamp() < st){
                continue;
            }
            try {
                String msgBody = new String(msg.getBody(),"utf-8");
                Transaction transaction = ObjectMapperUtils.parseJson(msgBody, Transaction.class);
                //之前已经执行过，多条msg有可能其中一条已经成功执行，其他失败，进行消息的重发
                if (ConsumerMsgExecuteCache.get(transaction.getId()) != null){
                    continue;
                }
                MqMsg mqmsg = ObjectMapperUtils.parseJson(transaction.getBody(), MqMsg.class);
                for (Map.Entry<String, List<MqMsg.MethodParam>> entry : mqmsg.getMap().entrySet()){
                    String methodkey = entry.getKey();
                    List<MqMsg.MethodParam> value = entry.getValue();
                    executeMethod(methodkey,value);
                    ConsumerMsgExecuteCache.set(transaction.getId(),transaction.getId());
                }
            }catch (JsonParseException e){
                logger.error(e.getMessage(),e);
                continue;
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                //todo 失败一定次数进行处理，加入消息表获取其他操作
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    /**
     * 执行方法
     * @param methodkey
     * @param value
     * @throws Exception
     */
   private void executeMethod(String methodkey, List<MqMsg.MethodParam> value) throws Exception{
        //判断是否是次参与者的方法
       String methodInfo = ConsumerMethodInfoCache.get(methodkey);
       if(StringUtils.isNotBlank(methodInfo)){
           int index = methodInfo.lastIndexOf(".");
           String className = methodInfo.substring(0, index);
           String methodName = methodInfo.substring(index+1, methodInfo.length());
           System.out.println("executeMethod,"+className+"-->"+methodName);
           Class clazz = Class.forName(className);
           //尝试从spring中获取类实例
           Object target = FactoryBuilder.factoryOf(clazz, clazz).getInstance();
           //获取类的所有方法
           Method[] methods = clazz.getMethods();
           for(Method method : methods){
               if(method.getName().equals(methodName)){
                   if(value == null || value.size() == 0){
                       method.invoke(target);
                   }else{
                       //准备参数，执行方法
                       Object[] params  = new Object[value.size()];
                       for(int i = 0 ; i < value.size() ; i++){
                           MqMsg.MethodParam methodParam = value.get(i);
                           Object o = ObjectMapperUtils.parseJson(methodParam.getValue(), methodParam.getClazzType());
                           params[i] = o;
                       }
                       method.invoke(target,params);
                   }
                   break;
               }
           }
       }

   }
}
