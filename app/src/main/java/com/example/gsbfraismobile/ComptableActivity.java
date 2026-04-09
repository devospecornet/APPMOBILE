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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comptable);

        sessionManager = new SessionManager(this);

        if (!"comptable".equals(sessionManager.getUserRole())) {
            Toast.makeText(this, R.string.acces_refuse, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerComptable);
        Button btnDeconnexion = findViewById(R.id.btnDeconnexionComptable);
        textAucuneFicheComptable = findViewById(R.id.textAucuneFicheComptable);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FicheComptableAdapter(this, fiches, sessionManager, this::chargerFichesTransmises);
        recyclerView.setAdapter(adapter);

        btnDeconnexion.setOnClickListener(v -> {
            sessionManager.clearSession();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        chargerFichesTransmises();
    }

    private void chargerFichesTransmises() {
        String url = ApiConfig.BASE_URL + "fiches_comptable.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    fiches.clear();

                    try {
                        JSONArray items = response.optJSONArray("fiches");

                        if (items != null) {
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);

                                String nomComplet = obj.optString("prenom", "") + " " + obj.optString("nom", "");

                                fiches.add(new FicheComptable(
                                        obj.optInt("id"),
                                        obj.optString("numero_fiche"),
                                        obj.optString("mois"),
                                        obj.optDouble("montant_total"),
                                        nomComplet.trim()
                                ));
                            }
                        }

                        adapter.notifyDataSetChanged();
                        textAucuneFicheComptable.setVisibility(fiches.isEmpty() ? View.VISIBLE : View.GONE);

                    } catch (Exception e) {
                        textAucuneFicheComptable.setVisibility(View.VISIBLE);
                        Toast.makeText(this, R.string.erreur_json_comptable, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    textAucuneFicheComptable.setVisibility(View.VISIBLE);
                    Toast.makeText(this, R.string.erreur_reseau_comptable, Toast.LENGTH_LONG).show();
                }
        ) {
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
