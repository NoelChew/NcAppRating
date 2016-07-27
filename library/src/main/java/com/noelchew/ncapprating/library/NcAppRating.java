package com.noelchew.ncapprating.library;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Created by noelchew on 7/18/16.
 */
public class NcAppRating {

    private static final String TAG = "NcAppRating";
    private static final String PREF_NAME = "NcAppRating";
    private static final String KEY_INSTALL_DATE = "nc_install_date";
    private static final String KEY_LAUNCH_TIMES = "nc_launch_times";
    private static final String KEY_OPT_OUT = "nc_opt_out";
    private static final String KEY_ASK_LATER_DATE = "nc_ask_later_date";

    /**
     * If true, print LogCat
     */
    public static final boolean DEBUG = false;

    private Context context;

    private Date mInstallDate = new Date();
    private int mLaunchTimes = 0;
    private boolean mOptOut = false;
    private Date mAskLaterDate = new Date();

    private Config sConfig = new Config();

    // Weak ref to avoid leaking the context
    private WeakReference<AlertDialog> sDialogRef = null;

    private AlertDialog.Builder builder;
    private View dialogView;

    public NcAppRating(Context context) {
        this.context = context;
        onStart();
    }

    public NcAppRating(Context context, Config config) {
        this.context = context;
        this.sConfig = config;
        onStart();
    }

    public NcAppRating(Context context, Callback callback) {
        this.context = context;
        this.sConfig.setCallback(callback);
        onStart();
    }

    public NcAppRating(Context context, Config config, Callback callback) {
        this.context = context;
        config.setCallback(callback);
        this.sConfig = config;
        onStart();
    }

    /**
     * Call this API when the launcher activity is launched.<br>
     * It is better to call this API in onStart() of the launcher activity.
     */
    private void onStart() {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        // If it is the first launch, save the date in shared preference.
        if (pref.getLong(KEY_INSTALL_DATE, 0) == 0L) {
            storeInstallDate(context, editor);
        }
        // Increment launch times
        int launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
        launchTimes++;
        editor.putInt(KEY_LAUNCH_TIMES, launchTimes);
        log("Launch times; " + launchTimes);

        editor.commit();

        mInstallDate = new Date(pref.getLong(KEY_INSTALL_DATE, 0));
        mLaunchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
        mOptOut = pref.getBoolean(KEY_OPT_OUT, false);
        mAskLaterDate = new Date(pref.getLong(KEY_ASK_LATER_DATE, 0));

        printStatus(context);
    }

