package com.example.snake;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class Activity_VideoNew extends AppCompatActivity {

    public static final String TAG = "PTTT_Activity_VideoNew";

    private MaterialButton action_a;


    VideoAd coinVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        action_a.setOnClickListener(view -> start());

        initAds();
    }

    private void start() {
        if (coinVideo.isLoaded()) {
            action_a.setEnabled(false);
            coinVideo.show();
        } else {
            initAds();
            // move to next level
        }
    }

    VideoAd.CallBack callBack = new VideoAd.CallBack() {
        @Override
        public void unitLoaded() {
            action_a.setEnabled(true);
        }

        @Override
        public void earned() {

        }

        @Override
        public void canceled() {

        }

        @Override
        public void failed() {

        }
    };

    private void initAds() {
        action_a.setEnabled(false);
        String UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
        coinVideo = new VideoAd(this, UNIT_ID, callBack);
    }



    private void findViews() {
        action_a = findViewById(R.id.action_a);
    }

}
