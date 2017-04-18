package com.example.cheng.volleydemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.apkfuns.logutils.LogUtils;
import com.example.cheng.volleydemo.http.HttpMethod;
import com.example.cheng.volleydemo.http.Volley;
import com.example.cheng.volleydemo.http.interfaces.IDataListener;

public class MainActivity extends AppCompatActivity {
    public static final String url = "http://192.168.100.24:8080/UserRecord/LoginServlet";
    public static final String url2 = "http://tt.iqtogether.com/qxueyou/sys/login/loginNew/13650566650?password=123456";
    private static final String TAG = "seven";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login(View view) {
        User user = new User();
        user.setName("13650566650");
        user.setPassword("123456");
        for (int i = 0; i < 5; i++) {
            Volley.sendRequest(null, url2, HttpMethod.GET, LoginRespense.class, new IDataListener<LoginRespense>() {
                @Override
                public void onSuccess(LoginRespense loginRespense) {
                    LogUtils.e(loginRespense);
                }

                @Override
                public void onFail() {

                }
            });
        }

//        DownFileManager downFileService = new DownFileManager();
//        downFileService.download("http://gdown.baidu.com/data/wisegame/8be18d2c0dc8a9c9/WPSOffice_177.apk");
    }
}
