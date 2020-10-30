package com.zhuo.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.Random;

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
    private String cancalMethod;
    private Object[] cancalMethodParam;
    private String confirmMethod;
    private Object[] confirmMethodParam;
    private Integer transactionType;
    private Integer initiatorNum;
    private Integer initiatorSuccessNum;
    private Date createTime;
    private Date updateTime;

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

    public String getCancalMethod() {
        return cancalMethod;
    }

    public void setCancalMethod(String cancalMethod) {
        this.cancalMethod = cancalMethod;
    }

    public String getConfirmMethod() {
        return confirmMethod;
    }

    public void setConfirmMethod(String confirmMethod) {
        this.confirmMethod = confirmMethod;
    }

    public Integer getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Object[] getCancalMethodParam() {
        return cancalMethodParam;
    }

    public void setCancalMethodParam(Object[] cancalMethodParam) {
        this.cancalMethodParam = cancalMethodParam;
    }

    public Object[] getConfirmMethodParam() {
        return confirmMethodParam;
    }

    public void setConfirmMethodParam(Object[] confirmMethodParam) {
        this.confirmMethodParam = confirmMethodParam;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getInitiatorNum() {
        return initiatorNum;
    }

    public void setInitiatorNum(Integer initiatorNum) {

        this.initiatorNum = initiatorNum;
    }

    public Integer getInitiatorSuccessNum() {
        return initiatorSuccessNum;
    }

    public void setInitiatorSuccessNum(Integer initiatorSuccessNum) {
        this.initiatorSuccessNum = initiatorSuccessNum;
    }
}
