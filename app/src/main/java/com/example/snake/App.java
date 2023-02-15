package com.example.snake;

import android.app.Application;

import com.example.snake.Models.MyDataManager;
import com.google.android.gms.ads.MobileAds;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this);
        MyDataManager.initHelper();
    }
}