package com.noelchew.ncapprating.library;

/**
 * Created by noelchew on 7/27/16.
 */
public interface NcAppRatingListener {
    /**
     * "Rate now" event
     *
     String appPackage = context.getPackageName();
     Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
     context.startActivity(intent);
     */
    void onOpenMarket(int rating);

    /**
     * "No, thanks" event
     */
    void onNoClicked();

    /**
     * "Later" event
     */
    void onCancelClicked();

    // bad rating received. will prompt feedback
    void onShowFeedbackDialog(int rating);
}
