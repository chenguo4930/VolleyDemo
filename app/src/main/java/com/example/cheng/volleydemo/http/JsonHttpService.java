package com.example.cheng.volleydemo.http;


import com.example.cheng.volleydemo.http.interfaces.IHttpListener;
import com.example.cheng.volleydemo.http.interfaces.IHttpService;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/13 0013.
 */

public class JsonHttpService implements IHttpService {
    private IHttpListener httpListener;

    private HttpClient httpClient = new DefaultHttpClient();
    private HttpPost httpPost;
    private HttpGet httpGet;
    private String url;
    private HttpMethod method;

    private byte[] requestData;
    /**
     * httpClient获取网络的回调
     */
    private HttpRespnceHandler httpRespnceHandler = new HttpRespnceHandler();

    @Override
    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public void excute() {
        if (method == HttpMethod.GET) {
            httpGet = new HttpGet(url);
            try {
                httpClient.execute(httpGet, httpRespnceHandler);
            } catch (IOException e) {
                httpListener.onFail();
            }
        } else if (method == HttpMethod.POST) {
            httpPost = new HttpPost(url);
            if (requestData != null && requestData.length > 0) {
                ByteArrayEntity byteArrayEntity = new ByteArrayEntity(requestData);
                httpPost.setEntity(byteArrayEntity);
            }
            try {
                httpClient.execute(httpPost, httpRespnceHandler);
            } catch (IOException e) {
                httpListener.onFail();
            }
        }

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

    }

    @Override
    public Map<String, String> getHttpHeadMap() {
        return null;
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
        return false;
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
            if (code == 200) {
                httpListener.onSuccess(response.getEntity());
            } else {
                httpListener.onFail();
            }

            return null;
        }
    }
}
