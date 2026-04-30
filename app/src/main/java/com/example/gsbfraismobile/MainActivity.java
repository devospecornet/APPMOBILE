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
    private final ArrayList<Fiche> listeFiches = new ArrayList<>();
    private FicheAdapter adapter;
    private TextView textInfos;
    private TextView textUserMain;
    private TextView textResumeMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if (!"visiteur".equals(sessionManager.getUserRole())) {
            SessionNavigator.openHomeForRole(this, sessionManager);
            return;
        }

        setContentView(R.layout.activity_main);

        Button btnAjouter = findViewById(R.id.btnAjouter);
        Button btnActualiser = findViewById(R.id.btnActualiser);
        Button btnDeconnexion = findViewById(R.id.btnDeconnexion);
        progressBar = findViewById(R.id.progressMain);
        RecyclerView recyclerView = findViewById(R.id.recyclerFiches);
        textInfos = findViewById(R.id.textInfos);
        textUserMain = findViewById(R.id.textUserMain);
        textResumeMain = findViewById(R.id.textResumeMain);

        textUserMain.setText(getString(R.string.connecte_en_tant_que, sessionManager.getUserName()));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FicheAdapter(this, listeFiches);
        recyclerView.setAdapter(adapter);

        btnAjouter.setOnClickListener(v -> startActivity(new Intent(this, AjouterFicheActivity.class)));
        btnActualiser.setOnClickListener(v -> chargerFiches());
        btnDeconnexion.setOnClickListener(v -> LogoutHelper.logout(this, sessionManager));
    }

    @Override
    protected void onResume() {
        super.onResume();
        chargerFiches();
    }

    private void chargerFiches() {
        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ApiConfig.BASE_URL + "fiches.php", null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    listeFiches.clear();
                    try {
                        if (!response.optBoolean("succes", false)) {
                            Toast.makeText(this, response.optString("message", "Impossible de charger les fiches"), Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONArray fiches = response.optJSONArray("fiches");
                        if (fiches != null) {
                            for (int i = 0; i < fiches.length(); i++) {
                                JSONObject obj = fiches.getJSONObject(i);
                                listeFiches.add(new Fiche(
                                        obj.optInt("id"),
                                        obj.optString("numero_fiche"),
                                        obj.optString("mois"),
                                        obj.optDouble("montant_total"),
                                        obj.optString("statut"),
                                        obj.optString("date_creation"),
                                        obj.optString("commentaire_visiteur"),
                                        obj.optString("commentaire_comptable")
                                ));
                            }
                        }
                        adapter.notifyDataSetChanged();
                        int modifiables = 0;
                        for (Fiche fiche : listeFiches) {
                            if (UiHelper.estFicheModifiable(fiche.getStatut())) modifiables++;
                        }
                        textResumeMain.setText(getString(R.string.resume_visiteur, listeFiches.size(), modifiables));
                        textInfos.setText(listeFiches.isEmpty() ? getString(R.string.aucune_fiche) : getString(R.string.mes_fiches));
                    } catch (Exception e) {
                        Toast.makeText(this, "Erreur de lecture JSON", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    if (ApiErrorHelper.isAuthError(error)) {
                        ApiErrorHelper.forceLogin(this, sessionManager, ApiErrorHelper.extractMessage(error, "Session expirée."));
                        return;
                    }
                    Toast.makeText(this, ApiErrorHelper.extractMessage(error, "Erreur réseau lors du chargement"), Toast.LENGTH_LONG).show();
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