    /**
     * Show the rate dialog if the criteria is satisfied.
     * @param context Context
     * @return true if shown, false otherwise.
     */
    public boolean showRateDialogIfNeeded() {
        if (shouldShowRateDialog()) {
            showRateDialog(context);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Show the rate dialog if the criteria is satisfied.
     * @param context Context
     * @param themeId Theme ID
     * @return true if shown, false otherwise.
     */
    public boolean showRateDialogIfNeeded(int themeId) {
        if (shouldShowRateDialog()) {
            showRateDialog(context, themeId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check whether the rate dialog should be shown or not.
     * Developers may call this method directly if they want to show their own view instead of
     * dialog provided by this library.
     * @return
     */
    public boolean shouldShowRateDialog() {
        if (mOptOut) {
            return false;
        } else {
            if (mLaunchTimes >= sConfig.mCriteriaLaunchTimes) {
                return true;
            }
            long threshold = sConfig.mCriteriaInstallDays * 24 * 60 * 60 * 1000L;	// msec
            if (new Date().getTime() - mInstallDate.getTime() >= threshold &&
                    new Date().getTime() - mAskLaterDate.getTime() >= threshold) {
                return true;
            }
            return false;
        }
    }

    /**
     * Show the rate dialog
     * @param context
     */
    public void showRateDialog(final Context context) {
        builder = new AlertDialog.Builder(context);
        showRateDialog(context, builder);
    }

    /**
     * Show the rate dialog
     * @param context
     * @param themeId
     */
    public void showRateDialog(final Context context, int themeId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, themeId);
        showRateDialog(context, builder);
    }

    /**
     * Stop showing the rate dialog
     * @param context
     */
    public void stopRateDialog(final Context context){
        setOptOut(context, true);
    }

    private void showRateDialog(final Context context, AlertDialog.Builder builder) {
        if (sDialogRef != null && sDialogRef.get() != null) {
            // Dialog is already present
            return;
        }

        int titleId = sConfig.mTitleId != 0 ? sConfig.mTitleId : R.string.nc_utils_rate_dialog_title;
        int messageId = sConfig.mMessageId != 0 ? sConfig.mMessageId : R.string.nc_utils_rate_dialog_message;
        int cancelButtonID = sConfig.mCancelButton != 0 ? sConfig.mCancelButton : R.string.nc_utils_rate_dialog_cancel;
        int thanksButtonID = sConfig.mNoButtonId != 0 ? sConfig.mNoButtonId : R.string.nc_utils_rate_dialog_no;
        int rateButtonID = sConfig.mYesButtonId != 0 ? sConfig.mYesButtonId : R.string.nc_utils_rate_dialog_ok;
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogView = inflater.inflate(R.layout.stars, null);
        TextView contentTextView = (TextView)dialogView.findViewById(R.id.text_content);
        contentTextView.setText(messageId);
        RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Log.d(TAG, "Rating changed : " + v);
                if (sConfig.mForceMode && v >= sConfig.mUpperBound) {
                    if (sConfig.mCallback != null) {
                        sConfig.mCallback.onOpenMarket((int)ratingBar.getRating());
                    }
                }
            }
        });
        builder.setView(dialogView);
        builder.setTitle(titleId);
        builder.setPositiveButton(rateButtonID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sConfig.mCallback != null) {
//                    sCallback.onYesClicked();
                    final RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
                    if (ratingBar.getRating() >= sConfig.mUpperBound) {
                        sConfig.mCallback.onOpenMarket((int)ratingBar.getRating());
                        setOptOut(context, true);
                    } else if (ratingBar.getRating() == 0) {
                        Toast.makeText(context, R.string.nc_utils_rate_dialog_please_select_a_rating, Toast.LENGTH_SHORT).show();
                    } else {
                        // delay a while before showing feedback dialog
                        final ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setTitle(R.string.nc_utils_feedback_loading);
                        progressDialog.setMessage(context.getString(R.string.nc_utils_feedback_please_wait));
                        progressDialog.show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                sConfig.mCallback.onShowFeedbackDialog((int)ratingBar.getRating());
                                setOptOut(context, true);
                            }
                        }, 1500);

                    }

                }
            }
        });
        builder.setNeutralButton(cancelButtonID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sConfig.mCallback != null) {
                    sConfig.mCallback.onCancelClicked();
                }
                clearSharedPreferences(context);
                storeAskLaterDate(context);
            }
        });
        builder.setNegativeButton(thanksButtonID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sConfig.mCallback != null) {
                    sConfig.mCallback.onNoClicked();
                }
                setOptOut(context, true);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (sConfig.mCallback != null) {
                    sConfig.mCallback.onCancelClicked();
                }
                clearSharedPreferences(context);
                storeAskLaterDate(context);
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sDialogRef.clear();
            }
        });
        sDialogRef = new WeakReference<>(builder.show());
    }

    /**
     * Clear data in shared preferences.<br>
     * This API is called when the rate dialog is approved or canceled.
     * @param context
     */
    private void clearSharedPreferences(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(KEY_INSTALL_DATE);
        editor.remove(KEY_LAUNCH_TIMES);
        editor.commit();
    }

    /**
     * Set opt out flag. If it is true, the rate dialog will never shown unless app data is cleared.
     * @param context
     * @param optOut
     */
    public void setOptOut(final Context context, boolean optOut) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_OPT_OUT, optOut);
        editor.commit();
        mOptOut = optOut;
    }

    /**
     * Store install date.
     * Install date is retrieved from package manager if possible.
     * @param context
     * @param editor
     */
    private void storeInstallDate(final Context context, SharedPreferences.Editor editor) {
        Date installDate = new Date();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            PackageManager packMan = context.getPackageManager();
            try {
                PackageInfo pkgInfo = packMan.getPackageInfo(context.getPackageName(), 0);
                installDate = new Date(pkgInfo.firstInstallTime);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        editor.putLong(KEY_INSTALL_DATE, installDate.getTime());
        log("First install: " + installDate.toString());
    }

    /**
     * Store the date the user asked for being asked again later.
     * @param context
     */
    private void storeAskLaterDate(final Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(KEY_ASK_LATER_DATE, System.currentTimeMillis());
        editor.commit();
    }

    /**
     * Print values in SharedPreferences (used for debug)
     * @param context
     */
    private void printStatus(final Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        log("*** NcAppRatingUtil Status ***");
        log("Install Date: " + new Date(pref.getLong(KEY_INSTALL_DATE, 0)));
        log("Launch Times: " + pref.getInt(KEY_LAUNCH_TIMES, 0));
        log("Opt out: " + pref.getBoolean(KEY_OPT_OUT, false));
    }

    /**
     * Print log if enabled
     * @param message
     */
    private void log(String message) {
        if (DEBUG) {
            Log.v(TAG, message);
        }
    }

    /**
     * RateThisApp configuration.
     */
    public static class Config {
        private int mCriteriaInstallDays;
        private int mCriteriaLaunchTimes;
        private boolean mForceMode; // if true, will open market once user change rating
        private int mUpperBound; // app will open market if user's rating is >= mUpperBound
        private int mTitleId = 0;
        private int mMessageId = 0;
        private int mYesButtonId = 0;
        private int mNoButtonId = 0;
        private int mCancelButton = 0;
        private Callback mCallback = null;

        /**
         * Constructor with default criteria.
         */
        public Config() {
            this(5, 8, false, 4, null);
        }

        public Config(int criteriaInstallDays, int criteriaLaunchTimes, boolean forceMode, int upperBound, Callback callback) {
            this.mCriteriaInstallDays = criteriaInstallDays;
            this.mCriteriaLaunchTimes = criteriaLaunchTimes;
            this.mForceMode = forceMode;
            this.mUpperBound = upperBound;
            this.mCallback = callback;
        }

        public void setCallback(Callback mCallback) {
            this.mCallback = mCallback;
        }

        /**
         * Set title string ID.
         * @param stringId
         */
        public void setTitle(@StringRes int stringId) {
            this.mTitleId = stringId;
        }

        /**
         * Set message string ID.
         * @param stringId
         */
        public void setMessage(@StringRes int stringId) {
            this.mMessageId = stringId;
        }

        /**
         * Set rate now string ID.
         * @param stringId
         */
        public void setYesButtonText(@StringRes int stringId) {
            this.mYesButtonId = stringId;
        }

        /**
         * Set no thanks string ID.
         * @param stringId
         */
        public void setNoButtonText(@StringRes int stringId) {
            this.mNoButtonId = stringId;
        }

        /**
         * Set cancel string ID.
         * @param stringId
         */
        public void setCancelButtonText(@StringRes int stringId) {
            this.mCancelButton = stringId;
        }
    }

    /**
     * Callback of dialog click event
     */
    public interface Callback {
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
}
