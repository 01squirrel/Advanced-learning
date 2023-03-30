package com.example.learnningproject.util;

import com.example.network.exception.ExceptionHandler;
import com.example.network.http.BaseBean;

import io.reactivex.rxjava3.subscribers.DisposableSubscriber;


public abstract class RXSubscribeUtil<T> extends DisposableSubscriber<BaseBean<T>> {

    @Override
    public void onNext(BaseBean<T> dataBean) {
        doOnNext(dataBean);
    }

    @Override
    public void onError(Throwable t) {
      doOnError(ExceptionHandler.handlerException(t));
      if(t instanceof ExceptionHandler.ServerException){
          ExceptionHandler.ServerException exception = (ExceptionHandler.ServerException) t;
          if(exception.getCode() >= 500) {
              exception.setCode(((ExceptionHandler.ServerException) t).getCode());
              exception.setMsg(((ExceptionHandler.ServerException) t).getMsg());
              throw exception;
          }
      }
    }

    @Override
    public void onComplete() {

    }

    protected abstract void doOnNext(BaseBean<T> bean);

    protected abstract void doOnError(ExceptionHandler.ResponseThrowable e);

}
