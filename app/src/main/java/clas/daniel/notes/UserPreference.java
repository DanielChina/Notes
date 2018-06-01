package clas.daniel.notes;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreference {

    private static final String PREFS_NAME = "user_preference";
    private static final String USER_NAME = "user_name";
    private static final String USER_PWD = "user_pwd";
    private static final String USER_STATUS = "user_status";
    private static final String SYNC_ENABLED = "sync_enabled";

    private SharedPreferences mPreference;
    private SharedPreferences.Editor mEditor;
    private Context mContext;

    public UserPreference(Context context) {
        mContext = context;
        mPreference = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mEditor = mPreference.edit();
    }

    public boolean isUserLoggedIn() {
        return mPreference.getBoolean(USER_STATUS, false);
    }

    public void setUserSignInStatus(boolean isSignedIn) {
        mEditor.putBoolean(USER_STATUS, isSignedIn);
        mEditor.commit();
    }

    public boolean getUserLoginStatus() {
        return mPreference.getBoolean(USER_STATUS, false);
    }

    public String getUserName() {
        return mPreference.getString(USER_NAME, "");
    }

    public String getUserpwd() {
        return mPreference.getString(USER_PWD, "");
    }

    public void setUserName(String userName) {
        mEditor.putString(USER_NAME, userName);
        mEditor.commit();
    }

    public void setPassword(String pwd) {
        mEditor.putString(USER_PWD, pwd);
        mEditor.commit();
    }

    public boolean isSyncEnabled() {
        return mPreference.getBoolean(SYNC_ENABLED, false);
    }

    public void setSyncEnabled(boolean syncEnabled) {
        mEditor.putBoolean(SYNC_ENABLED, syncEnabled);
        mEditor.commit();
    }

    public void logout() {
        mEditor.clear();
        mEditor.commit();
    }

}
