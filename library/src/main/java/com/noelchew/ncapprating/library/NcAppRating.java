package com.noelchew.ncapprating.library;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

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
    private static final String NEVER_SHOW_AGAIN = "nc_opt_out";
    private static final String KEY_ASK_LATER_DATE = "nc_ask_later_date";

    public static final boolean DEBUG = false;

    private Context context;

    private Date mInstallDate = new Date();
    private int mLaunchTimes = 0;
    private boolean mNeverShowAgain = false;
    private Date mAskLaterDate = new Date();

    private NcAppRatingConfig sConfig = new NcAppRatingConfig();

    // Weak ref to avoid leaking the context
    private WeakReference<AlertDialog> sDialogRef = null;

    private AlertDialog.Builder builder;
    private View dialogView;

    public NcAppRating(Context context) {
        this.context = context;
        onStart();
    }

    public NcAppRating(Context context, NcAppRatingConfig config) {
        this.context = context;
        this.sConfig = config;
        onStart();
    }

    public NcAppRating(Context context, NcAppRatingListener callback) {
        this.context = context;
        this.sConfig.setListener(callback);
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
        mNeverShowAgain = pref.getBoolean(NEVER_SHOW_AGAIN, false);
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
        if (mNeverShowAgain) {
            return false;
        } else {
            if (mLaunchTimes >= sConfig.getLaunchedTimes()) {
                return true;
            }
            long threshold = sConfig.getInstalledDays() * 24 * 60 * 60 * 1000L;	// msec
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
        if (!checkContext(context)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context, themeId);
        showRateDialog(context, builder);
    }

    /**
     * Stop showing the rate dialog
     * @param context
     */
    public void stopRateDialog(final Context context){
        setNeverShowAgain(context, true);
    }

    private void showRateDialog(final Context context, AlertDialog.Builder builder) {
        if (sDialogRef != null && sDialogRef.get() != null) {
            // Dialog is already present
            return;
        }

        int titleId = sConfig.getTitleResourceId() != 0 ? sConfig.getTitleResourceId() : R.string.nc_utils_rate_dialog_title;
        int messageId = sConfig.getMessageResourceId() != 0 ? sConfig.getMessageResourceId() : R.string.nc_utils_rate_dialog_message;
        int cancelButtonID = sConfig.getCancelButtonTextResourceId() != 0 ? sConfig.getCancelButtonTextResourceId() : R.string.nc_utils_rate_dialog_cancel;
        int thanksButtonID = sConfig.getNoButtonTextResourceId() != 0 ? sConfig.getNoButtonTextResourceId() : R.string.nc_utils_rate_dialog_no;
        int rateButtonID = sConfig.getYesButtonTextResourceId() != 0 ? sConfig.getYesButtonTextResourceId() : R.string.nc_utils_rate_dialog_ok;
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogView = inflater.inflate(R.layout.stars, null);
        TextView contentTextView = (TextView)dialogView.findViewById(R.id.text_content);
        contentTextView.setText(messageId);
        RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
        builder.setView(dialogView);
        builder.setTitle(titleId);
        builder.setPositiveButton(rateButtonID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sConfig.getListener() != null) {
//                    sCallback.onYesClicked();
                    final RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
                    if (ratingBar.getRating() >= sConfig.getMinimumTargetRating()) {
                        sConfig.getListener().onOpenMarket((int)ratingBar.getRating());
                        setNeverShowAgain(context, true);
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
                                sConfig.getListener().onShowFeedbackDialog((int)ratingBar.getRating());
                                setNeverShowAgain(context, true);
                            }
                        }, 1500);

                    }

                }
            }
        });
        builder.setNeutralButton(cancelButtonID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sConfig.getListener() != null) {
                    sConfig.getListener().onCancelClicked();
                }
                clearSharedPreferences(context);
                storeAskLaterDate(context);
            }
        });
        builder.setNegativeButton(thanksButtonID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sConfig.getListener() != null) {
                    sConfig.getListener().onNoClicked();
                }
                setNeverShowAgain(context, true);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (sConfig.getListener() != null) {
                    sConfig.getListener().onCancelClicked();
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
     * @param neverShowAgain
     */
    public void setNeverShowAgain(final Context context, boolean neverShowAgain) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(NEVER_SHOW_AGAIN, neverShowAgain);
        editor.commit();
        mNeverShowAgain = neverShowAgain;
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
        log("Opt out: " + pref.getBoolean(NEVER_SHOW_AGAIN, false));
    }

    /**
     * Print log if enabled
     * @param message
     */
    private void log(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    // call this method if you want users to rate your app
    public static void rateUs(Context context) {
        PlayStoreUtil.rateUs(context);
    }

    private boolean checkContext(Context context) {
        if (context == null) {
            return false;
        } else if (context instanceof Activity && isActivityFinishingOrDestroyed((Activity) context)) {
            return false;
        }
        return true;
    }

    private boolean isActivityFinishingOrDestroyed(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return activity.isDestroyed() || activity.isFinishing();
        } else {
            return activity.isFinishing();
        }
    }
}
