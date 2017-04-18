/*
 * Volley    2017-04-06
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http;

import com.example.cheng.volleydemo.http.interfaces.IDataListener;
import com.example.cheng.volleydemo.http.interfaces.IHttpListener;
import com.example.cheng.volleydemo.http.interfaces.IHttpService;

import java.util.concurrent.FutureTask;

/**
 * class description here
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-06
 */
public class Volley {

    /**
     * 暴露给调用层
     *
     * @param <T> 请求参数类型
     * @param <M> 响应参数类型
     */
    public static <T, M> void sendRequest(T requestInfo, String url, HttpMethod method, Class<M> response, IDataListener dataListener) {
        RequestHolder<T> requestHolder = new RequestHolder<>();
        requestHolder.setRequestInfo(requestInfo);
        requestHolder.setUrl(url);
        requestHolder.setMethod(method);

        IHttpService httpService = new JsonHttpService();
        requestHolder.setHttpService(httpService);

        IHttpListener httpListener = new JsonDealListener<>(response, dataListener);
        requestHolder.setHttpListener(httpListener);
        HttpTask<T> httpTask = new HttpTask<>(requestHolder);
        try {
            ThreadPoolManager.getInstance().execte(new FutureTask<Object>(httpTask, null));
        } catch (InterruptedException e) {
            dataListener.onFail();
        }
    }
}