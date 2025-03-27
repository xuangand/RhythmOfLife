package com.xuangand.rhythmoflife.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.xuangand.rhythmoflife.activity.MainActivity;

import java.util.Date;

public class AppOpenAdManager {
    private static final String LOG_TAG = "AppOpenAdManager";
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921";

    private AppOpenAd appOpenAd = null;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;
    private long loadTime = 0;

    /** Constructor. */
    public AppOpenAdManager(MainActivity mainActivity) {}

    /** Request an ad. */
    private void loadAd(Context context) {
        if (isLoadingAd || isAdAvailable()) {
            return;
        }

        isLoadingAd = true;
//        AdRequest request = new AdRequest.Builder().build();
//        AppOpenAd.load(
//                context, AD_UNIT_ID, request,
//                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
//                new AppOpenAd.AppOpenAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(AppOpenAd ad) {
//                        // Called when an app open ad has loaded.
//                        Log.d(LOG_TAG, "Ad was loaded.");
//                        appOpenAd = ad;
//                        isLoadingAd = false;
//                        loadTime = (new Date()).getTime();
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(LoadAdError loadAdError) {
//                        // Called when an app open ad has failed to load.
//                        Log.d(LOG_TAG, loadAdError.getMessage());
//                        isLoadingAd = false;
//                    }
//                });
    }

    /** Check if ad exists and can be shown. */
    private boolean isAdAvailable() {
        return appOpenAd != null;
    }

}
