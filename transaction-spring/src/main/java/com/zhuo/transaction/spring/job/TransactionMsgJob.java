package com.zhuo.transaction.spring.job;

import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.TransactionRepository;
import com.zhuo.transaction.common.commonEnum.TransactionMsgStatusEnum;
import com.zhuo.transaction.support.FactoryBuilder;
import com.zhuo.transaction.utils.CommonUtils;
import com.zhuo.transaction.utils.TransactionRepositoryUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * describe: 执行事务失败的补偿方法定时任务
 *
 * @author zhuojing
 * @date 2020/09/02
 */
public class TransactionMsgJob {

    private static Logger logger = LoggerFactory.getLogger(TransactionMsgJob.class);
    private Integer bactchSize;
    public void execute(){
        try {
            List<Transaction> list = TransactionRepositoryUtils.getFailTranMsgList();
            if(list != null && list.size() > 0){
                for(Transaction transaction : list){
                    if(transaction.getInitiatorSuccessNum() >= transaction.getInitiatorNum() && TransactionMsgStatusEnum.code_4.getCode() == transaction.getStatus()){
                        TransactionRepositoryUtils.updateStatus(transaction.getId(),TransactionMsgStatusEnum.code_2.getCode());
                        continue;
                    }
                    //判断最新的更新时间在一个小时之前(redis存储时候需要加这个判断)
                    if(transaction.getUpdateTime() != null && System.currentTimeMillis() - transaction.getUpdateTime().getTime() < 1000*60*60){
                        continue;
                    }

                    //获取取消方法名
                    String cancalMethod = transaction.getCancalMethod();
                    //获取参数
                    Object[] cancalMethodParam = transaction.getCancalMethodParam();
                    //执行方法
                    int index = cancalMethod.lastIndexOf(".");
                    String className = cancalMethod.substring(0, index);
                    String methodName = cancalMethod.substring(index+1, cancalMethod.length());
                    try {
                        CommonUtils.executeMethod(className,methodName,cancalMethodParam);
                        TransactionRepositoryUtils.updateStatus(transaction.getId(), TransactionMsgStatusEnum.code_2.getCode());
                    }catch (Exception e){
                        logger.error("cancalMethod execute fail,id: {}",transaction.getId(),e);
                        TransactionRepositoryUtils.addTryTime(transaction.getId());
                        continue;
                    }

                }
            }
            System.out.println("TransactionMsgJob run...");
        }catch (Exception e){
            logger.error("cancalMethod execute fail",e);
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        TransactionMsgJob.logger = logger;
    }

    public Integer getBactchSize() {
        return bactchSize;
    }

    public void setBactchSize(Integer bactchSize) {
        this.bactchSize = bactchSize;
    }
}
