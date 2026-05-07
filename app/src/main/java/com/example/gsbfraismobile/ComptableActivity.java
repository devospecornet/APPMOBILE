package com.example.gsbfraismobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ComptableActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private final ArrayList<FicheComptable> fiches = new ArrayList<>();
    private FicheComptableAdapter adapter;
    private TextView textAucuneFicheComptable;
    private TextView textResumeComptable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) { startActivity(new Intent(this, LoginActivity.class)); finish(); return; }
        if (!"comptable".equals(sessionManager.getUserRole())) { Toast.makeText(this, R.string.acces_refuse, Toast.LENGTH_LONG).show(); finish(); return; }
        setContentView(R.layout.activity_comptable);

        RecyclerView recyclerView = findViewById(R.id.recyclerComptable);
        Button btnDeconnexion = findViewById(R.id.btnDeconnexionComptable);
        Button btnActualiser = findViewById(R.id.btnActualiserComptable);
        textAucuneFicheComptable = findViewById(R.id.textAucuneFicheComptable);
        textResumeComptable = findViewById(R.id.textResumeComptable);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FicheComptableAdapter(this, fiches, sessionManager, this::chargerFichesTransmises);
        recyclerView.setAdapter(adapter);

        btnActualiser.setOnClickListener(v -> chargerFichesTransmises());
        btnDeconnexion.setOnClickListener(v -> LogoutHelper.logout(this, sessionManager));
    }

    @Override
    protected void onResume() {
        super.onResume();
        chargerFichesTransmises();
    }

    private void chargerFichesTransmises() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ApiConfig.BASE_URL + "fiches_comptable.php", null,
                response -> {
                    fiches.clear();
                    try {
                        if (!response.optBoolean("succes", false)) {
                            Toast.makeText(this, response.optString("message", getString(R.string.erreur_json_comptable)), Toast.LENGTH_LONG).show();
                            return;
                        }
                        JSONArray items = response.optJSONArray("fiches");
                        if (items != null) {
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                String nomComplet = obj.optString("prenom", "") + " " + obj.optString("nom", "");
                                fiches.add(new FicheComptable(obj.optInt("id"), obj.optString("numero_fiche"), obj.optString("mois"), obj.optDouble("montant_total"), nomComplet.trim()));
                            }
                        }
                        adapter.notifyDataSetChanged();
                        textAucuneFicheComptable.setVisibility(fiches.isEmpty() ? View.VISIBLE : View.GONE);
                        textResumeComptable.setText(getString(R.string.resume_comptable, fiches.size()));
                    } catch (Exception e) {
                        textAucuneFicheComptable.setVisibility(View.VISIBLE);
                        Toast.makeText(this, R.string.erreur_json_comptable, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    textAucuneFicheComptable.setVisibility(View.VISIBLE);
                    if (ApiErrorHelper.isAuthError(error)) { ApiErrorHelper.forceLogin(this, sessionManager, ApiErrorHelper.extractMessage(error, "Session expirée.")); return; }
                    Toast.makeText(this, ApiErrorHelper.extractMessage(error, getString(R.string.erreur_reseau_comptable)), Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + sessionManager.getToken());
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
