package com.zhuo.transaction.common.commonEnum;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * describe: zookeeper节点类型枚举类
 *
 * @author zhuojing
 * @date 2019/09/16
 */
public enum ZkNodeTypeEnum {
    /**
     * 持久节点枚举
     */
    zkNodeType_1(1,"持久节点"),
    /**
     * 持久节点枚举
     */
    zkNodeType_2(2, "持久顺序型"),
    zkNodeType_3(3, "临时型"),
    zkNodeType_4(4, "临时顺序型");

    private int code;
    private String description;

    private ZkNodeTypeEnum(int code, String description) {
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

    public static Map<String,Integer> getProhibitTypeMap(){
        Map<String,Integer> map = new HashMap<String,Integer>();
        for (ZkNodeTypeEnum s : ZkNodeTypeEnum.values()) {
            map.put(s.getDescription(), s.getCode());
        }
        return map;
    }

    public static Integer getCodeByDesc(String desc) {
        for (ZkNodeTypeEnum s : ZkNodeTypeEnum.values()) {
            if (StringUtils.equals(s.getDescription(), desc)) {
                return s.getCode();
            }
        }
        return null;
    }

    public static void checkCode(int code){
        ZkNodeTypeEnum zkNodeTypeEnum =   getByCode(code);
        if(zkNodeTypeEnum==null){
            throw new RuntimeException("请检查违禁词类型");
        }
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
    }
}
