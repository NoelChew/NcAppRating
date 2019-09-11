package com.noelchew.ncapprating.library;

import androidx.annotation.StringRes;

/**
 * Created by noelchew on 7/27/16.
 */
public class NcAppRatingConfig {

    private int installedDays;
    private int launchedTimes;
    private int minimumTargetRating; // app will open market if user's rating is >= minimumTargetRating
    private int titleResourceId = 0;
    private int messageResourceId = 0;
    private int yesButtonTextResourceId = 0;
    private int noButtonTextResourceId = 0;
    private int cancelButtonTextResourceId = 0;
    private NcAppRatingListener listener = null;

    /**
     * Constructor with default criteria.
     */
    public NcAppRatingConfig() {
        this(5, 8, 4, null);
    }

    /***
     * Configuration for NcAppRating
     * @param installedDays Days installed before prompting user to rate
     * @param launchedTimes Number of times launched before prompting user to rate
     * @param minimumTargetRating Minimum rating to call NcAppRatingListener.onOpenMarket
     * @param listener NcAppRatingListener
     */
    public NcAppRatingConfig(int installedDays, int launchedTimes, int minimumTargetRating, NcAppRatingListener listener) {
        this.installedDays = installedDays;
        this.launchedTimes = launchedTimes;
        this.minimumTargetRating = minimumTargetRating;
        this.listener = listener;
    }

    public void setListener(NcAppRatingListener listener) {
        this.listener = listener;
    }

    /**
     * Set title string ID.
     * @param stringId
     */
    public void setTitle(@StringRes int stringId) {
        this.titleResourceId = stringId;
    }

    /**
     * Set message string ID.
     * @param stringId
     */
    public void setMessage(@StringRes int stringId) {
        this.messageResourceId = stringId;
    }

    /**
     * Set rate now string ID.
     * @param stringId
     */
    public void setYesButtonText(@StringRes int stringId) {
        this.yesButtonTextResourceId = stringId;
    }

    /**
     * Set no thanks string ID.
     * @param stringId
     */
    public void setNoButtonText(@StringRes int stringId) {
        this.noButtonTextResourceId = stringId;
    }

    /**
     * Set cancel string ID.
     * @param stringId
     */
    public void setCancelButtonText(@StringRes int stringId) {
        this.cancelButtonTextResourceId = stringId;
    }

    public int getInstalledDays() {
        return installedDays;
    }

    public int getLaunchedTimes() {
        return launchedTimes;
    }

    public int getMinimumTargetRating() {
        return minimumTargetRating;
    }

    public int getTitleResourceId() {
        return titleResourceId;
    }

    public int getMessageResourceId() {
        return messageResourceId;
    }

    public int getYesButtonTextResourceId() {
        return yesButtonTextResourceId;
    }

    public int getNoButtonTextResourceId() {
        return noButtonTextResourceId;
    }

    public int getCancelButtonTextResourceId() {
        return cancelButtonTextResourceId;
    }

    public NcAppRatingListener getListener() {
        return listener;
    }
}
