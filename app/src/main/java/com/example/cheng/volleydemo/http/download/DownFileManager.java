/*
 * DownFileManager    2017-04-08
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http.download;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.cheng.volleydemo.db.BaseDaoFactory;
import com.example.cheng.volleydemo.http.HttpTask;
import com.example.cheng.volleydemo.http.RequestHolder;
import com.example.cheng.volleydemo.http.ThreadPoolManager;
import com.example.cheng.volleydemo.http.download.dao.DownloadDao;
import com.example.cheng.volleydemo.http.download.enums.DownloadStatus;
import com.example.cheng.volleydemo.http.download.enums.DownloadStopMode;
import com.example.cheng.volleydemo.http.download.enums.Priority;
import com.example.cheng.volleydemo.http.download.interfaces.IDownloadCallable;
import com.example.cheng.volleydemo.http.download.interfaces.IDownloadServiceCallable;
import com.example.cheng.volleydemo.http.interfaces.IHttpListener;
import com.example.cheng.volleydemo.http.interfaces.IHttpService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;

import static com.example.cheng.volleydemo.MainActivity.url;


/**
 * class description here
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-08
 */
public class DownFileManager implements IDownloadServiceCallable {
    private static final String TAG = "DownFileManager";

    private byte[] lock = new byte[0];
    DownloadDao downloadDao = BaseDaoFactory.getInstance().getDataHelper(DownloadDao.class, DownloadItemInfo.class);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    /**
     * 观察者模式
     */
    private final List<IDownloadCallable> applisteners = new CopyOnWriteArrayList<IDownloadCallable>();
    /**
     * 怎在下载的所有任务
     */
    private static List<DownloadItemInfo> downloadFileTaskList = new CopyOnWriteArrayList();

    Handler handler = new Handler(Looper.getMainLooper());

    public int download(String url) {
        String[] preFix = url.split("/");
        return this.download(url, Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + preFix[preFix.length - 1]);
    }

    public int download(String url, String filePath) {
        String[] preFix = url.split("/");
        String displayName = preFix[preFix.length - 1];
        return this.download(url, filePath, displayName);
    }

    public int download(String url, String filePath, String displayName) {
        return this.download(url, filePath, displayName, Priority.middle);
    }

