package com.zhuo.transaction.context;

import com.zhuo.transaction.api.TcInitiator;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/09/01
 */
public class TcInitiatorContext {

    private String cancalMethod;
    private Object[] cancalMethodParam;


    public String getCancalMethod() {
        return cancalMethod;
    }

    public void setCancalMethod(String cancalMethod) {
        this.cancalMethod = cancalMethod;
    }

    public Object[] getCancalMethodParam() {
        return cancalMethodParam;
    }

    public void setCancalMethodParam(Object[] cancalMethodParam) {
        this.cancalMethodParam = cancalMethodParam;
    }

    /**
     * 填充参数
     * @param method
     */
    public void buildParamContent(Method method, ProceedingJoinPoint pjp){
        if(method == null || pjp == null){
            return;
        }
        TcInitiator annotation = method.getAnnotation(TcInitiator.class);
        String methodName = annotation.cancalMethod();
        if(StringUtils.isBlank(methodName)){
            return;
        }
        String className = method.getDeclaringClass().getName();
        this.cancalMethod = className+"."+methodName;
        this.cancalMethodParam = pjp.getArgs();
        System.out.println(pjp.getArgs().length);
    }
}
