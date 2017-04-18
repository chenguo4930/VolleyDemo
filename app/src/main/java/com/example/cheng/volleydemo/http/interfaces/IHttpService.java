/*
 * IHttpListener    2017-04-06
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http.interfaces;

import com.example.cheng.volleydemo.http.HttpMethod;

import java.util.Map;

/**
 * 获取网络
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-06
 */
public interface IHttpService {
    /**
     * 设置Url
     *
     * @param url
     */
    void setUrl(String url);

    /**
     * 执行网络请求
     */
    void excute();

    /**
     * 设置处理接口
     *
     * @param httpListener
     */
    void setHttpListener(IHttpListener httpListener);

    /**
     * 设置请求参数
     */
    void setRequestData(byte[] requestData);

    void pause();

    /**
     * 获取请求头的map
     *
     * @return
     */
    Map<String, String> getHttpHeadMap();


    boolean cancle();

    boolean isCancle();

    boolean isPause();

    void setMethod(HttpMethod value);
}