package com.zhuo.transaction;

import com.zhuo.transaction.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/29
 */
public class TccTransactionRunnable implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(TccTransactionRunnable.class);
    private String className;
    private int type;
    private String cancelMethod;
    private String confirmMethod;
    private Object[] args;

    public TccTransactionRunnable(String className,int type,String confirmMethod,String cancelMethod,Object[] args){
        this.className = className;
        this.type = type;
        this.cancelMethod = cancelMethod;
        this.confirmMethod = confirmMethod;
        this.args = args;
    }
    @Override
    public void run() {

        if(type == 1){
            excuteConfirmMethod();
        }else{
            excuteCancelMethod();
        }
    }

    private void excuteConfirmMethod() {
        try {
            CommonUtils.executeMethod(className,confirmMethod,args);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            try {
                CommonUtils.executeMethod(className,cancelMethod,args);
            }catch (Exception ex){
                logger.error(e.getMessage(),e);
                CommonUtils.errorHandle(confirmMethod,cancelMethod,args);
            }
        }
    }

    private void excuteCancelMethod() {
        try {
            CommonUtils.executeMethod(className,cancelMethod,args);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            CommonUtils.errorHandle(confirmMethod,cancelMethod,args);
        }
    }
}
