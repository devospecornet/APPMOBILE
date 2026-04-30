package com.example.gsbfraismobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            SessionNavigator.openHomeForRole(this, sessionManager);
            return;
        }
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressLogin);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email et mot de passe obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("mot_de_passe", password);
        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Erreur de préparation JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiConfig.BASE_URL + "connexion.php",
                body,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        boolean succes = response.optBoolean("succes", false);
                        if (!succes) {
                            Toast.makeText(this, response.optString("message", "Connexion refusée"), Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONObject data = response.optJSONObject("data");
                        JSONObject utilisateur = data != null ? data.optJSONObject("utilisateur") : null;
                        String token = data != null ? data.optString("jeton", "") : "";

                        if (token.isEmpty() || utilisateur == null) {
                            Toast.makeText(this, "Réponse API invalide", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String role = utilisateur.optString("role", "");
                        String prenom = utilisateur.optString("prenom", "");
                        String nom = utilisateur.optString("nom", "");
                        String userName = (prenom + " " + nom).trim();

                        sessionManager.saveSession(token, email, role, userName);
                        Toast.makeText(this, R.string.connexion_reussie, Toast.LENGTH_SHORT).show();
                        SessionNavigator.openHomeForRole(this, sessionManager);
                    } catch (Exception e) {
                        Log.e("GSB_LOGIN", "Erreur parsing login", e);
                        Toast.makeText(this, "Réponse serveur invalide", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    String message = "Erreur réseau ou serveur";
                    if (error instanceof TimeoutError) {
                        message = "Délai dépassé.";
                    } else if (error instanceof NoConnectionError || error instanceof NetworkError) {
                        message = "Impossible de joindre le serveur.";
                    } else if (error instanceof ServerError) {
                        message = "Erreur côté serveur PHP.";
                    } else if (error instanceof ParseError) {
                        message = "Réponse serveur illisible.";
                    }

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e("GSB_LOGIN", "HTTP " + error.networkResponse.statusCode + " : " + responseBody);
                        message += " (HTTP " + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void ouvrirEcranSelonRole(String role) {
        Intent intent;
        switch (role) {
            case "comptable":
                intent = new Intent(this, ComptableActivity.class);
                break;
            case "administrateur":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "visiteur":
            default:
                intent = new Intent(this, MainActivity.class);
                break;
        }
        startActivity(intent);
    }
}
