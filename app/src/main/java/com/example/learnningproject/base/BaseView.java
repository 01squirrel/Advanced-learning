package com.example.learnningproject.base;


public interface BaseView {
    /*
    显示加载progress
     */
    void showLoadingProgress();
    /**
     * 隐藏加载progress
     */
    void dismissLoadingProgress();
    /**
     * 异常处理
     *
     * @param requestCode 请求码
     * @param e 异常
     */
   // default void onError(int requestCode, ExceptionHandler.ResponseThrowable e){

    //}
    /*
    Toast信息
     */
    //default void onToast(ExceptionHandler.ResponseThrowable e){}
    /**
     * 无数据处理
     */
    default void noDataProcess(){}
}
