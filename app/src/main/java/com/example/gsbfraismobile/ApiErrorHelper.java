package com.example.gsbfraismobile;

import android.app.Activity;
import android.content.Intent;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public final class ApiErrorHelper {
    private ApiErrorHelper() {}

    public static boolean isAuthError(VolleyError error) {
        return error != null && error.networkResponse != null && (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403);
    }

    public static String extractMessage(VolleyError error, String fallback) {
        if (error == null) return fallback;
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                JSONObject obj = new JSONObject(body);
                String message = obj.optString("message", "").trim();
                if (!message.isEmpty()) return message;
            } catch (Exception ignored) { }
        }
        if (error instanceof TimeoutError) return "Délai dépassé.";
        if (error instanceof NoConnectionError || error instanceof NetworkError) return "Impossible de joindre le serveur.";
        if (error instanceof ServerError) return "Erreur côté serveur.";
        if (error instanceof ParseError) return "Réponse serveur illisible.";
        return fallback;
    }

    public static void forceLogin(Activity activity, SessionManager sessionManager, String message) {
        sessionManager.clearSession();
        if (message != null && !message.isEmpty()) {
            android.widget.Toast.makeText(activity, message, android.widget.Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
