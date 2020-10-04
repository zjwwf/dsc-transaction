package com.zhuo.transaction.utils;

import com.zhuo.transaction.MqMsg;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.cache.ConsumerMethodInfoCache;
import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.common.commonEnum.TransactionTypeEnum;
import com.zhuo.transaction.common.utils.ObjectMapperUtils;
import com.zhuo.transaction.common.utils.UuidUtils;
import com.zhuo.transaction.support.FactoryBuilder;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/17
 */
public class CommonUtils {


    /**
     * 反射执行方法
     * @param methodkey
     * @param value
     * @throws Exception
     */
    public static void executeMethod(String methodkey, List<MqMsg.MethodParam> value) throws Exception{
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

    public static void executeMethod(String className,String methodName,Object[] param) throws Exception{
        Class clazz = Class.forName(className);
        //尝试从spring中获取类实例
        Object target = FactoryBuilder.factoryOf(clazz, clazz).getInstance();
        Method[] methods = target.getClass().getMethods();
        for(Method method : methods){
            if(method.getName().equals(methodName)){
                if(param == null || param.length == 0){
                    method.invoke(target);
                }else{
                    method.invoke(target,param);
                }
                break;
            }
        }
    }

    /**
     *  出错记录消息表方法
     * @param confirmMethod
     * @param cancelMethod
     * @param args
     */
    public static void errorHandle(String confirmMethod,String cancelMethod,Object[] args){
        Transaction transaction = new Transaction();
        transaction.setId(UuidUtils.getId());
        transaction.setStatus(TransactionMsgStatusEnum.code_3.getCode());
        transaction.setUpdateTime(new Date());
        transaction.setCreateTime(new Date());
        transaction.setTransactionType(TransactionTypeEnum.tcc.getCode());
        transaction.setConfirmMethodParam(args);
        transaction.setConfirmMethod(confirmMethod);
        transaction.setCancalMethodParam(args);
        transaction.setCancalMethod(cancelMethod);
        TransactionRepositoryUtils.create(transaction);
    }
}
