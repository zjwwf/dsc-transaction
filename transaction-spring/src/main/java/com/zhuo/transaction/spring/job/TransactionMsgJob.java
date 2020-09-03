package com.zhuo.transaction.spring.job;

import com.zhuo.transaction.MqMsg;
import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.common.utils.ObjectMapperUtils;
import com.zhuo.transaction.support.FactoryBuilder;
import com.zhuo.transaction.utils.TransactionRepositoryUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/02
 */
public class TransactionMsgJob {

    public void execute(){
        try {
            List<Transaction> list = TransactionRepositoryUtils.getFailTranMsgList();
            if(list != null && list.size() > 0){
                for(Transaction transaction : list){
                    //获取取消方法名
                    String cancalMethod = transaction.getCancalMethod();
                    //获取参数
                    Object[] cancalMethodParam = transaction.getCancalMethodParam();
                    //执行方法
                    int index = cancalMethod.lastIndexOf(".");
                    String className = cancalMethod.substring(0, index);
                    String methodName = cancalMethod.substring(index+1, cancalMethod.length());
                    Class clazz = Class.forName(className);
                    //尝试从spring中获取类实例
                    Object target = FactoryBuilder.factoryOf(clazz, clazz).getInstance();
                    Method[] methods = target.getClass().getMethods();
                    for(Method method : methods){
                        if(method.getName().equals(methodName)){
                            if(cancalMethodParam == null || cancalMethodParam.length == 0){
                                method.invoke(target);
                            }else{
                                method.invoke(target,cancalMethodParam);
                            }
                            break;
                        }
                    }
                }
            }
            System.out.println("TransactionMsgJob run...");
        }catch (Exception e){

        }

    }
}
