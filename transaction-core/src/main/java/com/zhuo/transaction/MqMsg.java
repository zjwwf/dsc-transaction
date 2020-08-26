package com.zhuo.transaction;

import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * describe: mq消息Bean
 *
 * @author zhuojing
 * @date 2020/08/21
 */
public class MqMsg {

    private Map<String, List<MethodParam>> map = new HashedMap();
    private List<String> participantService;

    public List<MethodParam> get(String key){
        return map.get(key);
    }

    public void put(String key,List<MethodParam> value){
        map.put(key,value);
    }
    public void put(String key,MethodParam value){
        map.put(key,new ArrayList<>(Arrays.asList(value)));
    }

    public Map<String, List<MethodParam>> getMap() {
        return map;
    }

    public List<String> getParticipantService() {
        return participantService;
    }

    public void setParticipantService(List<String> participantService) {
        this.participantService = participantService;
    }

    public static class MethodParam{

        private String value;
        private Class clazzType;
        public MethodParam(){

        }
        public MethodParam(String value, Class clazzType) {
            this.value = value;
            this.clazzType = clazzType;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Class getClazzType() {
            return clazzType;
        }

        public void setClazzType(Class clazzType) {
            this.clazzType = clazzType;
        }
    }
}
