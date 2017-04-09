/*
 * IDownListener    2017-04-08
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http.download.interfaces;

import com.example.cheng.volleydemo.http.interfaces.IHttpListener;
import com.example.cheng.volleydemo.http.interfaces.IHttpService;

/**
 * class description here
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-08
 */
public interface IDownListener extends IHttpListener{

    void setHttpservice(IHttpService httpservice);

    void setCancleCalle();

    void setPauseCallble();
}