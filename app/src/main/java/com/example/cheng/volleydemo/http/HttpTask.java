/*
 * HttpTash    2017-04-06
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http;

import com.alibaba.fastjson.JSON;
import com.example.cheng.volleydemo.http.interfaces.IHttpListener;
import com.example.cheng.volleydemo.http.interfaces.IHttpService;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.FutureTask;

/**
 * class description here
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-06
 */
public class HttpTask<T> implements Runnable {

    private IHttpService httpService;
    private FutureTask futureTask;
    public HttpTask(RequestHolder<T> requestHolder) {
        httpService = requestHolder.getHttpService();
        httpService.setHttpListener(requestHolder.getHttpListener());
        httpService.setUrl(requestHolder.getUrl());

        //增加方法
        IHttpListener httpListener = requestHolder.getHttpListener();
        httpListener.addHttpHeader(httpService.getHttpHeadMap());
        try {
            T request = requestHolder.getRequestInfo();
            if (request != null) {
                String requestInfo = JSON.toJSONString(request);
                httpService.setRequestData(requestInfo.getBytes("UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        httpService.excute();
    }


    /**
     * 新增方法
     */
    public void start()
    {
        futureTask=new FutureTask(this,null);
        try {
            ThreadPoolManager.getInstance().execte(futureTask);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    /**
     * 新增方法
     */
    public  void pause()
    {
        httpService.pause();
        if(futureTask!=null)
        {
            ThreadPoolManager.getInstance().removeTask(futureTask);
        }

    }
}