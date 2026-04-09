package com.example.gsbfraismobile;

import android.app.AlertDialog;
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
import java.util.Locale;
import java.util.Map;

public class DetailFicheActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private int ficheId;
    private boolean ficheModifiable = false;

    private final ArrayList<HorsForfait> listeHorsForfaits = new ArrayList<>();
    private HorsForfaitAdapter horsForfaitAdapter;

    private TextView textNumero;
    private TextView textMois;
    private TextView textMontant;
    private TextView textStatut;
    private TextView textCommentaireVisiteur;
    private TextView textCommentaireComptable;
    private TextView textAucunHorsForfait;
    private TextView textEtatEdition;
    private Button btnAjouterHorsForfait;
    private Button btnTransmettreFiche;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_fiche);

        sessionManager = new SessionManager(this);
        ficheId = getIntent().getIntExtra("fiche_id", 0);

        if (ficheId <= 0) {
            Toast.makeText(this, R.string.fiche_introuvable, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textNumero = findViewById(R.id.textDetailNumero);
        textMois = findViewById(R.id.textDetailMois);
        textMontant = findViewById(R.id.textDetailMontant);
        textStatut = findViewById(R.id.textDetailStatut);
        textCommentaireVisiteur = findViewById(R.id.textCommentaireVisiteur);
        textCommentaireComptable = findViewById(R.id.textCommentaireComptable);
        textAucunHorsForfait = findViewById(R.id.textAucunHorsForfait);
        textEtatEdition = findViewById(R.id.textEtatEdition);
        btnAjouterHorsForfait = findViewById(R.id.btnAjouterHorsForfait);
        btnTransmettreFiche = findViewById(R.id.btnTransmettreFiche);
        Button btnRetour = findViewById(R.id.btnRetour);
        RecyclerView recyclerHorsForfaits = findViewById(R.id.recyclerHorsForfaits);

        recyclerHorsForfaits.setLayoutManager(new LinearLayoutManager(this));
        horsForfaitAdapter = new HorsForfaitAdapter(listeHorsForfaits);
        recyclerHorsForfaits.setAdapter(horsForfaitAdapter);

        btnRetour.setOnClickListener(v -> finish());

        btnAjouterHorsForfait.setOnClickListener(v -> {
            if (!ficheModifiable) {
                Toast.makeText(this, R.string.fiche_non_modifiable, Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(this, AjouterHorsForfaitActivity.class);
            intent.putExtra("fiche_id", ficheId);
            startActivity(intent);
        });

        btnTransmettreFiche.setOnClickListener(v -> confirmerTransmission());
    }

    @Override
    protected void onResume() {
        super.onResume();
        rafraichirEcran();
    }

    private void rafraichirEcran() {
        chargerDetailFiche();
        chargerHorsForfaits();
    }

    private void chargerDetailFiche() {
        String url = ApiConfig.BASE_URL + "fiches.php?id=" + ficheId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean succes = response.optBoolean("succes", false);
                        if (!succes) {
                            Toast.makeText(this, R.string.fiche_introuvable, Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONObject fiche = response.getJSONObject("fiche");
                        String statut = fiche.optString("statut", "");

                        textNumero.setText(fiche.optString("numero_fiche"));
                        textMois.setText(getString(R.string.detail_mois, fiche.optString("mois")));
                        textMontant.setText(getString(
                                R.string.detail_montant,
                                String.format(Locale.FRANCE, "%.2f", fiche.optDouble("montant_total"))
                        ));
                        textStatut.setText(getString(R.string.detail_statut, statut));
                        textCommentaireVisiteur.setText(getString(
                                R.string.detail_commentaire_visiteur,
                                valeurOuDefaut(fiche.optString("commentaire_visiteur", ""))
                        ));
                        textCommentaireComptable.setText(getString(
                                R.string.detail_commentaire_comptable,
                                valeurOuDefaut(fiche.optString("commentaire_comptable", ""))
                        ));

                        ficheModifiable = estStatutModifiable(statut);
                        appliquerEtatEdition();

                    } catch (Exception e) {
                        Toast.makeText(this, R.string.erreur_json_detail_fiche, Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, R.string.erreur_reseau_detail_fiche, Toast.LENGTH_LONG).show()
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

    private void chargerHorsForfaits() {
        String url = ApiConfig.BASE_URL + "hors_forfaits.php?id_fiche=" + ficheId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    listeHorsForfaits.clear();

                    try {
                        boolean succes = response.optBoolean("succes", false);
                        if (!succes) {
                            textAucunHorsForfait.setVisibility(View.VISIBLE);
                            horsForfaitAdapter.notifyDataSetChanged();
                            return;
                        }

                        JSONArray items = response.optJSONArray("hors_forfaits");

                        if (items != null) {
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);

                                HorsForfait hf = new HorsForfait(
                                        obj.optInt("id"),
                                        obj.optString("type_consommation"),
                                        obj.optString("date"),
                                        obj.optString("libelle"),
                                        obj.optDouble("montant"),
                                        obj.optString("commentaire")
                                );

                                listeHorsForfaits.add(hf);
                            }
                        }

                        horsForfaitAdapter.notifyDataSetChanged();
                        textAucunHorsForfait.setVisibility(listeHorsForfaits.isEmpty() ? View.VISIBLE : View.GONE);

                    } catch (Exception e) {
                        textAucunHorsForfait.setVisibility(View.VISIBLE);
                        Toast.makeText(this, R.string.erreur_json_hors_forfaits, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    textAucunHorsForfait.setVisibility(View.VISIBLE);
                    Toast.makeText(this, R.string.erreur_reseau_hors_forfaits, Toast.LENGTH_LONG).show();
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

    private void confirmerTransmission() {
        if (!ficheModifiable) {
            Toast.makeText(this, R.string.fiche_non_modifiable, Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.transmettre_fiche)
                .setMessage(R.string.confirmation_transmission)
                .setPositiveButton(R.string.transmettre, (dialog, which) -> transmettreFiche())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void transmettreFiche() {
        try {
            JSONObject body = new JSONObject();
            body.put("action", "transmettre");
            body.put("id_fiche", ficheId);

            String url = ApiConfig.BASE_URL + "fiches.php";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        boolean succes = response.optBoolean("succes", true);
                        String message = response.optString("message", getString(R.string.fiche_transmise_ok));
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                        if (succes) {
                            rafraichirEcran();
                        }
                    },
                    error -> Toast.makeText(this, R.string.erreur_transmission_fiche, Toast.LENGTH_LONG).show()
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + sessionManager.getToken());
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
            Toast.makeText(this, R.string.erreur_transmission_fiche, Toast.LENGTH_LONG).show();
        }
    }

    private void appliquerEtatEdition() {
        btnAjouterHorsForfait.setEnabled(ficheModifiable);
        btnAjouterHorsForfait.setAlpha(ficheModifiable ? 1f : 0.5f);
        btnTransmettreFiche.setVisibility(ficheModifiable ? View.VISIBLE : View.GONE);
        textEtatEdition.setText(ficheModifiable ? R.string.fiche_brouillon_modifiable : R.string.fiche_non_modifiable);
    }

    private boolean estStatutModifiable(String statut) {
        String statutNormalise = statut == null ? "" : statut.trim().toLowerCase(Locale.ROOT);
        return "brouillon".equals(statutNormalise)
                || "cr".equals(statutNormalise)
                || "créée".equals(statutNormalise)
                || "cree".equals(statutNormalise);
    }

    private String valeurOuDefaut(String valeur) {
        if (valeur == null || valeur.trim().isEmpty()) {
            return getString(R.string.aucun_commentaire);
        }
        return valeur;
    }
}