    public int download(String url, String filePath,
                        String displayName, Priority priority) {


        if (priority == null) {
            priority = Priority.low;
        }

        File file = new File(filePath);
        DownloadItemInfo downloadItemInfo = null;

        downloadItemInfo = downloadDao.findRecord(url, filePath);
        //没下载
        if (downloadItemInfo == null) {
            /**
             * 根据文件路劲查找
             */
            List<DownloadItemInfo> samesFile = downloadDao.findRecord(filePath);
            /**
             * 大于0，表示下载
             */
            if (samesFile.size() > 0) {
                DownloadItemInfo sameDown = samesFile.get(0);
                if (sameDown.getCurrentLen() == sameDown.getTotalLen()) {
                    synchronized (applisteners) {
                        for (IDownloadCallable applistener : applisteners) {
                            applistener.onDownloadError(sameDown.getId(), 2, "文件已经下载");
                        }
                    }
                }
            }

            /**
             * 插入数据库
             */
            downloadItemInfo = downloadDao.addRecord(url, filePath, displayName, priority.getValue());
            if (downloadItemInfo != null) {
                synchronized (applisteners) {
                    for (IDownloadCallable applistener : applisteners) {
                        //通知应用层，数据库被添加了
                        applistener.onDownloadInfoAdd(downloadItemInfo.getId());
                    }
                }
            }
            downloadItemInfo = downloadDao.findRecord(url, filePath);
            if (isDowning(file.getAbsolutePath())) {
                synchronized (applisteners) {
                    for (IDownloadCallable applistener : applisteners) {
                        applistener.onDownloadError(downloadItemInfo.getId(), 3, "正在下载，请不要重复添加");
                    }
                }
                return downloadItemInfo.getId();
            }
            if (downloadItemInfo != null) {
                downloadItemInfo.setPriority(priority.getValue());
                //判断数据库存的状态是否是完成
                if (downloadItemInfo.getStatus() != DownloadStatus.finish.getValue()) {
                    if (downloadItemInfo.getTotalLen() == 0L || file.length() == 0L) {
                        Log.i(TAG, "还未开始下载");
                        downloadItemInfo.setStatus(DownloadStatus.failed.getValue());
                    }
                }
                //判断数据库中 总长度是否等于文件长度
                if (downloadItemInfo.getTotalLen() == file.length()) {
                    downloadItemInfo.setStatus(DownloadStatus.finish.getValue());
                    synchronized (applisteners) {
                        for (IDownloadCallable applistener : applisteners) {
                            //应该在每一个上层回调接口处try catch
                            try {
                                applistener.onDownloadError(downloadItemInfo.getId(), 4, "已经下载了");
                            } catch (Exception e) {

                            }
                        }
                    }
                    /**
                     * 更新
                     */
                    downloadDao.updateRecord(downloadItemInfo);
                }


                /**
                 * 判断是否已经下载完成
                 */
                if (downloadItemInfo.getStatus() == DownloadStatus.finish.getValue()) {
                    Log.i(TAG, "已经下载完成  回调应用层");
                    final int downId = downloadItemInfo.getId();
                    synchronized (applisteners) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                for (IDownloadCallable downloadCallable : applisteners) {
                                    downloadCallable.onDownloadStatusChanged(downId, DownloadStatus.finish);
                                }
                            }
                        });
                    }
                    downloadDao.removeRecordFromMemery(downId);
                    return downloadItemInfo.getId();
                }//之前的下载 状态为暂停状态

            }
            List<DownloadItemInfo> allDowning = downloadFileTaskList;
            //当前下载不是最高级  则先退出下载
            if (priority != Priority.high) {
                for (DownloadItemInfo downling : allDowning) {
                    //从下载表中  获取到全部正在下载的任务
                    downling = downloadDao.findSigleRecord(downling.getFilePath());

                    if (downloadItemInfo != null && downloadItemInfo.getPriority() == Priority.high.getValue()) {
                        if (downloadItemInfo.getFilePath().equals(downling.getFilePath())) {
                            return downloadItemInfo.getId();
                        }
                    }
                }
            }
            //
            DownloadItemInfo addDownloadInfo = reallyDown(downloadItemInfo);
            if (priority == Priority.high || priority == Priority.middle) {
                synchronized (allDowning) {
                    for (DownloadItemInfo downloadItemInfo1 : allDowning) {
                        if (!downloadItemInfo.getFilePath().equals(downloadItemInfo1.getFilePath())) {
                            DownloadItemInfo downingInfo = downloadDao.findSigleRecord(downloadItemInfo1.getFilePath());
                            if (downingInfo != null) {
                                pause(downloadItemInfo.getId(), DownloadStopMode.auto);
                            }
                        }
                    }
                }
                return downloadItemInfo.getId();
            }
            return -1;

        }
        return -1;
    }

    public DownloadItemInfo reallyDown(DownloadItemInfo downloadItemInfo) {
        synchronized (lock) {
            String[] preFix = url.split("/");
            String afterFix = preFix[preFix.length - 1];
            File file = new File(Environment.getExternalStorageDirectory(), afterFix);

            DownloadItemInfo downloadItemInfo = new DownloadItemInfo(url, file.getAbsolutePath());
            RequestHolder requestHolder = new RequestHolder();

            //设置请求下载的策略
            IHttpService httpService = new FileDownHttpService();
            //得到请求头的参数 map
            Map<String, String> map = httpService.getHttpHeadMap();

            IHttpListener httpListener = new DownloadListener(downloadItemInfo, this, httpService);

            requestHolder.setHttpListener(httpListener);
            requestHolder.setHttpService(httpService);
            requestHolder.setUrl(url);

            HttpTask httpTask = new HttpTask(requestHolder);
            downloadFileTaskList.add(downloadItemInfo);
            try {
                ThreadPoolManager.getInstance().execte(new FutureTask<Object>(httpTask, null));
            } catch (InterruptedException e) {

            }
        }
    }

    /**
     * 停止
     *
     * @param downloadId
     * @param mode
     */
    public void pause(int downloadId, DownloadStopMode mode) {
        if (mode == null) {
            mode = DownloadStopMode.auto;
        }
        final DownloadItemInfo downloadInfo = downloadDao.findRecordById(downloadId);
        if (downloadInfo != null) {
            // 更新停止状态
            if (downloadInfo != null) {
                downloadInfo.setStopMode(mode.getValue());
                downloadInfo.setStatus(DownloadStatus.pause.getValue());
                downloadDao.updateRecord(downloadInfo);
            }
            for (DownloadItemInfo downing : downloadFileTaskList) {
                if (downloadId == downing.getId()) {
                    downing.getHttpTask().pause();
                }
            }
        }
    }

    /**
     * 添加观察者
     *
     * @param downCallable
     */
    public void setDownCallable(IDownloadCallable downCallable) {
        synchronized (applisteners) {
            applisteners.add(downCallable);
        }
    }


    @Override
    public void onDownloadStatusChanged(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onTotalLengthReceived(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onCurrentSizeChanged(DownloadItemInfo downloadItemInfo, double downLenth, long speed) {

    }

    @Override
    public void onDownloadSuccess(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onDownloadPause(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onDownloadError(DownloadItemInfo downloadItemInfo, int var2, String var3) {

    }

    /**
     * 判断当前是否正在下载
     *
     * @param absolutePath
     * @return
     */
    private boolean isDowning(String absolutePath) {
        for (DownloadItemInfo downloadItemInfo : downloadFileTaskList) {
            if (downloadItemInfo.getFilePath().equals(absolutePath)) {
                return true;
            }
        }
        return false;
    }
}