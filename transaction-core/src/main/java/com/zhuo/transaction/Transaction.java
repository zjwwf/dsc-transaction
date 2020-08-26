package com.zhuo.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * describe:
 *
 * @author zhuojing
 * @date 2020/08/19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

    private String id;
    private String body;
    private Integer tryTime;
    private Integer status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getTryTime() {
        return tryTime;
    }

    public void setTryTime(Integer tryTime) {
        this.tryTime = tryTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
