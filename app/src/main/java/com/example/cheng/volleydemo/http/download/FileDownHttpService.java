/*
 * FileDownHttpService    2017-04-08
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http.download;

import android.util.Log;

import com.example.cheng.volleydemo.http.HttpMethod;
import com.example.cheng.volleydemo.http.interfaces.IHttpListener;
import com.example.cheng.volleydemo.http.interfaces.IHttpService;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.synchronizedMap;

/**
 * class description here
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-08
 */
public class FileDownHttpService implements IHttpService {
    private static final String TAG = "FileDownHttpService";

    /**
     * 即将添加到请求头的信息
     */
    private Map<String, String> headerMap = synchronizedMap(new HashMap<String, String>());
    /**
     * 含有请求处理的接口
     */
    private IHttpListener httpListener;

    private HttpClient httpClient = new DefaultHttpClient();
    private HttpGet httpGet;
    private String url;
    private HttpMethod method;

    private byte[] requestData;
    /**
     * httpClient获取网络的回调
     */
    private HttpRespnceHandler httpRespnceHandler = new HttpRespnceHandler();
    /**
     * 增加方法,线程安全的，只允许一个线程访问
     */
    private AtomicBoolean pause = new AtomicBoolean(false);

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void excute() {
        //下载都是用get方式
        httpGet = new HttpGet(url);
        constrcuteHeader();

        try {
            httpClient.execute(httpGet, httpRespnceHandler);
        } catch (IOException e) {
            httpListener.onFail();
        }
    }

    /**
     * 请求头参数
     */
    private void constrcuteHeader() {
        Iterator iterator = headerMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = headerMap.get(key);
            Log.i(TAG, "请求头信息" + key + "  valuse=" + value);
            httpGet.addHeader(key, value);
        }
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    @Override
    public void setHttpListener(IHttpListener httpListener) {
        this.httpListener = httpListener;
    }

    @Override
    public void setRequestData(byte[] requestData) {
        this.requestData = requestData;
    }

    @Override
    public void pause() {
        pause.compareAndSet(false, true);
    }

    @Override
    public Map<String, String> getHttpHeadMap() {
        return headerMap;
    }

    @Override
    public boolean cancle() {
        return false;
    }

    @Override
    public boolean isCancle() {
        return false;
    }

    @Override
    public boolean isPause() {
        return pause.get();
    }

    @Override
    public void setMethod(HttpMethod value) {
        this.method = value;
    }

    private class HttpRespnceHandler extends BasicResponseHandler {
        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException {
            //响应吗
            int code = response.getStatusLine().getStatusCode();
            if (code == 200 || 206 == code) {
                httpListener.onSuccess(response.getEntity());
            } else {
                httpListener.onFail();
            }
            return null;
        }
    }
}