package com.example.deviceid;

import android.app.Application;
import android.content.Context;

import com.bun.miitmdid.core.JLibrary;

/**
 * @description: 全局Application
 * @author: zhukai
 * @date: 2019/12/9 15:07
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        JLibrary.InitEntry(base);
    }
}
