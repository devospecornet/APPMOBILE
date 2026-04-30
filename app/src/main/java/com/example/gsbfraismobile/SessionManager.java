package com.example.gsbfraismobile;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "gsb_mobile_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_NAME = "user_name";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String email, String role, String userName) {
        preferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ROLE, role)
                .putString(KEY_USER_NAME, userName)
                .apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, "");
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}