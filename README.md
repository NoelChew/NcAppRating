# NcAppRating
Android App Rating Library

####Prompts rating dialog when prompting criteria are met.

![screenshot1](https://github.com/NoelChew/NcAppRating/blob/master/screenshot_1.png)
![screenshot2](https://github.com/NoelChew/NcAppRating/blob/master/screenshot_2.png)


####Preferred rating to redirect to Google Play can be set.

For example:

4 Star is set as the target rating. If user selects 3 Star or below,  a "feedback" callback will be triggered.

![screenshot3](https://github.com/NoelChew/NcAppRating/blob/master/screenshot_3.png)
![screenshot4](https://github.com/NoelChew/NcAppRating/blob/master/screenshot_4.png)


## How to Use
In onCreate() of MainActivity:
```
NcAppRatingConfig config = new NcAppRatingConfig(INSTALLED_DAYS, LAUNCHED_TIMES, MINIMUM_TARGET_RATING, ncAppRatingListener);
ncAppRating = new NcAppRating(context, config);
ncAppRating.showRateDialogIfNeeded();
```

Declare an NcAppRatingListener

```
protected NcAppRatingListener ncAppRatingListener = new NcAppRatingListener() {
        @Override
        public void onOpenMarket(int rating) {
            Log.d(TAG, "NcAppRating - onOpenMarket() rating: " + String.valueOf(rating));
            
            // opens Google Play
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
        }
    };
```

## Integration
This library is hosted by jitpack.io.

Root level gradle:
```
allprojects {
 repositories {
    jcenter()
    maven { url "https://jitpack.io" }
 }
}
```

Application level gradle:
```
dependencies {
    compile 'com.github.noelchew:NcAppRating:0.1.3'
}
```
Note: do not add the jitpack.io repository under buildscript
