package com.example.snake;

import android.app.Application;

import com.example.snake.Models.MyDataManager;
import com.google.android.gms.ads.MobileAds;
import com.example.snake.MyInApp.Item;

public class App extends Application {

    Item[] items = new Item[] {
            new Item(MyInApp.TYPE.Subscription, ""),
            new Item(MyInApp.TYPE.OneTimeInApp, ""),
    };

    @Override
    public void onCreate() {
        super.onCreate();
        MyDataManager.initHelper();
        MobileAds.initialize(this);
        MyInApp.initHelper(this, "", items);
    }
}