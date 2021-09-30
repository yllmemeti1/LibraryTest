package com.example.testlibrary;

/**
 * Created by Yll Memeti on 9/30/2021.
 */
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.StatFs;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


public class Utils {

    public static boolean isDEBUG = false;
    public static HashMap<String, Boolean> userSettings = new HashMap<>();
    public static HashMap<Integer, Float> watchedVideos = new HashMap<>();
    public static boolean isSharingDialogOpening = false;

    /**
     * How to use:
     * drawable.setColorFilter(new ColorMatrixColorFilter(Utils.INVERT_COLORS));
     */
    public static final float[] INVERT_COLORS = {
            -1.0f, 0, 0, 0, 255, // red
            0, -1.0f, 0, 0, 255, // green
            0, 0, -1.0f, 0, 255, // blue
            0, 0, 0, 1.0f, 0  // alpha
    };

    public enum EditTextFocusStates {
        NON_FOCUSED, FOCUSED
    }

    public interface EditTextFocusDelegate {
        void onFocusChanged(EditTextFocusStates state);
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setupUI(Activity activity, View view, @Nullable EditTextFocusDelegate delegate) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideKeyboard(activity);

                if (delegate != null) {
                    delegate.onFocusChanged(EditTextFocusStates.NON_FOCUSED);
                }
                return false;
            });
        } else if (view instanceof EditText) {
            view.setOnTouchListener((v, event) -> {
                if (delegate != null) {
                    delegate.onFocusChanged(EditTextFocusStates.FOCUSED);
                }
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(activity, innerView, delegate);
            }
        }
    }

    public static void invertColors(Drawable drawable) {
        drawable.setColorFilter(new ColorMatrixColorFilter(Utils.INVERT_COLORS));
    }

    public static boolean isFragmentInBackStack(final FragmentManager fragmentManager, final String fragmentTagName) {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            return fragmentTagName.equals(fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName());
        }
        return false;
    }

    public static String getCurrentVersion(Activity activity) {
        String currentVersion = "";
        try {
            currentVersion = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentVersion;
    }

    public static String getDeviceModel() {
        try {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
                for (int i = 0, length = model.length(); i < length; i++) {
                    char c = model.charAt(i);
                    if (c <= '\u001f' || c >= '\u007f') {
                        model = model.replace(c, ' ');
                    }
                }
                return capitalize(removeUnexpectedChars(model));
            } else {
                return capitalize(removeUnexpectedChars(manufacturer)) + " " + removeUnexpectedChars(model);
            }
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void setActivityOrientation(Activity activity,Boolean isTablet) {
        if (activity != null) {
            if (isTablet) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    public static boolean isTablet(Context context) {
        if (context == null)
            return false;
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static String getDeviceID(Context c) {
        try {
            return Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * This method will remove any unexpected characters to prevent Header on User-Agent crashing by
     * replacing char with empty char.
     */
    public static String removeUnexpectedChars(String textToRemoveChars) {
        for (int i = 0, length = textToRemoveChars.length(); i < length; i++) {
            char c = textToRemoveChars.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                textToRemoveChars = textToRemoveChars.replace(c, ' ');
            }
        }
        return textToRemoveChars;
    }

    public static Uri getImageUri(String path) {
        return Uri.fromFile(new File(path));
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String getUserAgent() {
        try {
            return "AndroidApp; " + Utils.getDeviceModel() + "; " + android.os.Build.VERSION.RELEASE;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * @return true if there's network available and it's not null.
     */
    public static boolean isNetworkConnected(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null;
    }


//
//
//
//    @SuppressLint("RestrictedApi")
//    public static void disableShiftMode(BottomNavigationView view) {
//        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
//        try {
//            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
//            shiftingMode.setAccessible(true);
//            shiftingMode.setBoolean(menuView, false);
//            shiftingMode.setAccessible(false);
//            for (int i = 0; i < menuView.getChildCount(); i++) {
//                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
//                item.setShifting(false);
//                item.setChecked(item.getItemData().isChecked());
//            }
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void shareLink(final Activity activity, String videoUrl) {
//        if (activity == null)
//            return;
//
//        try {
//            Utils.isSharingDialogOpening = true;
//
//            if (videoUrl.contains("%")) {
//                videoUrl = java.net.URLDecoder.decode(videoUrl, "UTF-8");
//            }
//            List<Intent> targets = new ArrayList<>();
//            Intent template = new Intent(Intent.ACTION_SEND);
//            template.setType("text/plain");
//            List<ResolveInfo> candidates = activity.getPackageManager().
//                    queryIntentActivities(template, 0);
//
//            // remove facebook which has a broken share intent
//            for (ResolveInfo candidate : candidates) {
//                String packageName = candidate.activityInfo.packageName;
//                if (!packageName.equals("com.facebook.katana")) {
//                    Intent target = new Intent(android.content.Intent.ACTION_SEND);
//                    target.setType("text/plain");
//                    target.putExtra(Intent.EXTRA_TEXT, videoUrl);
//                    target.setPackage(packageName);
//                    targets.add(target);
//                    break;
//                }
//            }
//            Intent fbIntent = new Intent(activity, ShareToFacebookActivity.class);
//            fbIntent.setData(Uri.parse(videoUrl));
//            fbIntent.putExtra(Intent.EXTRA_TEXT,"Facebook");
//
//            Intent copyIntent = new Intent(activity, CopyToClipboardActivity.class);
//            copyIntent.setData(Uri.parse(videoUrl));
//
//            if (targets.size() > 0) {
//                targets.add(1, fbIntent);
//                targets.add(2, copyIntent);
//            }
//
//            Intent chooser = Intent.createChooser(targets.remove(0), "Share");
//            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targets.toArray(new Parcelable[]{}));
//            activity.startActivityForResult(chooser, 9999);
//        } catch (Exception e) {
//            Utils.isSharingDialogOpening = false;
//            e.printStackTrace();
//        }
//    }
//
//    public static void showKeyboard(Context context) {
//        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//    }
//
//    public static boolean isKeyboardShown(View rootView) {
//        /* 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard */
//        final int SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128;
//
//        Rect r = new Rect();
//        rootView.getWindowVisibleDisplayFrame(r);
//        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
//        /* heightDiff = rootView height - status bar height (r.top) - visible frame height (r.bottom - r.top) */
//        int heightDiff = rootView.getBottom() - r.bottom;
//        /* Threshold size: dp to pixels, multiply with display density */
//        boolean isKeyboardShown = heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density;
//
////        Log.d("TAG", "isKeyboardShown ? " + isKeyboardShown + ", heightDiff:" + heightDiff + ", density:" + dm.density
////                + "root view height:" + rootView.getHeight() + ", rect:" + r);
//
//        return isKeyboardShown;
//    }
//
    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
//
//    public static void hideSearchKeyboard(Activity activity) {
//        if (activity == null)
//            return;
//
//        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
//    }
//
//    private static String pattern = "yyyy-MM-dd HH:mm:ss.SS";
//
//    public static String getCurrentTime() {
//        try {
//            Date d = new Date();
//            SimpleDateFormat sdf = new SimpleDateFormat(Utils.pattern);
//            return sdf.format(d);
//        } catch (Exception e) {
//            return "";
//        }
//    }
//
//    private static final long ONE_MINUTE_IN_MILLIS = 60000;
//
//    public static String getCurrentTimeWithAddition(int addition) {
//        Calendar date = Calendar.getInstance();
//        long t = date.getTimeInMillis();
//        Date afterAddingTime = new Date(t + (addition * (24 * 60 * Utils.ONE_MINUTE_IN_MILLIS)));
//        SimpleDateFormat sdf = new SimpleDateFormat(Utils.pattern);
//        return sdf.format(afterAddingTime);
//    }
//
//    public static void openChromeWebView(Activity activity, String url) {
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setPackage("com.android.chrome");
//
//        try {
//            activity.startActivity(intent);
//        } catch (ActivityNotFoundException ex) {
//            intent.setPackage(null);
//            try {
//                activity.startActivity(intent);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    public static void collapse(final View v) {
//        final int initialHeight = v.getMeasuredHeight();
//
//        Animation a = new Animation() {
//            @Override
//            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                if (interpolatedTime == 1) {
//                    v.setVisibility(View.GONE);
//                } else {
//                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
//                    v.requestLayout();
//                }
//            }
//
//            @Override
//            public boolean willChangeBounds() {
//                return true;
//            }
//        };
//
//        // Collapse speed of 1dp/ms
//        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
//        v.startAnimation(a);
//    }
//
//    public static void expand(final View v) {
//        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
//        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
//        final int targetHeight = v.getMeasuredHeight();
//
//        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
//        v.getLayoutParams().height = 1;
//        v.setVisibility(View.VISIBLE);
//        Animation a = new Animation() {
//            @Override
//            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                v.getLayoutParams().height = interpolatedTime == 1
//                        ? LinearLayout.LayoutParams.WRAP_CONTENT
//                        : (int) (targetHeight * interpolatedTime);
//                v.requestLayout();
//            }
//
//            @Override
//            public boolean willChangeBounds() {
//                return true;
//            }
//        };
//
//        // Expansion speed of 1dp/ms
//        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
//        v.startAnimation(a);
//    }
//
//
//    public static void bringView2Front(View xView) {
//        xView.bringToFront();
//        xView.invalidate();
//        xView.requestLayout();
//    }
//
//    public static boolean changeLanguage(Context context, LanguageSwitchModel languageSwitchModel, Localizations localizationsX) {
//        try {
//            String languageSerialized = new Gson().toJson(languageSwitchModel, LanguageSwitchModel.class);
//            new StorageUtil(context).storeSelectedLanguage(languageSerialized);
//            Utils.localizations = localizationsX;
//            ApiHelper.retrofit = null;
//            ApiHelper.retrofitlogin = null;
//            ApiHelper.retrofitSSO = null;
//            Constants.homepageModel = null;
//            Constants.liveSections = null;
//            Constants.legalModel = null;
//            Constants.showsSectionModel = null;
//            Constants.userProfileModel = null;
//            String localizationsSerialized = new Gson().toJson(localizationsX, Localizations.class);
//            new StorageUtil(context).storeLocalizations(localizationsSerialized);
//            // Updating current target value
//            Constants.setupAppChanges(context);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static String getNetworkType(Activity activity) {
//        try {
//            final ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
//            final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//            final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//            if (wifi.isConnectedOrConnecting()) {
//                return "wifi";
//            } else if (mobile.isConnectedOrConnecting()) {
//                return "data";
//            } else {
//                return "none";
//            }
//        } catch (Exception e) {
//            return "none";
//        }
//    }
//
//    public interface AsyncTaskInterface {
//        void doInBackground();
//    }
//
//    public static void createAsyncTask(final AsyncTaskInterface asyncTaskInterface) {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//            }
//
//            @Override
//            protected void onProgressUpdate(Void... values) {
//                super.onProgressUpdate(values);
//            }
//
//            @Override
//            protected void onCancelled(Void aVoid) {
//                super.onCancelled(aVoid);
//            }
//
//            @Override
//            protected void onCancelled() {
//                super.onCancelled();
//            }
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                asyncTaskInterface.doInBackground();
//                return null;
//            }
//        }.execute();
//    }
//
//    public interface SignalRInterface {
//        void doInBackground();
//
//        void onPostExecute();
//    }
//
//    public static void createAsyncWithPostExec(final SignalRInterface signalRInterface) {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                signalRInterface.onPostExecute();
//            }
//
//            @Override
//            protected void onProgressUpdate(Void... values) {
//                super.onProgressUpdate(values);
//            }
//
//            @Override
//            protected void onCancelled(Void aVoid) {
//                super.onCancelled(aVoid);
//            }
//
//            @Override
//            protected void onCancelled() {
//                super.onCancelled();
//            }
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                signalRInterface.doInBackground();
//                return null;
//            }
//        }.execute();
//    }
//
//    public interface TVSignalRInterface {
//        void doInBackground();
//    }
//
//    public static void sendTVMethodAsync(final TVSignalRInterface listener) {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                listener.doInBackground();
//                return null;
//            }
//        }.execute();
//    }
//
//
//    /**
//     * A method that returns current installed version name on installed device. If this method goes
//     * on catch instead of returning version name then it will return just an empty string. ->
//     * Sample:  version = "";
//     */
//    public static String getVersionName(@Nullable Activity activity) {
//        String version = "";
//        try {
//            version = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return version == null ? "" : version;
//    }
//
//    /**
//     * Optional Alert Dialog - create custom optional dialog with desired title, message and other
//     * params. Any params that have @Nullable can be set to null when executed and won't be any
//     * problem.
//     *
//     * @param context                 - Class context.R
//     * @param alertDialogTitle        - Alert Dialog title.  -> (Title can be null)
//     * @param alertDialogMessage      - Alert Dialog message.
//     * @param isAlertDialogCancelable - Set to true if you want dialog to be cancelable or false
//     *                                that you can't cancel dialog at any way.
//     * @param positiveText            - Alert Dialog positive button text.
//     * @param positiveListener        - Alert Dialog positive button listener.
//     * @param negativeText            - Alert Dialog negative button text.  -> (Negative button text
//     *                                can be null)
//     * @param negativeListener        - Alert Dialog negative button listener.  -> (Negative button
//     *                                listener can be null)
//     * @param neutralText             - Alert Dialog neutral button text.  -> (Netural button text
//     *                                can be null)
//     * @param neutralListener         - Alert Dialog neutral button listener.  -> (Neutral button
//     *                                listener can be null)
//     */
//    public static void createAlertDialog(Context context,
//                                         @Nullable String alertDialogTitle, String alertDialogMessage, boolean isAlertDialogCancelable,
//                                         String positiveText, DialogInterface.OnClickListener positiveListener,
//                                         @Nullable String negativeText, @Nullable DialogInterface.OnClickListener negativeListener,
//                                         @Nullable String neutralText, @Nullable DialogInterface.OnClickListener neutralListener) {
//
//        final AlertDialog.Builder builder;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
//        } else {
//            builder = new AlertDialog.Builder(context);
//        }
//
//        builder.setTitle(alertDialogTitle)
//                .setMessage(alertDialogMessage)
//                .setCancelable(isAlertDialogCancelable)
//                .setPositiveButton(positiveText, positiveListener)
//                .setNegativeButton(negativeText, negativeListener)
//                .setNeutralButton(neutralText, neutralListener)
//                .show();
//    }
//
//    /**
//     * Optional slide left layout for any desired view while full screen. This view will slide from
//     * right to -width size of view to 0 fromXDelta position;
//     *
//     * @param view        - Selected view to animate.
//     * @param duration    - Duration for animation to run.
//     * @param onAnimation - Animation listener -> (Can be set to null)
//     */
//    public static void slideLeftFullScreen(final View view, int duration, @Nullable final Animation.AnimationListener onAnimation) {
//        TranslateAnimation animate = new TranslateAnimation(0, -view.getWidth(), 0, 0);
//        animate.setDuration(duration);
//        animate.setAnimationListener(onAnimation);
//        view.startAnimation(animate);
//    }
//
//
//    public static boolean isViewVisibleInScreen(View view, CustomScrollView nestedScroll) {
//        Rect scrollBounds = new Rect();
//        nestedScroll.getDrawingRect(scrollBounds);
//        int viewBottom = view.getBottom();
//        int scrollBottom = scrollBounds.bottom;
//        return viewBottom < scrollBottom;
//    }
//
//    /**
//     * Optional slide right layout for any desired view while full screen. This view will slide from
//     * -width size of view to currently positioned on view at 0 toXDelta position.
//     *
//     * @param view        - Selected view to animate.
//     * @param duration    - Duration for animation to run.
//     * @param onAnimation - Animation listener -> (Can be set to null)
//     */
//    public static void slideRightFullScreen(final View view, int duration, @Nullable final Animation.AnimationListener onAnimation) {
//        TranslateAnimation animate = new TranslateAnimation(-view.getWidth(), 0, 0, 0);
//        animate.setDuration(duration);
//        animate.setAnimationListener(onAnimation);
//        view.startAnimation(animate);
//    }
//
//    /**
//     * Optional animation to enter a view from the right side
//     *
//     * @param context     Class context.
//     * @param view        Desired view to add animation
//     * @param onAnimation Optional animation interface -> (Can be set to null)
//     */
//    public static void enterViewFromRight(Context context, final View view, @Nullable final Animation.AnimationListener onAnimation) {
//        Animation enterFromRight = AnimationUtils.loadAnimation(context, R.anim.enter_from_right);
//        enterFromRight.setAnimationListener(onAnimation);
//        view.startAnimation(enterFromRight);
//    }
//
//    /**
//     * Optional animation to enter a view from the left side
//     *
//     * @param context     Class context.
//     * @param view        Desired view to add animation
//     * @param onAnimation Optional animation interface -> (Can be set to null)
//     */
//    public static void enterViewFromLeft(Context context, final View view, @Nullable final Animation.AnimationListener onAnimation) {
//        Animation enterFromLeft = AnimationUtils.loadAnimation(context, R.anim.enter_from_left);
//        enterFromLeft.setAnimationListener(onAnimation);
//        view.startAnimation(enterFromLeft);
//    }
//
//    /**
//     * Optional animation to exit a view from the right side
//     *
//     * @param context     Class context.
//     * @param view        Desired view to add animation
//     * @param onAnimation Optional animation interface -> (Can be set to null)
//     */
//    public static void exitViewFromRight(Context context, final View view, @Nullable final Animation.AnimationListener onAnimation) {
//        Animation exitToRight = AnimationUtils.loadAnimation(context, R.anim.exit_to_right);
//        exitToRight.setAnimationListener(onAnimation);
//        view.startAnimation(exitToRight);
//    }
//
//    /**
//     * Optional animation to exit a view from the left side
//     *
//     * @param context     Class context.
//     * @param view        Desired view to add animation
//     * @param onAnimation Optional animation interface -> (Can be set to null)
//     */
//    public static void exitViewFromleft(Context context, final View view, @Nullable final Animation.AnimationListener onAnimation) {
//        Animation exitToLeft = AnimationUtils.loadAnimation(context, R.anim.exit_to_left);
//        exitToLeft.setAnimationListener(onAnimation);
//        view.startAnimation(exitToLeft);
//    }
//
//    /**
//     * Set multiple views to visible, gone or invisible at one time.
//     *
//     * @param type - Selected case: V for VISIBLE, G for GONE and I for INVISIBLE.
//     * @param v    - You can send 1 or more views to this param.
//     */
//    public static void setViewsVisibility(String type, View... v) {
//        for (View myView : v) {
//            switch (type.toUpperCase()) {
//                case "G":
//                    myView.setVisibility(View.GONE);
//                    break;
//                case "V":
//                    myView.setVisibility(View.VISIBLE);
//                    break;
//                case "I":
//                    myView.setVisibility(View.INVISIBLE);
//                    break;
//            }
//        }
//    }
//
//    /**
//     * Set custom text change listener for EditText-s
//     *
//     * @param editText          - Desired EditText to add text changer to
//     * @param beforeTextChanged - BeforeTextChanged Listener
//     * @param onTextChanged     - OnTextChanged Listener
//     * @param afterTextChanged  - AfterTextChanged Listener
//     */
//    public static void setTextChangeListener(EditText editText, @Nullable final ApiInterface.beforeTextChanged beforeTextChanged
//            , @Nullable final ApiInterface.onTextChanged onTextChanged
//            , @Nullable final ApiInterface.afterTextChanged afterTextChanged) {
//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if (beforeTextChanged != null)
//                    beforeTextChanged.beforeTextChanged(s, start, count, after);
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (onTextChanged != null)
//                    onTextChanged.onTextChanged(s, start, before, count);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (afterTextChanged != null)
//                    afterTextChanged.afterTextChanged(s);
//            }
//        });
//    }
//
//    /**
//     * Set custom text change listener for TextView-s
//     *
//     * @param textView          - Desired TextView to add text changer to
//     * @param beforeTextChanged - BeforeTextChanged Listener
//     * @param onTextChanged     - OnTextChanged Listener
//     * @param afterTextChanged  - AfterTextChanged Listener
//     */
//    public static void setTextChangeListener(TextView textView, @Nullable final ApiInterface.beforeTextChanged beforeTextChanged
//            , @Nullable final ApiInterface.onTextChanged onTextChanged
//            , @Nullable final ApiInterface.afterTextChanged afterTextChanged) {
//        textView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if (beforeTextChanged != null)
//                    beforeTextChanged.beforeTextChanged(s, start, count, after);
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (onTextChanged != null)
//                    onTextChanged.onTextChanged(s, start, before, count);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (afterTextChanged != null)
//                    afterTextChanged.afterTextChanged(s);
//            }
//        });
//    }
//
//    /**
//     * Enter from right animation
//     *
//     * @param context     - Class context
//     * @param view        - View to add animation to
//     * @param onAnimation - Animation listener interface
//     */
//    public static void enterFromRight(Context context, View view, @Nullable final Animation.AnimationListener onAnimation) {
//        if (context == null)
//            return;
//
//        Animation animation = AnimationUtils.loadAnimation(context, R.anim.enter_from_right);
//        animation.setAnimationListener(onAnimation);
//        view.startAnimation(animation);
//    }
//
//    /**
//     * Exit to right animation
//     *
//     * @param context     - Class context
//     * @param view        - View to add animation to
//     * @param onAnimation - Animation listener interface
//     */
//    public static void exitToRight(Context context, View view, @Nullable final Animation.AnimationListener onAnimation) {
//        if (context == null)
//            return;
//
//        Animation anitmation = AnimationUtils.loadAnimation(context, R.anim.exit_to_right);
//        anitmation.setAnimationListener(onAnimation);
//        view.startAnimation(anitmation);
//    }
//
//    /**
//     * Load layoutToInflate into LinearLayout, in this case we're using it for blur layout at
//     * Sensitive/Explicit content whether to load blur layout or black view layout on full screen
//     * This method can be used in other purposes such as to call Alexa web view. This prevents
//     * crashes for several devices and it makes a better performance when using blur because it
//     * loads only when adding layout to view.
//     *
//     * @param activity     - Activity method is used
//     * @param linearLayout - Linear layout to add blur background to
//     */
//    public static void loadBlurLayout(Activity activity, LinearLayout linearLayout, int layoutToInflate) {
//        try {
//            LayoutInflater inflater = LayoutInflater.from(activity);
//            View view = inflater.inflate(layoutToInflate, null);
//            linearLayout.addView(view, new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Remove blur view from selected linear layout
//     *
//     * @param linearLayout - Linear layout to remove blur from
//     */
//    public static void removeBlurLayout(LinearLayout linearLayout) {
//        try {
//            linearLayout.removeAllViews();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void setTextNotNullOrEmpty(TextView textView, String textToCheck) {
//        if (textToCheck != null && !textToCheck.isEmpty())
//            textView.setText(textToCheck);
//    }
//
//    /**
//     * This method will check if textToCheck is empty or null, in case it's null or empty will
//     * return true otherwise returns false
//     */
//    public static boolean isTextNullOrEmpty(String textToCheck) {
//        return textToCheck == null || textToCheck.isEmpty();
//    }
//
//    /**
//     * Destroy multiple AdViews at once
//     *
//     * @param adViews - adViews to destroy.
//     */
//    public static void destroyAdView(AdView... adViews) {
//        if (Constants.target.getCurrentAppTarget() != AppTarget.GJIRAFAVIDEO) return;
//
///*        for (AdView ad : adViews)
//            if (ad != null)
//                ad.destroy();*/
//    }
//
//    /**
//     * Resume multiple AdViews at once
//     *
//     * @param adViews - adViews to resume.
//     */
//    public static void resumeAdView(AdView... adViews) {
//        if (Constants.target.getCurrentAppTarget() != AppTarget.GJIRAFAVIDEO) return;
//
//        for (AdView adView : adViews)
//            if (adView != null)
//                adView.resume();
//    }
//
//    /**
//     * Pause multiple AdViews at once
//     *
//     * @param adViews - adViews to pause
//     */
//    public static void pauseAdView(AdView... adViews) {
//        if (Constants.target.getCurrentAppTarget() != AppTarget.GJIRAFAVIDEO) return;
//
//        for (AdView adView : adViews)
//            if (adView != null)
//                adView.pause();
//    }
//
//    /**
//     * Toast custom short message.
//     *
//     * @param context   - Class context
//     * @param textToast - Message to show at user
//     */
//    public static void toastMsgShort(Context context, String textToast) {
//        if (context == null)
//            return;
//
//        Toast.makeText(context, textToast, Toast.LENGTH_SHORT).show();
//    }
//
//    public static void toastMsgShortOnMainThread(Context context, String text) {
//        if (context == null)
//            return;
//
//        try {
//            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
//        } catch (Exception e) {
//            toastMsgShort(context, text);
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Toast custom long message.
//     *
//     * @param context   - Class context
//     * @param textToast - Messge to show at user
//     */
//    public static void toastMsgLong(Context context, String textToast) {
//        if (context == null)
//            return;
//
//        Toast.makeText(context, textToast, Toast.LENGTH_LONG).show();
//    }
//
//    /**
//     * Check if string text is empty or is null
//     *
//     * @return false if given string is null or empty otherwise if text is not null and empty
//     * returns true
//     */
//    public static boolean isStringNotNull(String text) {
//        try {
//            return text != null && !text.trim().equals("");
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    /**
//     * Parse a string to date with a desired format
//     *
//     * @param dateTxt    - String to parse at date
//     * @param formatDate - Desire format
//     * @return - Date from string
//     */
//    public static Date parseStringToDate(String dateTxt, String formatDate) {
//        try {
//            SimpleDateFormat format = new SimpleDateFormat(formatDate);
//            return format.parse(dateTxt);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    /**
//     * Check if localization text in Utils.localization class is empty or null
//     *
//     * @return true if text it's not null and it's not empty
//     */
//    public static boolean isLocalizationNotNullAndEmpty(String localizationTxt) {
//        if (Utils.localizations == null)
//            return false;
//        return localizationTxt != null && !localizationTxt.isEmpty();
//    }
//
//    /**
//     * Set localization text to a text view only if the localization it's not null or empty
//     *
//     * @param textView         - TextView to add text
//     * @param localizationText - Localization text from model
//     */
//    public static void setLocalizationText(TextView textView, String localizationText, @Nullable String optionalText) {
//        if (isLocalizationNotNullAndEmpty(localizationText))
//            if (optionalText != null)
//                textView.setText(localizationText + optionalText);
//            else
//                textView.setText(localizationText);
//    }
//
//    /**
//     * Set localization text to a text view only if the localization it's not null or empty
//     *
//     * @param button           - Button to add text
//     * @param localizationText - Localization text to add from model
//     */
//    @SuppressLint("SetTextI18n")
//    public static void setLocalizationText(Button button, String localizationText, @Nullable String optionalText) {
//        if (isLocalizationNotNullAndEmpty(localizationText))
//            if (optionalText != null)
//                button.setText(localizationText + optionalText);
//            else
//                button.setText(localizationText);
//    }
//
//    /**
//     * Set localization text to a text input layout only if the localization it's not null or empty
//     *
//     * @param textInputLayout  - TextInputLayout to add text
//     * @param localizationText - Localization text to add from model
//     */
//    @SuppressLint("SetTextI18n")
//    public static void setLocalizationText(TextInputLayout textInputLayout, String localizationText, @Nullable String optionalText) {
//        if (isLocalizationNotNullAndEmpty(localizationText))
//            if (optionalText != null)
//                textInputLayout.setHint(localizationText + optionalText);
//            else
//                textInputLayout.setHint(localizationText);
//    }
//
//    /**
//     * Set localization text to a text view only if the localization it's not null or empty
//     *
//     * @param editText         - EditText to add text
//     * @param localizationText - Localization text to add from model
//     */
//    @SuppressLint("SetTextI18n")
//    public static void setLocalizationText(EditText editText, String localizationText, @Nullable String optionalText) {
//        if (isLocalizationNotNullAndEmpty(localizationText))
//            if (optionalText != null)
//                editText.setText(localizationText + optionalText);
//            else
//                editText.setText(localizationText);
//    }
//
//    /**
//     * Set localization text to a text view only if the localization it's not null or empty
//     *
//     * @param textInputLayout  - TextInputLayout to add text
//     * @param localizationText - Localization text to add from model
//     */
//    public static void setLocalizationTextHint(TextInputLayout textInputLayout, String localizationText, @Nullable String optionalText) {
//        if (isLocalizationNotNullAndEmpty(localizationText))
//            if (optionalText != null)
//                textInputLayout.setHint(localizationText + optionalText);
//            else
//                textInputLayout.setHint(localizationText);
//    }
//
//    /**
//     * Set localization text to a text view only if the localization it's not null or empty
//     *
//     * @param editText         - EditText to add text
//     * @param localizationText - Localization text to add from model
//     */
//    public static void setLocalizationTextHint(EditText editText, String localizationText, @Nullable String optionalText) {
//        if (isLocalizationNotNullAndEmpty(localizationText))
//            if (optionalText != null)
//                editText.setHint(localizationText + optionalText);
//            else
//                editText.setHint(localizationText);
//    }
//
//    /**
//     * Fade in any view programmatically with a desired duration.
//     *
//     * @param view     - View which you want to add animation to
//     * @param duration - Duration for how long animation to last
//     */
//    public static void fadeInView(View view, int duration) {
//        Utils.setViewsVisibility("I", view);
//        Animation fadeIn = new AlphaAnimation(0, 1);
//        fadeIn.setInterpolator(new AccelerateInterpolator());
//        fadeIn.setDuration(duration);
//        fadeIn.setAnimationListener(new Animation.AnimationListener() {
//            public void onAnimationEnd(Animation animation) {
//                Utils.setViewsVisibility("V", view);
//            }
//
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            public void onAnimationStart(Animation animation) {
//            }
//        });
//        view.startAnimation(fadeIn);
//    }
//
//
//    public static void fadeInPlayer(View view) {
//        Animation fadeIn = new AlphaAnimation(0, 1);
//        fadeIn.setInterpolator(new AccelerateInterpolator());
//        fadeIn.setDuration(600);
//        fadeIn.setAnimationListener(new Animation.AnimationListener() {
//            public void onAnimationEnd(Animation animation) {
//                Utils.setViewsVisibility(View.VISIBLE, view);
//            }
//
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            public void onAnimationStart(Animation animation) {
//            }
//        });
//        view.startAnimation(fadeIn);
//    }
//
//
//    /**
//     * Fade out any view programmatically with a desired duration/timer
//     *
//     * @param view     - View which you want to add animation to
//     * @param duration - Duration for how long animation to last
//     */
//    public static void fadeOutView(View view, int duration) {
//        Utils.setViewsVisibility("V", view);
//        Animation fadeIn = new AlphaAnimation(1, 0);
//        fadeIn.setInterpolator(new AccelerateInterpolator());
//        fadeIn.setDuration(duration);
//        fadeIn.setAnimationListener(new Animation.AnimationListener() {
//            public void onAnimationEnd(Animation animation) {
//                Utils.setViewsVisibility("I", view);
//            }
//
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            public void onAnimationStart(Animation animation) {
//            }
//        });
//        view.startAnimation(fadeIn);
//    }
//
//
//    /**
//     * Fade out any view programmatically with a desired duration/timer
//     *
//     * @param view     - View which you want to add animation to
//     * @param duration - Duration for how long animation to last
//     */
//    public static void fadeOutViewGONE(View view, int duration) {
//        Utils.setViewsVisibility("V", view);
//        Animation fadeIn = new AlphaAnimation(1, 0);
//        fadeIn.setInterpolator(new AccelerateInterpolator());
//        fadeIn.setDuration(duration);
//        fadeIn.setAnimationListener(new Animation.AnimationListener() {
//            public void onAnimationEnd(Animation animation) {
//                Utils.setViewsVisibility("G", view);
//            }
//
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            public void onAnimationStart(Animation animation) {
//            }
//        });
//        view.startAnimation(fadeIn);
//    }
//
//    public static void slideInQuiz(final Context context, View view, View quizHeader) {
//        Animation slide_in = AnimationUtils.loadAnimation(context, R.anim.slide_out);
//        slide_in.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                fadeOutViewGONE(quizHeader, 750);
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });
//
//        view.startAnimation(slide_in);
//        view.setVisibility(View.VISIBLE);
//    }
//
//    /**
//     * This helps you copy any text in clipboard with a desired label.
//     */
//    public static void copyTextToClipboard(Activity activity, String label, String textToCopy) {
//        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
//        ClipData clip = ClipData.newPlainText(label, textToCopy);
//        clipboard.setPrimaryClip(clip);
//    }
//
//    enum DetailsFragmentEnum {ShowDetailsFragment, ChannelDetailsFragment}
//
//    /**
//     * Open details fragments by only passing show or channel id as params
//     *
//     * @param activity             - MainActivity only.
//     * @param detailsFragmentClass - Detailed fragment class name -> Ex: ChannelDetailsFragment.class.getSimpleName();
//     * @param id                   - Id of show or channel
//     */
//    public static void openDetailsFragment(Activity activity, Class detailsFragmentClass, int id) {
//        DetailsFragmentEnum detailsFragment = DetailsFragmentEnum.valueOf(detailsFragmentClass.getSimpleName());
//        Bundle b = new Bundle();
//
//        switch (detailsFragment) {
//            case ShowDetailsFragment:
//                ShowDetailsFragment showDetailsFragment = new ShowDetailsFragment();
//                b.putInt("SHOW_ID", id);
//                showDetailsFragment.setArguments(b);
//                ((MainActivity) activity).addFragment(showDetailsFragment, ShowDetailsFragment.class.getSimpleName());
//                break;
//            case ChannelDetailsFragment:
//                ChannelDetailsFragment channelDetailsFragment = new ChannelDetailsFragment();
//                b.putInt("CHANNEL_ID", id);
//                channelDetailsFragment.setArguments(b);
//                ((MainActivity) activity).addFragment(channelDetailsFragment, ChannelDetailsFragment.class.getSimpleName());
//                break;
//        }
//    }
//
//    /**
//     * Set multiple views to visible, gone or invisible at one time.
//     *
//     * @param visibility - Selected case: V for VISIBLE, G for GONE and I for INVISIBLE.
//     * @param v          - You can send 1 or more views to this param.
//     */
//    public static void setViewsVisibility(int visibility, View... v) {
//        for (View myView : v) {
//            switch (visibility) {
//                case View.GONE:
//                    if (myView != null)
//                        myView.setVisibility(View.GONE);
//                    break;
//                case View.VISIBLE:
//                    if (myView != null)
//                        myView.setVisibility(View.VISIBLE);
//                    break;
//                case View.INVISIBLE:
//                    if (myView != null)
//                        myView.setVisibility(View.INVISIBLE);
//                    break;
//            }
//        }
//    }
//
//    /**
//     * Clear multiple edit texts at a time and their focus.
//     */
//    public static void clearTextAndFocus(EditText... editTexts) {
//        for (EditText et : editTexts) {
//            et.setText("");
//            et.clearFocus();
//        }
//    }
//
//    /**
//     * Clear multiple TextInputLayouts focus at the same time.
//     */
//    public static void clearFocus(TextInputLayout... textInputLayouts) {
//        for (TextInputLayout textInputLayout : textInputLayouts) {
//            textInputLayout.clearFocus();
//        }
//    }
//
//    public static boolean isValidEmail(String email) {
//        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
//        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
//        java.util.regex.Matcher m = p.matcher(email);
//        return m.matches();
//    }
//
//    public static boolean isValidText(String text) {
//        String ePattern = "^[a-zA-Z ]+$";
//        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
//        java.util.regex.Matcher m = p.matcher(text);
//        return m.matches() && !text.trim().contains(" ");
//    }
//
//    public static void setMargins(Context context, View view, int leftDP, int topDP, int rightDP, int bottomDP) {
//        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
//            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
//            p.setMargins(getPixelFromDP(context, leftDP), getPixelFromDP(context, topDP), getPixelFromDP(context, rightDP), getPixelFromDP(context, bottomDP));
//            view.requestLayout();
//        }
//    }
//
//    public static void setPadding(Context context, View view, int leftDP, int topDP, int rightDP, int bottomDP) {
//        view.setPadding(getPixelFromDP(context, leftDP), getPixelFromDP(context, topDP), getPixelFromDP(context, rightDP), getPixelFromDP(context, bottomDP));
//    }
//
//    public static int getPixelFromDP(Context context, int dp) {
//        float density = context.getResources().getDisplayMetrics().density;
//        return (int) (dp * density);
//    }
//
//    public static void bringViewToFront(View view) {
//        view.bringToFront();
//        view.invalidate();
//        view.requestLayout();
//    }
//
//    public static EntityModel getEntityModelFromVideoModel(VideoDetailsModel videoData) {
//        EntityModel entityModel = new EntityModel();
//        entityModel.setEntityId(videoData.getEntityId());
//        entityModel.setId(videoData.getVideoId());
//        entityModel.setTitle(videoData.getTitle());
//        entityModel.setThumbnailUrl(videoData.getThumbnailUrl());
//        entityModel.setThumbnail(videoData.getThumbnailUrl());
//        entityModel.setSerieName(videoData.getContent().getTitle());
//        entityModel.setPublishDate(videoData.getDate());
//        entityModel.setDuration(videoData.getDuration());
//        entityModel.setShareUrl(videoData.getShareUrl());
//        entityModel.setLockingStatus(videoData.getLockingStatus());
//        entityModel.setDownloadedPath(videoData.getVideoSource());
//        if (videoData.getExpiresInMinutes() > 0) {
//            entityModel.setDownloadedExpirationDate(getTimeWithAdditionMinutes(videoData.getExpiresInMinutes()));
//        }
//        return entityModel;
//    }
//
//    public static boolean hasOfflineDownloads() {
//        return OfflineHelper.getOfflineDownloads().size() > 0;
//    }
//
//    public static void clearOfflineDownloads() {
//        getDownloadContext().getDownloadTracker().clearDownloadedFiles();
//        OfflineHelper.clearOfflineDownloads();
//    }
//
//    public static ArrayList<EntityModel> getDownloadedFiles() {
//        ArrayList<EntityModel> entities = new ArrayList<>();
//
//        if (MallTVApplication.sharedContext == null || getDownloadContext().getDownloadTracker().getDownloads().size() == 0)
//            return entities;
//
//        HashMap<Uri, Download> downloads = getDownloadContext().getDownloadTracker().getDownloads();
//
//        for (Map.Entry<Uri, Download> entry : downloads.entrySet()) {
//            String keyUri = entry.getKey().toString();
//            if (OfflineHelper.getOfflineDownloads().containsKey(keyUri)) {
//                entities.add(OfflineHelper.getOfflineDownloads().get(keyUri));
//            }
//        }
//
//        return entities;
//    }
//
//    private static final long MEGABYTE = 1024L * 1024L;
//
//    public static String getFileDownloadSizeInMB(Context context, int entityId) {
//        String defaultFileSize = "0 MB";
//
//        if (context == null || MallTVApplication.sharedContext == null || getDownloadContext().getDownloadTracker().getDownloads().size() == 0)
//            return defaultFileSize;
//
//        HashMap<Uri, Download> downloads = getDownloadContext().getDownloadTracker().getDownloads();
//
//        try {
//            Uri uri = Uri.parse(OfflineHelper.getDownloadById(entityId).getDownloadedPath());
//
//            long downloadSizeInBytes = downloads.get(uri).getBytesDownloaded();
//            String sizeInStr = new DecimalFormat("##.#").format(downloadSizeInBytes / MEGABYTE);
//            return sizeInStr + " MB";
//
//        } catch (Exception E) {
//            return defaultFileSize;
//        }
//
//    }
//
//    public static double getDownloadedFilesSizeInGB(Context context) {
//        if (context == null || MallTVApplication.sharedContext == null || getDownloadContext().getDownloadTracker().getDownloads().size() == 0)
//            return 0;
//
//        double sumGB = 0;
//
//        HashMap<Uri, Download> downloads = getDownloadContext().getDownloadTracker().getDownloads();
//
//        try {
//            for (Map.Entry<Uri, Download> entry : downloads.entrySet()) {
//                sumGB += downloads.get(entry.getKey()).getBytesDownloaded();
//            }
//
//        } catch (Exception E) {
//            return 0;
//        }
//
//
//        sumGB = sumGB / (Math.pow(1024, 3));
//        String parsedSizeString = parseStringWithDoubleDigits(sumGB);
//        if (parsedSizeString.contains(",")) {
//            parsedSizeString = parsedSizeString.replace(",", "");
//        }
//        sumGB = Double.parseDouble(parsedSizeString);
//        return sumGB;
//    }
//
//    public static double getDeviceTotalGB() {
//        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
//        long blockSize = (long) statFs.getBlockSize();
//        long totalSize = statFs.getBlockCount() * blockSize;
//
//        double totalSizeGB = totalSize / (Math.pow(1024, 3));
//        String parsedSizeString = parseStringWithDoubleDigits(totalSizeGB);
//        if (parsedSizeString.contains(",")) {
//            parsedSizeString = parsedSizeString.replace(",", "");
//        }
//        totalSizeGB = Double.parseDouble(parsedSizeString);
//        return totalSizeGB;
//    }
//
//    public static double getDeviceAvailableGB() {
//        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
//        long blockSize = (long) statFs.getBlockSize();
//        long availableSize = statFs.getAvailableBlocks() * blockSize;
//
//        double availableSizeGB = availableSize / (Math.pow(1024, 3));
//        String parsedSizeString = parseStringWithDoubleDigits(availableSizeGB);
//        if (parsedSizeString.contains(",")) {
//            parsedSizeString = parsedSizeString.replace(",", "");
//        }
//        availableSizeGB = Double.parseDouble(parsedSizeString);
//        return availableSizeGB;
//    }
//
//    private static String parseStringWithDoubleDigits(double value) {
//        try {
//            return String.format(java.util.Locale.US, "%.2f", value);
//        } catch (Exception e) {
//            return "0.0";
//        }
//    }
//
//    public static List<String> getSavedSearches(Context context) {
//        Type listType = new TypeToken<ArrayList<String>>() {
//        }.getType();
//        String serializedString = new StorageUtil(context).getSearches();
//        if (new Gson().fromJson(serializedString, listType) == null) {
//            return new ArrayList<>();
//        } else {
//            ArrayList<String> lista = new Gson().fromJson(serializedString, listType);
//            Collections.reverse(lista);
//            if (lista.size() > 5) {
//                return lista.subList(0, 5);
//            } else {
//                return lista;
//            }
//        }
//    }
//
//    public static void clearGlideCache(Activity activity) {
//        try {
//            new Thread(() -> Glide.get(activity).clearDiskCache()).start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void clearFrescoCache() {
//        try {
//            new Thread(() -> {
//                Fresco.getImagePipeline().clearCaches();
//                Fresco.getImagePipeline().clearDiskCaches();
//                Fresco.getImagePipeline().clearMemoryCaches();
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static ArrayList<EntityModel> reverseList(List<EntityModel> items) {
//        if (items.isEmpty())
//            return new ArrayList<>();
//
//        int reverseIndex = items.size() - 1;
//        ArrayList<EntityModel> sortedList = new ArrayList<>();
//
//        for (int i = 0; i < items.size(); i++) {
//            sortedList.add(items.get(reverseIndex));
//            reverseIndex--;
//        }
//
//        return sortedList;
//    }
//
//    public static int getColor(Context context, int colorName) {
//        TypedArray typedArray = context.getTheme().obtainStyledAttributes(R.styleable.Colors);
//        return typedArray.getColor(colorName, 0);
//    }
//
//    public static int getDrawable(Context context, int drawable) {
//        TypedArray typedArray = context.getTheme().obtainStyledAttributes(R.styleable.Colors);
//        return typedArray.getResourceId(drawable, 0);
//    }
//
//    public static Drawable getDrawableFile(Context context, int drawable) {
//        TypedArray typedArray = context.getTheme().obtainStyledAttributes(R.styleable.Colors);
//        return typedArray.getDrawable(drawable);
//    }
//
//    public static void enableSwiperOnReachingTopView(RecyclerView
//                                                             recyclerView, SwipeRefreshLayout swipeRefreshLayout, LinearLayoutManager
//                                                             stickyHeadersLinearLayoutManager) {
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                swipeRefreshLayout.setEnabled(stickyHeadersLinearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
//            }
//        });
//    }
//
//    public static void resetSelection(EditText... editTexts) {
//        for (EditText et : editTexts) {
//            if (et.hasFocus()) {
//                et.setSelection(et.getText().length());
//                break;
//            }
//        }
//    }
//
//    public static void initHeaderIcon(ImageView headerIcon, Context context) {
////        LanguageSwitchModel languageSwitchModel = new Gson().fromJson(new StorageUtil(context).loadSelectedLanguage(), LanguageSwitchModel.class);
//
//        switch (Constants.target.getCurrentAppTarget()) {
//            case MALLTV:
////                if (languageSwitchModel != null && languageSwitchModel.id != null && languageSwitchModel.id.equals("ol")) {
////                    headerIcon.setImageResource(R.drawable.cezch_team_icon);
////                } else {
//                headerIcon.setImageResource(Utils.getDrawable(context, R.styleable.Colors_mall_img_header_icon));
////                }
//                break;
//            case GJIRAFAVIDEO:
//                headerIcon.setImageResource(Utils.getDrawable(context, R.styleable.Colors_navBarIconGjirafaVideo));
//                break;
//            case KVIFF:
//                headerIcon.setImageResource(R.drawable.kviff_homepage_icon);
//                break;
//        }
//    }
//
//    public static void createBannerAd(final Context context,
//                                      final LinearLayout layoutToShow, AdSize adSize, String unitId, String parentNameForAnalytics,
//                                      final IHelper.AdvertisementDelegate advertisementDelegate) {
//        if (Constants.target.getCurrentAppTarget() != AppTarget.GJIRAFAVIDEO)
//            return;
//
//        final AdView adView = new AdView(context);
//        adView.setAdSize(adSize);
//        adView.setAdUnitId(unitId);
//
//        layoutToShow.addView(adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);
//        adView.setAdListener(new AdListener() {
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                super.onAdFailedToLoad(loadAdError);
//                Utils.setViewsVisibility("G", layoutToShow);
//            }
//
//            @Override
//            public void onAdImpression() {
//                super.onAdImpression();
//            }
//
//            @Override
//            public void onAdLoaded() {
//                Utils.setViewsVisibility("V", layoutToShow);
//                advertisementDelegate.onAdShown(adView);
//            }
//
//            @Override
//            public void onAdOpened() {
//            }
//
//            @Override
//            public void onAdClicked() {
//                new SendFirebaseAnalytics(context).sendDetailedAnalytics(GjirafaAnalyticsEnum.AD_CLICKED, new HashMap<String, String>() {{
//                    put("AdSize", adSize == AdSize.MEDIUM_RECTANGLE ? "Square AD" : "Smart Banner");
//                    put("Location", parentNameForAnalytics);
//                }});
//            }
//
//
//            @Override
//            public void onAdClosed() {
//            }
//        });
//    }
//
//    public static void setMultipleFontSettings9(TextView... textViews) {
//        try {
//            Typeface font = Typeface.createFromAsset(MallTVApplication.getAppContext().getAssets(), Constants.target.getCurrentAppTarget() == AppTarget.KVIFF ? "fonts/gt_america_ext_md.otf" : "fonts/Museo900.otf");
//            for (TextView textView : textViews) {
//                textView.setTypeface(font);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void setMultipleFontSettings7(TextView... textViews) {
//        try {
//            Typeface font = Typeface.createFromAsset(MallTVApplication.getAppContext().getAssets(), Constants.target.getCurrentAppTarget() == AppTarget.KVIFF ? "fonts/gt_america_bold.otf" :"fonts/Museo700.otf");
//            for (TextView textView : textViews) {
//                textView.setTypeface(font);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void setMultipleFontSettings5(TextView... textViews) {
//        try {
//            Typeface font = Typeface.createFromAsset(MallTVApplication.getAppContext().getAssets(), Constants.target.getCurrentAppTarget() == AppTarget.KVIFF ? "fonts/gt_america_rg.otf" :"fonts/Museo500.otf");
//            for (TextView textView : textViews) {
//                textView.setTypeface(font);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void setMultipleFontSettings3(TextView... textViews) {
//        try {
//            Typeface font = Typeface.createFromAsset(MallTVApplication.getAppContext().getAssets(), "fonts/Museo300.otf");
//            for (TextView textView : textViews) {
//                textView.setTypeface(font);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void setMultipleFontSettings1(TextView... textViews) {
//        try {
//            Typeface font = Typeface.createFromAsset(MallTVApplication.getAppContext().getAssets(), "fonts/Museo100.otf");
//            for (TextView textView : textViews) {
//                textView.setTypeface(font);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static String getLocalization(String itemToCheck) {
//        if (localizations == null)
//            return itemToCheck;
//
//        String tempItem = itemToCheck;
//        itemToCheck = "app." + itemToCheck.toLowerCase();
//        String localizationsJson = new Gson().toJson(localizations, Localizations.class);
//        HashMap<String, String> map = new HashMap<>();
//        try {
//            JSONObject jObject = new JSONObject(localizationsJson);
//            Iterator<?> keys = jObject.keys();
//            while (keys.hasNext()) {
//                String key = (String) keys.next();
//                String value = jObject.getString(key);
//                map.put(key.toLowerCase(), value);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return tempItem;
//        }
//
//        if (map.containsKey(itemToCheck)) {
//            return map.get(itemToCheck);
//        }
//
//        return tempItem;
//    }
//
//    public static Spanned processHtmlString(String htmlString) {
//
//        // remove leading <br/>
//        while (htmlString.startsWith("<br/>")) {
//
//            htmlString = htmlString.replaceFirst("<br/>", "");
//        }
//
//        // remove trailing <br/>
//        while (htmlString.endsWith("<br/>")) {
//
//            htmlString = htmlString.replaceAll("<br/>$", "");
//        }
//
//        // reduce multiple \n in the processed HTML string
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//
//            return Html.fromHtml(htmlString, FROM_HTML_MODE_COMPACT);
//        } else {
//
//            return Html.fromHtml(htmlString);
//        }
//    }
//
//    public static int getScreenWidth(Activity activity) {
//        if (activity == null || activity.getWindowManager() == null || activity.getWindowManager().getDefaultDisplay() == null)
//            return 0;
//
//        return activity.getWindowManager().getDefaultDisplay().getWidth();
//    }
//
//    public static int getScreenHeight(Activity activity) {
//        if (activity == null || activity.getWindowManager() == null || activity.getWindowManager().getDefaultDisplay() == null)
//            return 0;
//
//        return activity.getWindowManager().getDefaultDisplay().getHeight();
//    }
//
//    public static void setViewsEnabled(boolean state, View... views) {
//        for (View v : views) {
//            v.setEnabled(state);
//        }
//    }
//
//    public static void storeUserAgent(Context context) {
//        try {
//            String userAgent = "AndroidApp; " + Utils.getDeviceModel() + "; " + android.os.Build.VERSION.RELEASE;
//            new StorageUtil(context).storeUserAgent(userAgent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * This method will add headers to HubConnection.
//     *
//     * @param context - Class context
//     * @return - Added headers as HashMap<String,String>
//     */
//    public static HashMap<String, String> getSignalRCoreHeaders(Context context) {
//        HashMap<String, String> headers = new HashMap<>();
//
//        String identification = "";
//        String authToken = "";
//
//        String userAgent = Utils.getUserAgent();
//
//        if (context != null) {
//            new StorageUtil(context).storeUserAgent(userAgent);
//
//            if (new StorageUtil(context).loadIdentification() != null) {
//                identification = new StorageUtil(context).loadIdentification();
//            }
//
//
//            if (new StorageUtil(context).isLoggedIn()) {
//                if (new StorageUtil(context).loadAuthToken() != null) {
//                    authToken = new StorageUtil(context).loadAuthToken();
//                }
//                headers.put("Authorization", authToken);
//            }
//        }
//
//        headers.put("Identification", identification);
//        headers.put("X-User-Agent", userAgent);
//
//
//        return headers;
//    }
//
//    public static ArrayList<QuizParticipantModel> updateLineSeparators(ArrayList<QuizParticipantModel> quizParticipantModels) {
//        for (int i = 0; i < quizParticipantModels.size() - 1; i++) {
//            quizParticipantModels.get(i).hasLineSeparator = true;
//        }
//
//        return quizParticipantModels;
//    }
//
//    public static ArrayList<Integer> getCorrectAnswers(ArrayList<JsonElement> answers) {
//        ArrayList<Integer> x = new ArrayList<>();
//
//        for (JsonElement jsonElement : answers) {
//            x.add(new Gson().fromJson(jsonElement, Integer.class));
//        }
//
//        return x;
//    }
//
//    public static void writeQuizLogs(int quizId, String quizLogLine) {
//        if (!Constants.isDebug)
//            return;
//
//        File dir = new File(MallTVApplication.sharedContext.getExternalFilesDir(null), "Quiz_Logs");
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        try {
//            File gpxfile = new File(dir, quizId + "_log.txt");
//            FileWriter writer = new FileWriter(gpxfile, true);
//            writer.append(new DateTime().toString() + ":\n");
//            writer.append(quizLogLine);
//            writer.append("\n\n\n\n\n");
//            writer.flush();
//            writer.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void sendDetailedErrorInEmail(Context context, String error, Object modell, String className, String methodName) {
//        try {
//            String message = "Error:\n" + error + " \n\n\nModel:\n" + new Gson().toJson(modell) + "\n\n\nClass:\n" + className + "\n\n\n" + "Method:\n" + methodName;
//            new ApiRequests().sendDetailedErrorInEmail(context, message);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    /**
//     * Sets theme based on current target
//     */
//    public static void setTheme(Activity activity) {
//        if(Constants.target.getCurrentAppTarget() == AppTarget.KVIFF) {
//            activity.setTheme(R.style.KVIFFTheme);
//        } else if (new StorageUtil(activity).isOlympicsActive()) {
//            activity.setTheme(Utils.isLightTheme(activity) ? R.style.LightThemeOlympics : R.style.DarkThemeOlympics);
//        } else {
//            activity.setTheme(Utils.isLightTheme(activity) ? R.style.LightTheme : R.style.DarkTheme);
//        }
//    }
//
//    /**
//     * This method uses Application context found at -> {@link MallTVApplication#getAppContext()}
//     *
//     * @return false if getAppContext is null or light theme is not selected.
//     */
//    public static boolean isLightTheme() {
//        return MallTVApplication.getAppContext() != null && new StorageUtil(MallTVApplication.getAppContext()).isLightTheme();
//    }
//
//    /**
//     * This method requires context given from where method is called.
//     *
//     * @return false if given context in params is null or light theme is not selected.
//     */
//    public static boolean isLightTheme(Context context) {
//        return context != null && new StorageUtil(context).isLightTheme();
//    }
//
//    /**
//     * This method uses Application context found at -> {@link MallTVApplication#getAppContext()}
//     *
//     * @return false if getAppContext is null or dark theme is not selected.
//     */
//    public static boolean isDarkTheme() {
//        return MallTVApplication.getAppContext() != null && !new StorageUtil(MallTVApplication.getAppContext()).isLightTheme();
//    }
//
//    /**
//     * This method requires context given from where method is called.
//     *
//     * @return false if given context in params is null or dark theme is not selected.
//     */
//    public static boolean isDarkTheme(Context context) {
//        return context != null && !new StorageUtil(context).isLightTheme();
//    }
//
//    public static microsoft.aspnet.signalr.client.Credentials getDefaultCredentials(Context context) {
//        final String identification = new StorageUtil(context).loadIdentification();
//        final String userAgent = new StorageUtil(context).loadUserAgent();
//
//        return request -> {
//            request.addHeader("Identification", identification);
//            request.addHeader("X-User-Agent", userAgent);
//            if (new StorageUtil(context).isLoggedIn()) {
//                final String authToken = new StorageUtil(context).loadAuthToken();
//                request.addHeader("Authorization", authToken);
//            }
//        };
//    }
//
//    public static void createTimer(int duration, Runnable runnable) {
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                runnable.run();
//            }
//        }, duration);
//    }
//
//    public static void createTimer(Activity activity, int duration, Runnable runnable) {
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (activity != null) {
//                    activity.runOnUiThread(runnable);
//                }
//            }
//        }, duration);
//    }
//
//    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
//
//    static {
//        suffixes.put(1_000L, "K");
//        suffixes.put(1_000_000L, "M");
//        suffixes.put(1_000_000_000L, "G");
//        suffixes.put(1_000_000_000_000L, "T");
//        suffixes.put(1_000_000_000_000_000L, "P");
//        suffixes.put(1_000_000_000_000_000_000L, "E");
//    }
//
//    public static String format(long value) {
//        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
//        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
//        if (value < 0) return "-" + format(-value);
//        if (value < 1000) return Long.toString(value); //deal with easy case
//
//        Map.Entry<Long, String> e = suffixes.floorEntry(value);
//        Long divideBy = e.getKey();
//        String suffix = e.getValue();
//
//        long truncated = value / (divideBy / 10); //the number part of the output times 10
//        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
//        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
//    }
//
//    public static int getTheme(Context context) {
//        return new StorageUtil(context).isLightTheme() ? R.style.LightTheme : R.style.DarkTheme;
//    }
//
//    public static void setupDialogDefaults(Dialog dialog, int style) {
//        if (dialog != null) {
//            Window window = dialog.getWindow();
//            if (window != null) {
//                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                WindowManager.LayoutParams wlp = window.getAttributes();
//                wlp.windowAnimations = style;
//                wlp.gravity = Gravity.CENTER;
//                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                window.setAttributes(wlp);
//            }
//        }
//    }
//
//    public static boolean isVisible(final View view, Activity activity) {
//        if (view == null || activity == null) {
//            return false;
//        }
//
//        if (!view.isShown()) {
//            return false;
//        }
//
//        final Rect actualPosition = new Rect();
//        view.getGlobalVisibleRect(actualPosition);
//        final Rect screen = new Rect(0, 0, getScreenWidth(activity), getScreenHeight(activity));
//        return actualPosition.intersect(screen);
//    }
//
//    public static Drawable tintDrawable(Drawable originalDrawable, int color) {
//        Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
//        DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(color));
//        return wrappedDrawable;
//    }
}