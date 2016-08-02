package com.noelchew.ncapprating;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.noelchew.ncappfeedback.library.NcAppFeedback;
import com.noelchew.ncappfeedback.library.NcAppFeedbackListener;
import com.noelchew.ncapprating.library.NcAppRating;
import com.noelchew.ncapprating.library.NcAppRatingConfig;
import com.noelchew.ncapprating.library.NcAppRatingListener;
import com.noelchew.ncapprating.library.PlayStoreUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int INSTALLED_DAYS = 0;
    private static final int LAUNCHED_TIMES = 0;
    private static final int MINIMUM_TARGET_RATING = 4;

    private static final String SPARKPOST_API_KEY = "insert_your_sparkpost_api_key_here";
    private static final String SENDER_EMAIL = "sender@sparkpost.com";
    private static final String SENDER_NAME = "NcAppFeedback Demo User";
    private static final String RECIPIENT_EMAIL = "your_email@gmail.com";

    private Context context;
    private Button btnRateUs;
    private NcAppRating ncAppRating;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;

        Button btnRateUs = (Button) findViewById(R.id.button_request_rating);

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.nc_utils_feedback_loading);
        progressDialog.setMessage(getString(R.string.nc_utils_feedback_please_wait));

        NcAppRatingConfig config = new NcAppRatingConfig(INSTALLED_DAYS, LAUNCHED_TIMES, MINIMUM_TARGET_RATING, ncAppRatingListener);
        ncAppRating = new NcAppRating(context, config);
        ncAppRating.showRateDialogIfNeeded();

        btnRateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ncAppRating.showRateDialog(context);
            }
        });
    }

    protected NcAppRatingListener ncAppRatingListener = new NcAppRatingListener() {
        @Override
        public void onOpenMarket(int rating) {
            Log.d(TAG, "NcAppRating - onOpenMarket() rating: " + String.valueOf(rating));
            PlayStoreUtil.rateUs(context);
        }

        @Override
        public void onNoClicked() {
            Log.d(TAG, "NcAppRating - onNoClicked()");
        }

        @Override
        public void onCancelClicked() {
            Log.d(TAG, "NcAppRating - onCancelClicked()");
        }

        @Override
        public void onShowFeedbackDialog(int rating) {
            Log.d(TAG, "NcAppRating - onShowFeedbackDialog() rating: " + String.valueOf(rating));
            // TODO: implement your own feedback mechanism here

            // or use simple email api to send feedback
            NcAppFeedback.feedbackWithBadRating(context,
                    SPARKPOST_API_KEY,
                    SENDER_EMAIL,
                    SENDER_NAME,
                    RECIPIENT_EMAIL,
                    new NcAppFeedbackListener() {
                        @Override
                        public void onFeedbackAnonymouslySuccess() {
                            Toast.makeText(context, "onFeedbackAnonymouslySuccess()", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFeedbackNonAnonymouslySuccess(String senderEmail) {
                            Toast.makeText(context, "onFeedbackNonAnonymouslySuccess() senderEmail: " + senderEmail, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFeedbackAnonymouslyError(String error) {
                            Toast.makeText(context, "onFeedbackAnonymouslyError() error: " + error, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFeedbackViaPhoneEmailClient() {
                            Toast.makeText(context, "onFeedbackViaPhoneEmailClient()", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(context, "onError() error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    },
                    progressDialog,
                    true);

        }
    };
}
