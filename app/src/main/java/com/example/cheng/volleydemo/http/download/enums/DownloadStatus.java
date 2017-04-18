/*
 * DownloadStatus    2017-04-08
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http.download.enums;

/**
 * 下载状态
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-08
 */
public enum DownloadStatus {
    waitting(0),

    starting(1),

    downloading(2),

    pause(3),

    finish(4),

    failed(5);

    private int value;

    private DownloadStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}