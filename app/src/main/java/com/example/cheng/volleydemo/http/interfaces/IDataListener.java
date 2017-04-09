/*
 * IHttpListener    2017-04-06
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http.interfaces;

/**
 * class description here
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-06
 */
public interface IDataListener<M> {

    /**
     * 回调结果给调用层
     * @param m
     */
    void onSuccess(M m);

    void onFail();
}