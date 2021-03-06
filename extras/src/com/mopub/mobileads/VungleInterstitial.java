package com.mopub.mobileads;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.vungle.publisher.EventListener;
import com.vungle.publisher.VunglePub;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * Tested with Vungle SDK 3.2.2.
 */
public class VungleInterstitial extends CustomEventInterstitial implements EventListener {

    public static final String DEFAULT_VUNGLE_APP_ID = "YOUR_DEFAULT_VUNGLE_APP_ID";

    /*
     * APP_ID_KEY is intended for MoPub internal use. Do not modify.
     */
    private static final String APP_ID_KEY = "appId";

    private final VunglePub mVunglePub;
    private final Handler mHandler;
    private final ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor;
    private CustomEventInterstitialListener mCustomEventInterstitialListener;
    private boolean mIsLoading;

    public VungleInterstitial() {
        mHandler = new Handler();
        mScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(10);
        mVunglePub = VunglePub.getInstance();
    }

    @Override
    protected void loadInterstitial(Context context,
            CustomEventInterstitialListener customEventInterstitialListener,
            Map<String, Object> localExtras,
            Map<String, String> serverExtras) {
        mCustomEventInterstitialListener = customEventInterstitialListener;

        if (context == null) {
            mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
            return;
        }

        /*
         * You may pass the Vungle App Id in the serverExtras Map by specifying Custom Event Data
         * in MoPub's web interface.
         */
        final String appId;
        if (extrasAreValid(serverExtras)) {
            appId = serverExtras.get(APP_ID_KEY);
        } else {
            appId = DEFAULT_VUNGLE_APP_ID;
        }

        // init clears the event listener.
        mVunglePub.init(context, appId);
        mVunglePub.setEventListener(this);
        scheduleOnInterstitialLoaded();
    }

    @Override
    protected void showInterstitial() {
        if (mVunglePub.isCachedAdAvailable()) {
            mVunglePub.playAd();
        } else {
            Log.d("MoPub", "Tried to show a Vungle interstitial ad before it finished loading. Please try again.");
        }
    }

    @Override
    protected void onInvalidate() {
        mVunglePub.setEventListener(null);
        mScheduledThreadPoolExecutor.shutdownNow();
        mIsLoading = false;
    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        return serverExtras.containsKey(APP_ID_KEY);
    }

    private void scheduleOnInterstitialLoaded() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            if (mVunglePub.isCachedAdAvailable()) {
                Log.d("MoPub", "Vungle interstitial ad successfully loaded.");
                mScheduledThreadPoolExecutor.shutdownNow();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCustomEventInterstitialListener.onInterstitialLoaded();
                    }
                });
                mIsLoading = false;
            }
            }
        };

        if (!mIsLoading) {
            mScheduledThreadPoolExecutor.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS);
            mIsLoading = true;
        }
    }

    /*
     * EventListener implementation
     */

    @Override
    public void onVideoView(final boolean isCompletedView, final int watchedMillis, final int videoDurationMillis) {
        final double watchedPercent = (double) watchedMillis / videoDurationMillis * 100;
        Log.d("MoPub", String.format("%.1f%% of Vungle video watched.", watchedPercent));
    }

    @Override
    public void onAdStart() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("MoPub", "Showing Vungle interstitial ad.");
                mCustomEventInterstitialListener.onInterstitialShown();
            }
        });
    }

    @Override
    public void onAdEnd(final boolean wasCallToActionClicked) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("MoPub", "Vungle interstitial ad dismissed.");
                mCustomEventInterstitialListener.onInterstitialDismissed();
            }
        });
    }

    @Override
    public void onAdUnavailable(final String s) {
        mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
    }

    @Override
    public void onCachedAdAvailable() {
        // Due to the inconsistent behavior of this method, we rely on scheduleOnInterstitialLoaded instead.
    }

    @Deprecated // for testing
    ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
        return mScheduledThreadPoolExecutor;
    }
}
