package com.mopub.simpleadsdemo;

import android.content.Context;
import android.util.Log;

import com.greystripe.sdk.AdPosition;
import com.greystripe.sdk.GSAd;
import com.greystripe.sdk.GSAdErrorCode;
import com.greystripe.sdk.GSAdListener;
import com.greystripe.sdk.GSMobileBannerAdView;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;

import java.util.Map;

/*
 * Tested with Greystripe SDK 2.4.0.
 */
class GreystripeBanner extends CustomEventBanner implements GSAdListener {
    public static final String DEFAULT_GREYSTRIPE_APP_ID = "YOUR_GREYSTRIPE_APP_ID";

    private CustomEventBannerListener mBannerListener;
    private GSMobileBannerAdView mGreystripeAd;

    /*
     * Abstract methods from CustomEventBanner
     */
    @Override
    protected void loadBanner(final Context context,
                              final CustomEventBannerListener bannerListener,
                              final Map<String, Object> localExtras,
                              final Map<String, String> serverExtras) {
        mBannerListener = bannerListener;

        /*
         * You may also pass this String down in the serverExtras Map by specifying Custom Event Data
         * in MoPub's web interface.
         */
        String greystripeAppId = DEFAULT_GREYSTRIPE_APP_ID;
        mGreystripeAd = new GSMobileBannerAdView(context, greystripeAppId);
        mGreystripeAd.addListener(this);

        mGreystripeAd.refresh();
    }

    @Override
    protected void onInvalidate() {
        mGreystripeAd.removeListener(this);
    }

    /*
     * GSAdListener implementation
     */
    @Override
    public void onAdClickthrough(final GSAd greystripeAd) {
        Log.d("MoPub", "Greystripe banner ad clicked.");
        mBannerListener.onBannerClicked();
    }

    @Override
    public void onAdDismissal(final GSAd greystripeAd) {
        Log.d("MoPub", "Greystripe banner ad modal dismissed.");
    }

    @Override
    public void onFailedToFetchAd(final GSAd greystripeAd, final GSAdErrorCode errorCode) {
        Log.d("MoPub", "Greystripe banner ad failed to load.");
        mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
    }

    @Override
    public void onFetchedAd(final GSAd greystripeAd) {
        if (mGreystripeAd != null && mGreystripeAd.isAdReady()) {
            Log.d("MoPub", "Greystripe banner ad loaded successfully. Showing ad...");
            mBannerListener.onBannerLoaded(mGreystripeAd);
        } else {
            mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
        }
    }

    @Override
    public void onAdCollapse(final GSAd greystripeAd) {
        Log.d("MoPub", "Greystripe banner ad collapsed.");
        mBannerListener.onBannerCollapsed();
    }

    @Override
    public void onAdExpansion(final GSAd greystripeAd) {
        Log.d("MoPub", "Greystripe banner ad expanded.");
        mBannerListener.onBannerExpanded();
    }

    @Override
    public void onAdResize(final GSAd gsAd, final AdPosition adPosition) {
    }
}
