package com.example.snake;


import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class Activity_Shop extends AppCompatActivity {

    public static final String COINS_1_ID = "test_coins_10_2";
    private MaterialButton shop_BTN_coins10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        shop_BTN_coins10 = findViewById(R.id.shop_BTN_coins10);

        MyInApp.getInstance().addCallBack(COINS_1_ID, callBackMyInApp_coins10);
        initCoins1();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyInApp.getInstance().removeCallBack(COINS_1_ID, callBackMyInApp_coins10);
    }

    private void initCoins1() {
        shop_BTN_coins10.setVisibility(View.GONE);

        if (MyInApp.getInstance().getInitialStatus() >= 0) {
            MyInApp.getInstance().getInAppDetails(COINS_1_ID);
        } else {
            // No In App initialized
        }
    }

    MyInApp.CallBack_MyInApp callBackMyInApp_coins10 = new MyInApp.CallBack_MyInApp() {
        @Override
        public void successfullyPurchased(boolean isPurchasedNow, String purchaseKey) {
            // add 10 coins to user bank
        }

        @Override
        public void purchaseFailed(String purchaseKey, int code, String message) {

        }

        @Override
        public void details(boolean isInAppExist, String title, String description, String price, long priceMic) {
            updateCoins(price);
        }
    };

    private void updateCoins(String price) {
        shop_BTN_coins10.setVisibility(View.VISIBLE);

        shop_BTN_coins10.setText("Buy 10 Coins for\n" + price);

    }

}