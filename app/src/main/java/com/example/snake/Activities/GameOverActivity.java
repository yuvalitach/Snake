package com.example.snake.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snake.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.BuildConfig;

public class GameOverActivity extends AppCompatActivity {

    private Bundle bundle,getDataBundle;

    public static  final String SCORE = "SCORE";
    public static  final String PREMIUM = "PREMIUM";
    public static  final String EMAIL = "EMAIL";
    MaterialButton btnPlayAgain, btnPurchaseLife;
    TextView scoreTv;
    private FrameLayout main_LAY_banner;
    private RewardedAd mRewardedAd;
    private boolean premium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        findViews();
        bundle = new Bundle();
        getDataBundle = getIntent().getExtras();
        scoreTv.setText("Your score: "+getDataBundle.getInt(SCORE));
        loadVideoAd();

        premium = getDataBundle.getBoolean(PREMIUM);
        if (!premium)
            showBanner();


        btnPlayAgain.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!premium) {
                showVideoAd();
            }
            else {
                startGameActivity();
            }
            }
    });

        btnPurchaseLife.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bundle.putBoolean(GameActivity.PREMIUM,premium);
                bundle.putString(GameActivity.EMAIL,getDataBundle.getString(EMAIL));
                bundle.putInt(GameActivity.SCORE,getDataBundle.getInt(SCORE));
                Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });


    }

    private void findViews() {
        btnPlayAgain = findViewById(R.id.Play_again_BTN);
        btnPurchaseLife = findViewById(R.id.Purchase_life_BTN);
        scoreTv = findViewById(R.id.scoreTv);
        main_LAY_banner = findViewById(R.id.main_LAY_banner);
    }

    private void showBanner() {
        String UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
        AdView adView = new AdView(this);
        adView.setAdUnitId(UNIT_ID);
        main_LAY_banner.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }


    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void loadVideoAd() {
        // action_a.setEnabled(false);
        String UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
        if (BuildConfig.DEBUG) {
            UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, UNIT_ID,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d("pttt", loadAdError.toString());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        //action_b.setEnabled(true);
                        Log.d("pttt", "Ad was loaded.");
                    }
                });
    }


    private void showVideoAd() {
        mRewardedAd.show(this, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                loadVideoAd();
                bundle.putBoolean(GameActivity.PREMIUM,getDataBundle.getBoolean(PREMIUM));
                bundle.putString(GameActivity.EMAIL,getDataBundle.getString(EMAIL));
                Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();

//                startGameActivity();
            }
        });
    }

    private void startGameActivity (){
        bundle.putBoolean(GameActivity.PREMIUM,getDataBundle.getBoolean(PREMIUM));
        bundle.putString(GameActivity.EMAIL,getDataBundle.getString(EMAIL));
        Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();

    }
}