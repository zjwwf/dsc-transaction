package com.zhuo.transaction.common.commonEnum;

/**
 * describe: 分布式事务消息状态
 *
 * @author zhuojing
 * @date 2020/08/27
 */
public enum TransactionMsgStatusEnum {
    /**
     *
     */
    code_1(1,"未结束"),
    code_2(2,"已结束"),
    code_3(3,"出现异常"),
    code_4(4,"已发送");


    private int code;
    private String description;

    private TransactionMsgStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ZkNodeTypeEnum getByCode(int code) {
        for (ZkNodeTypeEnum s : ZkNodeTypeEnum.values()) {
            if (s.getCode() == code) {
                return s;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }}
