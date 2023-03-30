package com.example.network.http;

import com.example.network.exception.ResponseException;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RxUtil {
    public RxUtil() {
    }

    public static <T> Flowable<BaseBean<T>> createData(final BaseBean<T> baseBean){
        @NonNull
        Flowable<BaseBean<T>> baseBeanFlowable = Flowable.create((FlowableOnSubscribe<BaseBean<T>>) emitter -> {
            try {
                emitter.onNext(baseBean);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        return baseBeanFlowable;
    }

    public static <T> FlowableTransformer<BaseBean<T>,BaseBean<T>> handleResult(){
        return upstream -> upstream.flatMap(
                (Function<BaseBean<T>, Publisher<BaseBean<T>>>) bean ->{
            if(bean.getCode() == BaseBean.CODE_OK){
                return createData(bean);
            } else {
                return Flowable.error(new ResponseException(bean.getCode(), bean.getMsg()));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> FlowableTransformer<BaseBean<T>,BaseBean<T>> handleResult(final int successCode){
        return upstream -> upstream.flatMap(
                (Function<BaseBean<T>,Publisher<BaseBean<T>>>) bean ->{
                    if (bean.getCode() == successCode) {
                        return createData(bean);
                    } else {
                        return Flowable.error(new ResponseException(bean.getCode(),bean.getMsg()));
                    }
                }
        ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
