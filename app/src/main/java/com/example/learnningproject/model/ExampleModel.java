package com.example.learnningproject.model;

import com.example.learnningproject.api.ApiHolder;
import com.example.learnningproject.contract.ExampleContract;
import com.example.network.http.BaseBean;
import com.example.network.http.RxUtil;

import io.reactivex.rxjava3.core.Flowable;

public class ExampleModel implements ExampleContract.Model {
    @Override
    public Flowable<BaseBean<Object>> login(String uid) {
        return ApiHolder.getModuleApi().login(uid).compose(RxUtil.handleResult());
    }
}
