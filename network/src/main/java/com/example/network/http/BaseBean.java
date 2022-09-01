package com.example.network.http;

public class BaseBean<T> {
    //成功编码
    public static final int CODE_OK = 200;
    public static final int CODE_LOGIN_OVERDUE = 201002;
    //编码
    private int code;
    //消息
    private String msg;
    //数据实体
    private T data;
    //配置更新时 时间戳
    private long configUpdateTime;
    //服务器时间
    private long serverTime;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getConfigUpdateTime() {
        return configUpdateTime;
    }

    public void setConfigUpdateTime(long configUpdateTime) {
        this.configUpdateTime = configUpdateTime;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }
}
