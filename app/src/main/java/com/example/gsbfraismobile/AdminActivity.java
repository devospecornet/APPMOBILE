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
    private EditText editNom;
    private EditText editPrenom;
    private EditText editEmail;
    private EditText editMdp;
    private Spinner spinnerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sessionManager = new SessionManager(this);

        if (!"administrateur".equals(sessionManager.getUserRole())) {
            Toast.makeText(this, R.string.acces_refuse, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Button btnDeconnexion = findViewById(R.id.btnDeconnexionAdmin);
        Button btnCreer = findViewById(R.id.btnCreerUtilisateurAdmin);

        editNom = findViewById(R.id.editNomAdmin);
        editPrenom = findViewById(R.id.editPrenomAdmin);
        editEmail = findViewById(R.id.editEmailAdmin);
        editMdp = findViewById(R.id.editMdpAdmin);
        spinnerRole = findViewById(R.id.spinnerRoleAdmin);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.roles_utilisateur,
                android.R.layout.simple_spinner_item
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        RecyclerView recyclerUsers = findViewById(R.id.recyclerAdminUsers);
        RecyclerView recyclerFiches = findViewById(R.id.recyclerAdminFiches);
        textAucuneFicheAdmin = findViewById(R.id.textAucuneFicheAdmin);

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerFiches.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(users, this::supprimerUtilisateur);
        ficheAdminAdapter = new FicheAdminAdapter(this, fiches, sessionManager, this::chargerFichesAdmin);

        recyclerUsers.setAdapter(userAdapter);
        recyclerFiches.setAdapter(ficheAdminAdapter);

        btnDeconnexion.setOnClickListener(v -> {
            sessionManager.clearSession();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

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
        chargerUtilisateurs();
        chargerFichesAdmin();
    }

    private void chargerUtilisateurs() {
        String url = ApiConfig.BASE_URL + "utilisateurs.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    users.clear();

                    try {
                        JSONArray items = response.optJSONArray("utilisateurs");
                        if (items != null) {
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                String nomComplet = obj.optString("prenom", "") + " " + obj.optString("nom", "");

                                users.add(new UserItem(
                                        obj.optInt("id"),
                                        nomComplet.trim(),
                                        obj.optString("email"),
                                        obj.optString("role")
                                ));
                            }
                        }

                        userAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(this, R.string.erreur_json_admin_utilisateurs, Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, R.string.erreur_reseau_admin_utilisateurs, Toast.LENGTH_LONG).show()
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

    private void creerUtilisateur(String nom, String prenom, String email, String mdp, String role) {
        try {
            JSONObject body = new JSONObject();
            body.put("action", "create");
            body.put("nom", nom);
            body.put("prenom", prenom);
            body.put("email", email);
            body.put("mot_de_passe", mdp);
            body.put("role", role);

            String url = ApiConfig.BASE_URL + "utilisateurs.php";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this, response.optString("message", getString(R.string.utilisateur_cree)), Toast.LENGTH_LONG).show();
                        viderFormulaireUtilisateur();
                        chargerUtilisateurs();
                    },
                    error -> Toast.makeText(this, R.string.erreur_creation_utilisateur, Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, R.string.erreur_creation_utilisateur, Toast.LENGTH_LONG).show();
        }
    }

    private void supprimerUtilisateur(UserItem user) {
        try {
            JSONObject body = new JSONObject();
            body.put("action", "delete");
            body.put("id_utilisateur", user.getId());

            String url = ApiConfig.BASE_URL + "utilisateurs.php";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this, response.optString("message", getString(R.string.utilisateur_supprime)), Toast.LENGTH_LONG).show();
                        chargerUtilisateurs();
                    },
                    error -> Toast.makeText(this, R.string.erreur_suppression_utilisateur, Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, R.string.erreur_suppression_utilisateur, Toast.LENGTH_LONG).show();
        }
    }

    private void chargerFichesAdmin() {
        String url = ApiConfig.BASE_URL + "admin_fiches.php";

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

                                fiches.add(new FicheAdmin(
                                        obj.optInt("id"),
                                        obj.optString("numero_fiche"),
                                        obj.optString("mois"),
                                        obj.optDouble("montant_total"),
                                        obj.optString("statut"),
                                        nomComplet.trim()
                                ));
                            }
                        }

                        ficheAdminAdapter.notifyDataSetChanged();
                        textAucuneFicheAdmin.setVisibility(fiches.isEmpty() ? View.VISIBLE : View.GONE);

                    } catch (Exception e) {
                        textAucuneFicheAdmin.setVisibility(View.VISIBLE);
                        Toast.makeText(this, R.string.erreur_json_admin_fiches, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    textAucuneFicheAdmin.setVisibility(View.VISIBLE);
                    Toast.makeText(this, R.string.erreur_reseau_admin_fiches, Toast.LENGTH_LONG).show();
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

    private void viderFormulaireUtilisateur() {
        editNom.setText("");
        editPrenom.setText("");
        editEmail.setText("");
        editMdp.setText("");
        spinnerRole.setSelection(0);
    }
}
