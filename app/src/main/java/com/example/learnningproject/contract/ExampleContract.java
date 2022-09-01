package com.example.learnningproject.contract;

import com.example.network.http.BaseBean;
import com.example.learnningproject.base.BaseView;

import io.reactivex.rxjava3.core.Flowable;
/**
 * MVP契约类，定义接口方法
 */
public class ExampleContract {
    public interface View extends BaseView {
        void onLoginSuccess();
    }
    public interface Presenter{
        void login(String uid);
    }
    public interface Model{
        Flowable<BaseBean<Object>> login(String uid);
    }
}
