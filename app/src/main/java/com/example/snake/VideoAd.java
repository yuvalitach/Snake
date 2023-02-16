package com.example.snake;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.BuildConfig;

public class VideoAd {

    public interface CallBack {
        void unitLoaded();
        void earned();
        void canceled();
        void failed();
    }

    public static final String TAG = "PTTT_VideoAd";

    private Activity activity;
    private String adUnitId;
    private CallBack callBack;

    private RewardedAd mRewardedAd;
    private boolean isRewarded = false;

    public VideoAd(Activity activity, String adUnitId, CallBack callBack) {
        this.activity = activity;
        this.adUnitId = adUnitId;
        this.callBack = callBack;

        load(false);
    }

    public boolean isLoaded() {
        return mRewardedAd != null;
    }


    private void load(boolean showAfterLoading) {
        AdRequest adRequest = new AdRequest.Builder().build();
        //RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("E3D543B2BDC7AEA65DE5DE9E2538ED50")).build();
        //MobileAds.setRequestConfiguration(configuration);

        RewardedAd.load(activity, adUnitId,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        Log.d(TAG, "Ad was loaded.");
                        mRewardedAd = rewardedAd;
                        callBack.unitLoaded();
                        if (showAfterLoading) {
                            show();
                        }
                    }
                });
    }

    public void show() {
        if (mRewardedAd != null) {
            Log.d(TAG, "mRewardedAd != null");
            isRewarded = false;

            mRewardedAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d(TAG, "onAdShowedFullScreenContent");
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(activity, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            // Called when ad fails to show.
                            Log.d(TAG, "onAdFailedToShowFullScreenContent");
                            // Don't forget to set the ad reference to null so you
                            // don't show the ad a second time.
                            mRewardedAd = null;
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(activity, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT).show();
                            }
                            load(true);
                            callBack.failed();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Don't forget to set the ad reference to null so you
                            // don't show the ad a second time.
                            mRewardedAd = null;
                            Log.d(TAG, "onAdDismissedFullScreenContent");
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(activity, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT).show();
                            }
                            // Preload the next rewarded ad.

                            if (!isRewarded) {
                                load(false);
                                callBack.canceled();
                            }
                        }
                    });

            mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    isRewarded = true;
                    mRewardedAd = null;
                    load(false);
                    callBack.earned();
                }
            });
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
            load(true);
            callBack.failed();
        }
    }

}
