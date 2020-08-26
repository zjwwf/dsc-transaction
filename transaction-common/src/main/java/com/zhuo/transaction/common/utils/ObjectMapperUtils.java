package com.zhuo.transaction.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhuo.transaction.common.exception.JsonParseException;
import com.zhuo.transaction.common.exception.MapperException;

import java.util.Map;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
public class ObjectMapperUtils {

    private static final ObjectMapper DEFAULT_JSON_MAPPER = initDefaultJsonMapper();

    private static ObjectMapper initDefaultJsonMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        //当有未知属性不抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        //不序列化 null
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    public static <T> T parseJson(String context,Class<T> calassType){
        try {
            return DEFAULT_JSON_MAPPER.readValue(context,calassType);
        }catch (Exception e){
            throw new JsonParseException("json解析错误，msg:"+context);
        }
    }

    public static String toJsonString(Object obj){
        try {
            return DEFAULT_JSON_MAPPER.writeValueAsString(obj);
        }catch (Exception e){
            throw new MapperException(e);
        }
    }
}
