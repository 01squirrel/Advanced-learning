package com.example.learnningproject.presenter;

import com.example.learnningproject.contract.ExampleContract;
import com.example.learnningproject.model.ExampleModel;
import com.example.learnningproject.base.BasePresenter;
import com.example.network.exception.ExceptionHandler;
import com.example.network.http.BaseBean;
import com.example.learnningproject.util.RXSubscribeUtil;

public class ExamplePresenter extends BasePresenter<ExampleContract.View> implements ExampleContract.Presenter{

    private final ExampleModel model;
    public ExamplePresenter(ExampleContract.View view) {
        super(view);
        this.model = new ExampleModel();
    }

    @Override
    public void login(String uid) {
        if(!isBind()) return;
        getView().showLoadingProgress();
        compositeDisposable.add(model.login(uid).subscribeWith(new RXSubscribeUtil<Object>(){

            @Override
            protected void doOnNext(BaseBean<Object> bean) {
                //成功返回数据处理
            }

            @Override
            protected void doOnError(ExceptionHandler.ResponseThrowable e) {
                //错误异常处理
            }
        }));
    }
}
