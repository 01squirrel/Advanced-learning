package com.example.network.exception;

import com.example.network.R;
import com.example.learnningproject.base.BaseBean;

public class ResponseException extends ExceptionHandler.ServerException{
    public ResponseException(int code,String msg) {
        this.setMsg(msg);
        this.setCode(code);
        switch (code){
            case BaseBean.CODE_LOGIN_OVERDUE:
                this.setMsgResId(R.string.login_overdue);
                break;
            case 10001://http请求方法不支持
            case 10002://json数据格式错误
            case 10003://请求的body缺失
            case 10004://请求的URL参数缺失
            case 10005://数据验证不通过
                this.setMsgResId(R.string.network_error);
            default:
                if(String.valueOf(code).length() == 3){
                    this.setMsgResId(R.string.network_error);
                }else {
                    this.setMsgResId(0);
                }
                break;
        }
    }
}
