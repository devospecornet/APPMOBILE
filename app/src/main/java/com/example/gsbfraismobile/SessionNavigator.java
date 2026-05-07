package com.example.gsbfraismobile;

import android.app.Activity;
import android.content.Intent;

public final class SessionNavigator {
    private SessionNavigator() {}

    public static void openHomeForRole(Activity activity, SessionManager sessionManager) {
        String role = sessionManager.getUserRole();
        Intent intent;
        if ("comptable".equals(role)) {
            intent = new Intent(activity, ComptableActivity.class);
        } else if ("administrateur".equals(role)) {
            intent = new Intent(activity, AdminActivity.class);
        } else {
            intent = new Intent(activity, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
    }
}
