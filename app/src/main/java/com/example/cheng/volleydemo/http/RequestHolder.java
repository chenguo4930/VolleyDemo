/*
 * RequestHolder    2017-04-06
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http;

import com.example.cheng.volleydemo.http.interfaces.IHttpListener;
import com.example.cheng.volleydemo.http.interfaces.IHttpService;

/**
 * class description here
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-06
 */
public class RequestHolder<T> {
    /**
     * 执行下载类
     */
    private IHttpService httpService;
    /**
     * 获取数据 回调结果的类
     */
    private IHttpListener httpListener;
    /**
     * 请求参数对应的实体类
     */
    private T requestInfo;

    private String url;

    public IHttpService getHttpService() {
        return httpService;
    }

    public void setHttpService(IHttpService httpService) {
        this.httpService = httpService;
    }

    public IHttpListener getHttpListener() {
        return httpListener;
    }

    public void setHttpListener(IHttpListener httpListener) {
        this.httpListener = httpListener;
    }

    public T getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(T requestInfo) {
        this.requestInfo = requestInfo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}