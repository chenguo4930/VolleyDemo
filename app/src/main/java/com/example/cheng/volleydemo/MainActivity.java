package com.example.cheng.volleydemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.example.cheng.volleydemo.http.download.DownFileManager;
import com.example.cheng.volleydemo.http.download.enums.DownloadStatus;
import com.example.cheng.volleydemo.http.download.interfaces.IDownloadCallable;

public class MainActivity extends AppCompatActivity {
    public static final String url = "http://192.168.100.24:8080/UserRecord/LoginServlet";
    public static final String url2 = "http://tt.iqtogether.com/qxueyou/sys/login/loginNew/13650566650?password=123456";
    private static final String TAG = "seven";

    private ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    public void login(View view) {
//        User user = new User();
//        user.setName("13650566650");
//        user.setPassword("123456");
//        for (int i = 0; i < 5; i++) {
//            Volley.sendRequest(url2,LoginRespense.class, new IDataListener<LoginRespense>() {
//                @Override
//                public void onSuccess(LoginRespense loginRespense) {
//                    LogUtils.e(loginRespense);
//                }
//
//                @Override
//                public void onFail() {
//
//                }
//            });
//        }

        DownFileManager downFileService = new DownFileManager();
        downFileService.download("http://gdown.baidu.com/data/wisegame/8be18d2c0dc8a9c9/WPSOffice_177.apk");
        downFileService.setDownCallable(new IDownloadCallable() {
            @Override
            public void onDownloadInfoAdd(int downloadId) {

            }

            @Override
            public void onDownloadInfoRemove(int downloadId) {

            }

            @Override
            public void onDownloadStatusChanged(int downloadId, DownloadStatus status) {

            }

            @Override
            public void onTotalLengthReceived(int downloadId, long totalLength) {

            }

            @Override
            public void onCurrentSizeChanged(int downloadId, double downloadpercent, long speed) {
                mProgressBar.setProgress((int) downloadpercent);
            }

            @Override
            public void onDownloadSuccess(int downloadId) {

            }

            @Override
            public void onDownloadError(int downloadId, int errorCode, String errorMsg) {

            }
        });
    }
}
