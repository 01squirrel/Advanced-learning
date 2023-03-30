package com.example.learnningproject.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.disposables.CompositeDisposable;


public abstract class BasePresenter<V extends BaseView> implements DefaultLifecycleObserver {
    //弱引用对象
    protected WeakReference<V> view;
    //rxjava调度器
    protected CompositeDisposable compositeDisposable;
    public BasePresenter(V view){
        bind(view);
    }
    /**
     * 绑定view
     * @param view 视图
     */
    public void bind(V view){
        if(compositeDisposable == null)
            compositeDisposable = new CompositeDisposable();
        this.view = new WeakReference<>(view);
    }
    /**
     * 获取view
     * @return  view
     */
    public V getView(){
        return view.get();
    }
    /**
     * 是否绑定
     */
    public boolean isBind(){
        return null != view && null != view.get();
    }

    /**
     * 解除绑定
     */
    public void unBind(){
        if(compositeDisposable != null){
            unSubscribe();
            compositeDisposable = null;
        }
        if(view != null){
            view.clear();
            view = null;
        }
    }
    /**
     * 清除订阅
     */
    public void unSubscribe(){
        if(compositeDisposable != null)
            compositeDisposable.clear();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        unBind();
    }

}
