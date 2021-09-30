package com.example.testlibrary;

/**
 * Created by Yll Memeti on 9/30/2021.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gjirafa.gjirafavideo.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;



import static android.content.ContentValues.TAG;

public class StorageUtil {

    private Context context;
    private SharedPreferences preferences;
    private final String STORAGE_NAME = BuildConfig.APPLICATION_ID + "_STORAGE";
    private final String THEME = BuildConfig.APPLICATION_ID + "_THEME";

    // Preference names
    public static final String AUTH_TOKEN = BuildConfig.APPLICATION_ID + "_AUTH_TOKEN";
    public static final String AUTO_PLAY = BuildConfig.APPLICATION_ID + "_AUTO_PLAY";
    public static final String NOTIFICATION = BuildConfig.APPLICATION_ID + "_NOTIFICATION";
    public static final String BADGES = BuildConfig.APPLICATION_ID + "_BADGES";
    public static final String QUIZ_PLAYER_COUNTS = BuildConfig.APPLICATION_ID + "_QUIZ_PLAYER_COUNTS";
    public static final String GENDER = BuildConfig.APPLICATION_ID + "_GENDER";
    public static final String EnableNotifications = BuildConfig.APPLICATION_ID + "_EnableNotifications";
    public static final String SELECTED_LANGUAGE = BuildConfig.APPLICATION_ID + "_SELECTED_LANGUAGE";
    public static final String VideoSkiped = BuildConfig.APPLICATION_ID + "_VideoSkiped";
    public static final String QUIZ = BuildConfig.APPLICATION_ID + "_QUIZ";
    public static final String Olympics = BuildConfig.APPLICATION_ID + "_Olympics";
    public static final String QUIZ_ANSWER_ID = BuildConfig.APPLICATION_ID + "_QUIZ_ANSWER_ID";
    public static final String LAST_ANSWERED_QUESTION_ID = BuildConfig.APPLICATION_ID + "_LAST_ANSWERED_QUESTION_ID";
    public static final String USER_SELECTED_LOCALE = BuildConfig.APPLICATION_ID + "_USER_SELECTED_LOCALE";

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAuthToken(String userName, String singleSignOn, String token, String identification, int type) {
        // Type: -1 for none, 0 for guest, 1 for user
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userFullName", userName);
        editor.putString("authToken", token);
        editor.putString("singleSignOn", singleSignOn);
        editor.putString("identification", identification);
        editor.putInt("authType", type);
        editor.apply();
    }

    public String loadAuthToken() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            // Log.e(TAG, "loadAuthToken: "+preferences.getString("authToken", context.getString(R.string.anonim_token)) );
            return preferences.getString("authToken", null);
        } catch (Exception e) {
            return null;
        }
    }

    public void storeSSOToken(String token) {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("SSOToken", token);
        editor.apply();
    }

    public String loadSSOToken() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            return preferences.getString("SSOToken", null);
        } catch (Exception e) {
            return null;
        }
    }

    public void storeCampaignDetails(HashSet<String> mSet) {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("campaignDetails", mSet);
        editor.apply();
    }

    public Set<String> loadCampaignDetails() {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        return preferences.getStringSet("campaignDetails", new HashSet<>());
    }

    public void storeHistoryVideos(ArrayList<VideoDetailsModel> videoModelList) {
        Type listType = new TypeToken<ArrayList<VideoDetailsModel>>() {
        }.getType();
        String historySeralizedVideos = new Gson().toJson(videoModelList, listType);
        preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("historyVideos", historySeralizedVideos);
        editor.apply();
    }

    public ArrayList<VideoDetailsModel> loadHistoryVideos() {
        try {
            preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
            Type listType = new TypeToken<ArrayList<VideoDetailsModel>>() {
            }.getType();
            String serializedArrayList = preferences.getString("historyVideos", new Gson().toJson(new ArrayList<VideoDetailsModel>(), listType));
            return new Gson().fromJson(serializedArrayList, listType);
        } catch (Exception e) {
            return null;
        }
    }

    public void storeUserAgent(String agent) {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("UserAgent", agent);
        editor.apply();
    }

    public String loadUserAgent() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            return preferences.getString("UserAgent", null);
        } catch (Exception e) {
            return null;
        }
    }

    public void storeUpdateLater(String agent) {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("UpdateDialogDate", agent);
        editor.apply();
    }

    public String loadUpdateLater() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            return preferences.getString("UpdateDialogDate", null);
        } catch (Exception e) {
            return null;
        }
    }

    public void storeResetQuizPlayers(String time) {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("QUIZ_PLAYER_RESET_STATE", time);
        editor.apply();
    }

    public String getResetQuizPlayers() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            return preferences.getString("QUIZ_PLAYER_RESET_STATE", null);
        } catch (Exception e) {
            return null;
        }
    }

    public String loadIdentification() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            // Log.e(TAG, "loadAuthToken: "+preferences.getString("authToken", context.getString(R.string.anonim_token)) );
            return preferences.getString("identification", null);
        } catch (Exception e) {
            return null;
        }
    }

    public void storeUserDetails(UserProfileModel userProfileModel) {
        if (context == null)
            return;

        String modelString = new Gson().toJson(userProfileModel);
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userDetails", modelString);
        editor.apply();
    }

    public UserProfileModel loadUserDetails() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            return new Gson().fromJson(preferences.getString("userDetails", null), UserProfileModel.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void storeLocalizations(String localizations) {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(BuildConfig.APPLICATION_ID + "localization", localizations);
        editor.apply();
    }

    public Localizations loadLocalizations() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            return new Gson().fromJson(preferences.getString(BuildConfig.APPLICATION_ID + "localization", null), Localizations.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void storeUserSettings(HashMap<String, Boolean> userSettings) {
        String serializedSettings = new Gson().toJson(userSettings);

        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userSettings", serializedSettings);
        editor.apply();
    }

    public void storeBadgeDetails(String badgeDetails) {
        preferences = context.getSharedPreferences(BADGES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("badgeDetails", badgeDetails);
        editor.apply();
    }

    public String getBadgeDetails() {
        preferences = context.getSharedPreferences(BADGES, Context.MODE_PRIVATE);
        return preferences.getString("badgeDetails", "");
    }

    public void storeQuizPlayerDetails(String badgeDetails) {
        preferences = context.getSharedPreferences(QUIZ_PLAYER_COUNTS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("QUIZ_PLAYER_COUNTS", badgeDetails);
        editor.apply();
    }

    public String getQuizPlayerDetails() {
        preferences = context.getSharedPreferences(QUIZ_PLAYER_COUNTS, Context.MODE_PRIVATE);
        return preferences.getString("QUIZ_PLAYER_COUNTS", "{}");
    }

    public HashMap<String, Boolean> loadUserSettings() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            return new Gson().fromJson(preferences.getString("userSettings", null), HashMap.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void storeWatchedVideos(HashMap<Integer, Float> watchedVideos) {
        String serializedSettings = new Gson().toJson(watchedVideos);

        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("watchedVideos", serializedSettings);
        editor.apply();
    }

    public HashMap<Integer, Float> loadWatchedVideos() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            String storedHashMapString = preferences.getString("watchedVideos", "");

            java.lang.reflect.Type type = new TypeToken<HashMap<Integer, Float>>() {
            }.getType();
            return new Gson().fromJson(storedHashMapString, type);
        } catch (Exception ignored) {
        }

        return null;
    }


    public void storeOfflineVideos(HashMap<String, EntityModel> offlineVideos) {
        try {
            String serializedOfflineVideos = new Gson().toJson(offlineVideos);
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("offlineVideos", serializedOfflineVideos);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, EntityModel> loadOfflineVideos() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            String serializedOfflineVideos = preferences.getString("offlineVideos", "");

            java.lang.reflect.Type type = new TypeToken<HashMap<String, EntityModel>>() {
            }.getType();
            return new Gson().fromJson(serializedOfflineVideos, type);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void storeFirstTimeLoad() {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }

    public boolean isFirstTimeOpen() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            return preferences.getBoolean("isFirstTime", true);
        } catch (Exception e) {
            return true;
        }
    }

    public void storeIsLoggedIn(boolean isLoggedIn) {
        // Type: -1 for none, 0 for guest, 1 for user
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("userLoggedIn", isLoggedIn);
        editor.apply();
    }


    public boolean isLoggedIn() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            // Log.e(TAG, "loadAuthToken: "+preferences.getString("authToken", context.getString(R.string.anonim_token)) );
            return preferences.getBoolean("userLoggedIn", false);
        } catch (Exception e) {
            return false;
        }
    }

    public void storeGender(String gender) {
        preferences = context.getSharedPreferences(GENDER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userGender", gender);
        editor.apply();
    }

    public String loadGender() {
        preferences = context.getSharedPreferences(GENDER, Context.MODE_PRIVATE);
        return preferences.getString("userGender", "");
    }

    public void storeEnableNotifications(boolean enable) {
        preferences = context.getSharedPreferences(EnableNotifications, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("EnableNotifications", enable);
        editor.apply();
    }

    public boolean getEnableNotifications() {
        try {
            preferences = context.getSharedPreferences(EnableNotifications, Context.MODE_PRIVATE);
            return preferences.getBoolean("EnableNotifications", true);
        } catch (Exception e) {
            return false;
        }
    }

    public void storeSelectedLanguage(String language) {
        preferences = context.getSharedPreferences(SELECTED_LANGUAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("selectedLang", language);
        editor.apply();
    }

    public String loadSelectedLanguage() {
        preferences = context.getSharedPreferences(SELECTED_LANGUAGE, Context.MODE_PRIVATE);
        return preferences.getString("selectedLang", "{}");
    }

    public void storeShownRedirectLanguage(boolean isShown) {
        preferences = context.getSharedPreferences(SELECTED_LANGUAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("redirectLang", isShown);
        editor.apply();
    }

    public boolean loadIsShownRedirect() {
        preferences = context.getSharedPreferences(SELECTED_LANGUAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean("redirectLang", false);
    }

    public void clearUserEmail() {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("userEmail");
        editor.apply();
    }

    public void clearIdentification() {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("identification");
        editor.apply();
    }

    public String loadSingleSignOn() {
        preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
        return preferences.getString("singleSignOn", "???");
    }

    public void clearAuthToken() {
        try {
            preferences = context.getSharedPreferences(AUTH_TOKEN, Context.MODE_PRIVATE);
            preferences.edit().clear().apply();
        } catch (Exception ignore) {
        }
    }

    public void storeAutoPlayState(boolean isTrue) {
        try {
            preferences = context.getSharedPreferences(AUTO_PLAY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isAutoPlay", isTrue);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean loadAutoPlayState() {
        if (context != null) {
            preferences = context.getSharedPreferences(AUTO_PLAY, Context.MODE_PRIVATE);
            return preferences.getBoolean("isAutoPlay", true);
        } else {
            return false;
        }
    }

    public void storeNotificationToken(String token) {
        try {
            preferences = context.getSharedPreferences(NOTIFICATION, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            Log.e(TAG, "storeNotificationToken: " + token);
            editor.putString("NotificationToken", token);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String loadNotificationToken() {
        try {
            preferences = context.getSharedPreferences(NOTIFICATION, Context.MODE_PRIVATE);
            return preferences.getString("NotificationToken", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void storeSearches(String serializedList) {
        preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("savedSearch", serializedList);
        editor.apply();
    }

    public String getSearches() {
        preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        return preferences.getString("savedSearch", "");
    }

    public void clearSearches() {
        preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("savedSearch");
        editor.apply();
    }


    public void storeVideoSkips(int times) {
        try {
            preferences = context.getSharedPreferences(VideoSkiped, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("videoSkiped", times);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getVideoSkips() {
        try {
            preferences = context.getSharedPreferences(VideoSkiped, Context.MODE_PRIVATE);
            return preferences.getInt("videoSkiped", 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public void storeQuizScrollShownTimes(int times) {
        try {
            preferences = context.getSharedPreferences(QUIZ, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("QUIZ_SCROLL_TIMES", times);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getQuizScrollShownTimes() {
        try {
            preferences = context.getSharedPreferences(QUIZ, Context.MODE_PRIVATE);
            return preferences.getInt("QUIZ_SCROLL_TIMES", 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isOlympicsActive() {
        try {
            preferences = context.getSharedPreferences(Olympics, Context.MODE_PRIVATE);
            return preferences.getBoolean("isOlympicsActive", false);
        } catch (Exception e) {
            return false;
        }
    }

    public void setOlympicsActive(boolean isOlympicsActive) {
        try {
            preferences = context.getSharedPreferences(Olympics, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isOlympicsActive", isOlympicsActive);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLightTheme() {
        try {
            preferences = context.getSharedPreferences(THEME, Context.MODE_PRIVATE);
            return preferences.getBoolean("selectedTheme", false);
        } catch (Exception e) {
            return false;
        }
    }

    public void setLightTheme(boolean isLightTheme) {
        try {
            preferences = context.getSharedPreferences(THEME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("selectedTheme", isLightTheme);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveAnswerId(int answerId) {
        try {
            preferences = context.getSharedPreferences(QUIZ_ANSWER_ID, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("ANSWER_ID", answerId);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getSavedAnswerId() {
        try {
            preferences = context.getSharedPreferences(QUIZ_ANSWER_ID, Context.MODE_PRIVATE);
            return preferences.getInt("ANSWER_ID", -1);
        } catch (Exception e) {
            return -1;
        }
    }

    public void saveLastAnsweredQuestionId(int lastAnsweredQuestionId) {
        try {
            preferences = context.getSharedPreferences(LAST_ANSWERED_QUESTION_ID, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("LAST_QUESTION_ID", lastAnsweredQuestionId);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getLastAnsweredQuestionId() {
        try {
            preferences = context.getSharedPreferences(LAST_ANSWERED_QUESTION_ID, Context.MODE_PRIVATE);
            return preferences.getInt("LAST_QUESTION_ID", -1);
        } catch (Exception e) {
            return -1;
        }
    }

    public void saveUserSelectedLocale(String locale) {
        try {
            preferences = context.getSharedPreferences(USER_SELECTED_LOCALE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("USER_SELECTED_LOCALE", locale);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUserSelectedLocale() {
        try {
            preferences = context.getSharedPreferences(USER_SELECTED_LOCALE, Context.MODE_PRIVATE);
            return preferences.getString("USER_SELECTED_LOCALE", "");
        } catch (Exception e) {
            return "";
        }
    }
}

