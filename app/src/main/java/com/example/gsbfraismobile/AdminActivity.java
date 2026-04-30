package com.example.gsbfraismobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class AdminActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private final ArrayList<UserItem> users = new ArrayList<>();
    private final ArrayList<FicheAdmin> fiches = new ArrayList<>();
    private UserAdapter userAdapter;
    private FicheAdminAdapter ficheAdminAdapter;
    private TextView textAucuneFicheAdmin;
    private TextView textAdminConnected;
    private TextView textResumeAdmin;
    private EditText editNom;
    private EditText editPrenom;
    private EditText editEmail;
    private EditText editMdp;
    private Spinner spinnerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) { startActivity(new Intent(this, LoginActivity.class)); finish(); return; }
        if (!"administrateur".equals(sessionManager.getUserRole())) { Toast.makeText(this, R.string.acces_refuse, Toast.LENGTH_LONG).show(); finish(); return; }
        setContentView(R.layout.activity_admin);

        Button btnDeconnexion = findViewById(R.id.btnDeconnexionAdmin);
        Button btnCreer = findViewById(R.id.btnCreerUtilisateurAdmin);
        Button btnActualiser = findViewById(R.id.btnActualiserAdmin);
        editNom = findViewById(R.id.editNomAdmin);
        editPrenom = findViewById(R.id.editPrenomAdmin);
        editEmail = findViewById(R.id.editEmailAdmin);
        editMdp = findViewById(R.id.editMdpAdmin);
        spinnerRole = findViewById(R.id.spinnerRoleAdmin);
        textAucuneFicheAdmin = findViewById(R.id.textAucuneFicheAdmin);
        textAdminConnected = findViewById(R.id.textAdminConnected);
        textResumeAdmin = findViewById(R.id.textResumeAdmin);
        textAdminConnected.setText(getString(R.string.connecte_en_tant_que, sessionManager.getUserName()));

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this, R.array.roles_utilisateur, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        RecyclerView recyclerUsers = findViewById(R.id.recyclerAdminUsers);
        RecyclerView recyclerFiches = findViewById(R.id.recyclerAdminFiches);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerFiches.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(users, this::supprimerUtilisateur);
        ficheAdminAdapter = new FicheAdminAdapter(this, fiches, sessionManager, this::chargerToutesLesDonnees);
        recyclerUsers.setAdapter(userAdapter);
        recyclerFiches.setAdapter(ficheAdminAdapter);

        btnActualiser.setOnClickListener(v -> chargerToutesLesDonnees());
        btnDeconnexion.setOnClickListener(v -> LogoutHelper.logout(this, sessionManager));
        btnCreer.setOnClickListener(v -> {
            String nom = editNom.getText().toString().trim();
            String prenom = editPrenom.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String mdp = editMdp.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
                Toast.makeText(this, R.string.tous_champs_obligatoires, Toast.LENGTH_LONG).show();
                return;
            }
            creerUtilisateur(nom, prenom, email, mdp, role);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        chargerToutesLesDonnees();
    }

    private void chargerToutesLesDonnees() {
        chargerUtilisateurs();
        chargerFichesAdmin();
    }

    private void mettreAJourResume() {
        textResumeAdmin.setText(getString(R.string.resume_admin, users.size(), fiches.size()));
        textAucuneFicheAdmin.setVisibility(fiches.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void chargerUtilisateurs() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ApiConfig.BASE_URL + "utilisateurs.php", null,
                response -> {
                    users.clear();
                    try {
                        if (!response.optBoolean("succes", false)) {
                            Toast.makeText(this, response.optString("message", getString(R.string.erreur_json_admin_utilisateurs)), Toast.LENGTH_LONG).show();
                            return;
                        }
                        JSONArray items = response.optJSONArray("utilisateurs");
                        if (items != null) {
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                String nomComplet = obj.optString("prenom", "") + " " + obj.optString("nom", "");
                                users.add(new UserItem(obj.optInt("id"), nomComplet.trim(), obj.optString("email"), obj.optString("role")));
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                        mettreAJourResume();
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.erreur_json_admin_utilisateurs, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    if (ApiErrorHelper.isAuthError(error)) { ApiErrorHelper.forceLogin(this, sessionManager, ApiErrorHelper.extractMessage(error, "Session expirée.")); return; }
                    Toast.makeText(this, ApiErrorHelper.extractMessage(error, getString(R.string.erreur_reseau_admin_utilisateurs)), Toast.LENGTH_LONG).show();
                }) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError { Map<String, String> headers = new HashMap<>(); headers.put("Authorization", "Bearer " + sessionManager.getToken()); return headers; }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void creerUtilisateur(String nom, String prenom, String email, String mdp, String role) {
        try {
            JSONObject body = new JSONObject();
            body.put("nom", nom);
            body.put("prenom", prenom);
            body.put("email", email);
            body.put("mot_de_passe", mdp);
            body.put("role", role);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiConfig.BASE_URL + "utilisateurs.php", body,
                    response -> {
                        boolean succes = response.optBoolean("succes", false);
                        Toast.makeText(this, response.optString("message", getString(R.string.utilisateur_cree)), Toast.LENGTH_LONG).show();
                        if (succes) {
                            editNom.setText(""); editPrenom.setText(""); editEmail.setText(""); editMdp.setText(""); spinnerRole.setSelection(0);
                            chargerUtilisateurs();
                        }
                    },
                    error -> {
                        if (ApiErrorHelper.isAuthError(error)) { ApiErrorHelper.forceLogin(this, sessionManager, ApiErrorHelper.extractMessage(error, "Session expirée.")); return; }
                        Toast.makeText(this, ApiErrorHelper.extractMessage(error, getString(R.string.erreur_creation_utilisateur)), Toast.LENGTH_LONG).show();
                    }) {
                @Override public Map<String, String> getHeaders() throws AuthFailureError { Map<String, String> headers = new HashMap<>(); headers.put("Authorization", "Bearer " + sessionManager.getToken()); headers.put("Content-Type", "application/json"); return headers; }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            Toast.makeText(this, R.string.erreur_creation_utilisateur, Toast.LENGTH_LONG).show();
        }
    }

    private void supprimerUtilisateur(UserItem user) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, ApiConfig.BASE_URL + "utilisateurs.php?id=" + user.getId(), null,
                response -> { Toast.makeText(this, response.optString("message", getString(R.string.utilisateur_supprime)), Toast.LENGTH_LONG).show(); chargerUtilisateurs(); },
                error -> {
                    if (ApiErrorHelper.isAuthError(error)) { ApiErrorHelper.forceLogin(this, sessionManager, ApiErrorHelper.extractMessage(error, "Session expirée.")); return; }
                    Toast.makeText(this, ApiErrorHelper.extractMessage(error, getString(R.string.erreur_suppression_utilisateur)), Toast.LENGTH_LONG).show();
                }) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError { Map<String, String> headers = new HashMap<>(); headers.put("Authorization", "Bearer " + sessionManager.getToken()); return headers; }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void chargerFichesAdmin() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ApiConfig.BASE_URL + "admin_fiches.php", null,
                response -> {
                    fiches.clear();
                    try {
                        if (!response.optBoolean("succes", false)) {
                            textAucuneFicheAdmin.setVisibility(View.VISIBLE);
                            Toast.makeText(this, response.optString("message", getString(R.string.erreur_json_admin_fiches)), Toast.LENGTH_LONG).show();
                            return;
                        }
                        JSONArray items = response.optJSONArray("fiches");
                        if (items != null) {
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                String nomComplet = obj.optString("prenom", "") + " " + obj.optString("nom", "");
                                fiches.add(new FicheAdmin(obj.optInt("id"), obj.optString("numero_fiche"), obj.optString("mois"), obj.optDouble("montant_total"), obj.optString("statut"), nomComplet.trim()));
                            }
                        }
                        ficheAdminAdapter.notifyDataSetChanged();
                        mettreAJourResume();
                    } catch (Exception e) {
                        textAucuneFicheAdmin.setVisibility(View.VISIBLE);
                        Toast.makeText(this, R.string.erreur_json_admin_fiches, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    textAucuneFicheAdmin.setVisibility(View.VISIBLE);
                    if (ApiErrorHelper.isAuthError(error)) { ApiErrorHelper.forceLogin(this, sessionManager, ApiErrorHelper.extractMessage(error, "Session expirée.")); return; }
                    Toast.makeText(this, ApiErrorHelper.extractMessage(error, getString(R.string.erreur_reseau_admin_fiches)), Toast.LENGTH_LONG).show();
                }) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError { Map<String, String> headers = new HashMap<>(); headers.put("Authorization", "Bearer " + sessionManager.getToken()); return headers; }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
