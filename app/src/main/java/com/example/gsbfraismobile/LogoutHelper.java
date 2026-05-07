package com.example.gsbfraismobile;

import android.app.Activity;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import java.util.HashMap;
import java.util.Map;

public final class LogoutHelper {
    private LogoutHelper() {}

    public static void logout(Activity activity, SessionManager sessionManager) {
        String token = sessionManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            ApiErrorHelper.forceLogin(activity, sessionManager, "Session terminée.");
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiConfig.BASE_URL + "deconnexion.php", null,
                response -> ApiErrorHelper.forceLogin(activity, sessionManager, "Déconnexion effectuée."),
                error -> ApiErrorHelper.forceLogin(activity, sessionManager, "Déconnexion effectuée.")) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(activity).addToRequestQueue(request);
    }
}
