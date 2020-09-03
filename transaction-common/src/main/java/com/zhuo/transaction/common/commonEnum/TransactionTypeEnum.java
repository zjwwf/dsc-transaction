package com.zhuo.transaction.common.commonEnum;

/**
 * describe: 分布式事务方案类型枚举
 *
 * @author zhuojing
 * @date 2020/08/27
 */
public enum TransactionTypeEnum {
    /**
     * rocketmq
     */
    mq_rocketmq(1,"消息队列rocketmq实现方案"),
    mq_kafka(2,"消息队列kafka实现方案"),
    tcc(3,"tcc实现方案");

    private int code;
    private String description;

    private TransactionTypeEnum(int code, String description) {
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
