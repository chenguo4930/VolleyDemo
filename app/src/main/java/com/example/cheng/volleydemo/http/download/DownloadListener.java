/*
 * DownloadListener    2017-04-08
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http.download;

import android.os.Handler;
import android.os.Looper;

import com.example.cheng.volleydemo.http.download.enums.DownloadStatus;
import com.example.cheng.volleydemo.http.download.interfaces.IDownListener;
import com.example.cheng.volleydemo.http.download.interfaces.IDownloadServiceCallable;
import com.example.cheng.volleydemo.http.interfaces.IHttpService;

import org.apache.http.HttpEntity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 下载
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-08
 */
public class DownloadListener implements IDownListener {

    private DownloadItemInfo downloadItemInfo;
    private File file;
    protected String url;
    private long breakPoint;
    private IDownloadServiceCallable downloadServiceCallable;

    private IHttpService httpService;

    /**
     * 得到主线程
     */
    private Handler handler = new Handler(Looper.getMainLooper());

    public DownloadListener(DownloadItemInfo downloadItemInfo,
                            IDownloadServiceCallable downloadServiceCallable,
                            IHttpService httpService) {
        this.downloadItemInfo = downloadItemInfo;
        this.downloadServiceCallable = downloadServiceCallable;
        this.httpService = httpService;
        this.file = new File(downloadItemInfo.getFilePath());
        /**
         * 得到已经下载的长度
         */
        this.breakPoint = file.length();
    }

    @Override
    public void addHttpHeader(Map<String, String> headerMap) {
        long length = getFile().length();
        if (length > 0L) {
            headerMap.put("RANGE", "bytes=" + length + "-");
        }
    }

    public DownloadListener(DownloadItemInfo downloadItemInfo) {
        this.downloadItemInfo = downloadItemInfo;
    }


    @Override
    public void setHttpservice(IHttpService httpservice) {
        this.httpService = httpservice;
    }

    /**
     * 设置取消接口
     */
    @Override
    public void setCancleCalle() {

    }

    @Override
    public void setPauseCallble() {
    }

    @Override
    public void onSuccess(HttpEntity httpEntity) {
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long startTime = System.currentTimeMillis();
        //用于计算每秒多少k
        long speed = 0L;
        //花费时间
        long useTime = 0L;
        //下载的长度
        long getLen = 0L;
        //接受的长度
        long receiveLen = 0L;
        boolean bufferLen = false;
        //得到下载的长度
        long dataLength = httpEntity.getContentLength();
        //单位时间下载的字节数
        long calcSpeedLen = 0L;
        //总数
        long totalLength = breakPoint + dataLength;
        //更新数量
        receviceTotalLength(totalLength);
        //更新状态
        downloadStatusChange(DownloadStatus.downloading);
        byte[] buffer = new byte[512];
        int count = 0;
        long currentTime = System.currentTimeMillis();
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;

        try {
            if (!makeDir(getFile().getParentFile())) {
                downloadServiceCallable.onDownloadError(downloadItemInfo, 1, "创建文件夹失败");
            } else {
                //true 表示追加
                fos = new FileOutputStream(getFile(), true);
                bos = new BufferedOutputStream(fos);
                int length = 1;
                while ((length = inputStream.read(buffer)) != -1) {
                    if (getHttpService().isCancle()) {
                        downloadServiceCallable.onDownloadError(downloadItemInfo, 1, "用户取消了");
                        return;
                    }

                    if (getHttpService().isPause()) {
                        downloadServiceCallable.onDownloadError(downloadItemInfo, 2, "用户暂停了");
                        return;
                    }
                    bos.write(buffer, 0, length);
                    getLen += (long) length;
                    receiveLen += (long) length;
                    calcSpeedLen += (long) length;
                    ++count;
                    if (receiveLen * 10L / totalLength >= 1L || count >= 5000) {
                        currentTime = System.currentTimeMillis();
                        useTime = currentTime - startTime;
                        startTime = currentTime;
                        speed = 1000L * calcSpeedLen / useTime;
                        count = 0;
                        calcSpeedLen = 0L;
                        receiveLen = 0L;
                        //应该保存数据库
                        downloadLengthChange(breakPoint + getLen, totalLength, speed);
                    }
                }
                bos.close();
                inputStream.close();
                if (dataLength != getLen) {
                    downloadServiceCallable.onDownloadError(downloadItemInfo, 3, "下载长度不相等");
                } else {
                    downloadLengthChange(breakPoint + getLen, totalLength, speed);
                    downloadServiceCallable.onDownloadSuccess(downloadItemInfo.copy());
                }
            }
        } catch (IOException ioException) {
            if (getHttpService() != null) {
//                getHttpService().abortRequest();
            }
            return;
        } catch (Exception e) {
            if (getHttpService() != null) {
//                getHttpService().abortRequest();
            }
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (httpEntity != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建文件夹
     *
     * @param parentFile
     * @return
     */
    private boolean makeDir(File parentFile) {
        return parentFile.exists() && !parentFile.isFile()
                ? parentFile.exists() && parentFile.isDirectory()
                : parentFile.mkdirs();
    }

    private void downloadLengthChange(final long downlength, final long totalLength, final long speed) {
        downloadItemInfo.setCurrentLength(downlength);
        if (downloadServiceCallable != null) {
            final DownloadItemInfo copyDownloadItemInfo = downloadItemInfo.copy();
            synchronized (this.downloadServiceCallable) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadServiceCallable.onCurrentSizeChanged(copyDownloadItemInfo, downlength * 100 / totalLength, speed);
                    }
                });
            }
        }
    }

    /**
     * 更改下载状态
     *
     * @param status
     */
    private void downloadStatusChange(DownloadStatus status) {
        downloadItemInfo.setStatus(status.getValue());
        final DownloadItemInfo copyDownloadItemInfo = downloadItemInfo.copy();
        if (downloadServiceCallable != null) {
            synchronized (this.downloadServiceCallable) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadServiceCallable.onDownloadStatusChanged(copyDownloadItemInfo);
                    }
                });
            }
        }
    }

    /**
     * 回调 下载长度的变化
     *
     * @param totalLength
     */
    private void receviceTotalLength(long totalLength) {
        downloadItemInfo.setCurrentLength(totalLength);
        final DownloadItemInfo copyDownloadItemInfo = downloadItemInfo.copy();
        if (downloadServiceCallable != null) {
            synchronized (this.downloadServiceCallable) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadServiceCallable.onTotalLengthReceived(copyDownloadItemInfo);
                    }
                });
            }
        }
    }

    @Override
    public void onFail() {
    }

    private IHttpService getHttpService() {
        return httpService;
    }

    public File getFile() {
        return file;
    }
}