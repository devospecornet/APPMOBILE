package com.example.gsbfraismobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private final ArrayList<Fiche> listeFiches = new ArrayList<>();
    private FicheAdapter adapter;
    private TextView textInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Button btnAjouter = findViewById(R.id.btnAjouter);
        Button btnDeconnexion = findViewById(R.id.btnDeconnexion);
        progressBar = findViewById(R.id.progressMain);
        recyclerView = findViewById(R.id.recyclerFiches);
        textInfos = findViewById(R.id.textInfos);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FicheAdapter(this, listeFiches);
        recyclerView.setAdapter(adapter);

        btnAjouter.setOnClickListener(v ->
                startActivity(new Intent(this, AjouterFicheActivity.class)));

        btnDeconnexion.setOnClickListener(v -> {
            sessionManager.clearSession();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        chargerFiches();
    }

    private void chargerFiches() {
        progressBar.setVisibility(View.VISIBLE);

        String url = ApiConfig.BASE_URL + "fiches.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    listeFiches.clear();

                    try {
                        boolean succes = response.getBoolean("succes");
                        if (!succes) {
                            Toast.makeText(this, "Impossible de charger les fiches", Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONArray fiches = response.getJSONArray("fiches");

                        for (int i = 0; i < fiches.length(); i++) {
                            JSONObject obj = fiches.getJSONObject(i);

                            Fiche fiche = new Fiche(
                                    obj.optInt("id"),
                                    obj.optString("numero_fiche"),
                                    obj.optString("mois"),
                                    obj.optDouble("montant_total"),
                                    obj.optString("statut"),
                                    obj.optString("date_creation"),
                                    obj.optString("commentaire_visiteur"),
                                    obj.optString("commentaire_comptable")
                            );

                            listeFiches.add(fiche);
                        }

                        adapter.notifyDataSetChanged();

                        if (listeFiches.isEmpty()) {
                            textInfos.setText(getString(R.string.aucune_fiche));
                        } else {
                            textInfos.setText(getString(R.string.mes_fiches));
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Erreur de lecture JSON", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Erreur réseau lors du chargement", Toast.LENGTH_LONG).show();
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