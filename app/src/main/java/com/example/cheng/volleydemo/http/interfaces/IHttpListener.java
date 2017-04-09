/*
 * IHttpListener    2017-04-06
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http.interfaces;

import org.apache.http.HttpEntity;

import java.util.Map;

/**
 * 处理结果
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-06
 */
public interface IHttpListener {
    /**
     * 网络访问框架
     * 处理结果 回调
     * @param httpEntity
     */
    void onSuccess(HttpEntity httpEntity);

    void onFail();

    void addHttpHeader(Map<String, String> headerMap);
}