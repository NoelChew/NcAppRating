package com.noelchew.ncapprating.library;

import android.support.annotation.StringRes;

/**
 * Created by noelchew on 7/27/16.
 */
public class NcAppRatingConfig {

    private int criteriaInstallDays;
    private int criteriaLaunchTimes;
    private boolean forceMode; // if true, will open market once user change rating
    private int upperBound; // app will open market if user's rating is >= upperBound
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
        this(5, 8, false, 4, null);
    }

    public NcAppRatingConfig(int criteriaInstallDays, int criteriaLaunchTimes, boolean forceMode, int upperBound, NcAppRatingListener listener) {
        this.criteriaInstallDays = criteriaInstallDays;
        this.criteriaLaunchTimes = criteriaLaunchTimes;
        this.forceMode = forceMode;
        this.upperBound = upperBound;
        this.listener = listener;
    }

    public void setCallback(NcAppRatingListener mCallback) {
        this.listener = mCallback;
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

    public int getCriteriaInstallDays() {
        return criteriaInstallDays;
    }

    public int getCriteriaLaunchTimes() {
        return criteriaLaunchTimes;
    }

    public boolean isForceMode() {
        return forceMode;
    }

    public int getUpperBound() {
        return upperBound;
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
