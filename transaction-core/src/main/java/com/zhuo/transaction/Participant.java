package com.zhuo.transaction;

import com.zhuo.transaction.common.utils.ObjectMapperUtils;
import com.zhuo.transaction.context.TcServiceContext;

import java.io.Serializable;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/18
 */
public class Participant implements Serializable {

    private static final long serialVersionUID = 4127729421281425247L;

    private InvocationContext paramMethodInvocationContext;

    public Participant() {

    }

    public Participant(InvocationContext paramMethodInvocationContext) {
        this.paramMethodInvocationContext = paramMethodInvocationContext;
    }

    public void buildParamInfo(TcServiceContext tcServiceContext) {
        Object result = Terminator.invoke(paramMethodInvocationContext);
        if(result != null) {
            String paramInfo = ObjectMapperUtils.toJsonString(result);
            tcServiceContext.setParamInfo(paramInfo);
        }
    }

    public InvocationContext getParamMethodInvocationContext() {
        return paramMethodInvocationContext;
    }

    public void setParamMethodInvocationContext(InvocationContext paramMethodInvocationContext) {
        this.paramMethodInvocationContext = paramMethodInvocationContext;
    }
}
