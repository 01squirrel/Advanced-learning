package com.example.network.mvp;

import android.view.View;

public interface IPresenter<V extends IView>{
    /**
     * @param view 绑定view
     */
    void attachView(V view);

    /**
     *防止内存泄漏，接触presenter,activity绑定
     */
    void detachView();

    /**
     *
     * @return 获取view
     */
    IView getView();
}
