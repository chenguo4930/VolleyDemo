/*
 * JsonDealListener    2017-04-06
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.example.cheng.volleydemo.http.interfaces.IDataListener;
import com.example.cheng.volleydemo.http.interfaces.IHttpListener;

import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * 对应相应类
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-06
 */
public class JsonDealListener<T> implements IHttpListener {
    private Class<T> response;
    /**
     * 回调调用层 的接口
     */
    private IDataListener<T> dataListener;

    Handler handler = new Handler(Looper.getMainLooper());

    public JsonDealListener(Class<T> response, IDataListener<T> dataListener) {
        this.response = response;
        this.dataListener = dataListener;
    }

    @Override
    public void onSuccess(HttpEntity httpEntity) {
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
            //通过流转化为String,得到网络返回的数据, 子线程
            String content = getContent(inputStream);
            final T t = JSON.parseObject(content,response);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    dataListener.onSuccess(t);
                }
            });

        } catch (IOException e) {
            dataListener.onFail();
        }

    }

    @Override
    public void onFail() {

    }

    @Override
    public void addHttpHeader(Map<String, String> headerMap) {

    }

    private String getContent(InputStream inputStream) {
        String content = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                System.out.println("Error=" + e.toString());
                dataListener.onFail();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.out.println("Error=" + e.toString());
                }
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            dataListener.onFail();
        }
        return content;
    }
}