package com.example.gsbfraismobile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AjouterHorsForfaitActivity extends AppCompatActivity {

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private SessionManager sessionManager;
    private int ficheId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_hors_forfait);

        sessionManager = new SessionManager(this);
        ficheId = getIntent().getIntExtra("fiche_id", 0);

        if (ficheId <= 0) {
            Toast.makeText(this, R.string.fiche_introuvable, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        EditText editTypeConsommation = findViewById(R.id.editTypeConsommation);
        EditText editDateDepense = findViewById(R.id.editDateDepense);
        EditText editLibelle = findViewById(R.id.editLibelle);
        EditText editMontant = findViewById(R.id.editMontant);
        EditText editCommentaireHF = findViewById(R.id.editCommentaireHF);
        Button btnEnregistrerHF = findViewById(R.id.btnEnregistrerHF);

        btnEnregistrerHF.setOnClickListener(v -> {
            String typeConsommation = editTypeConsommation.getText().toString().trim();
            String dateDepense = editDateDepense.getText().toString().trim();
            String libelle = editLibelle.getText().toString().trim();
            String montantStr = editMontant.getText().toString().trim();
            String commentaire = editCommentaireHF.getText().toString().trim();

            if (typeConsommation.isEmpty() || dateDepense.isEmpty() || libelle.isEmpty() || montantStr.isEmpty()) {
                Toast.makeText(this, R.string.champs_obligatoires, Toast.LENGTH_LONG).show();
                return;
            }

            if (!DATE_PATTERN.matcher(dateDepense).matches()) {
                Toast.makeText(this, R.string.date_format_invalide, Toast.LENGTH_LONG).show();
                return;
            }

            try {
                double montant = Double.parseDouble(montantStr.replace(',', '.'));

                JSONObject body = new JSONObject();
                body.put("id_fiche", ficheId);
                body.put("type_consommation", typeConsommation);
                body.put("date", dateDepense);
                body.put("libelle", libelle);
                body.put("montant", montant);
                body.put("commentaire", commentaire);

                String url = ApiConfig.BASE_URL + "hors_forfaits.php";

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        body,
                        response -> {
                            boolean succes = response.optBoolean("succes", false);
                            String message = response.optString("message", getString(R.string.hors_forfait_ajout_ok));

                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                            if (succes) {
                                finish();
                            }
                        },
                        error -> Toast.makeText(this, R.string.erreur_ajout_hors_forfait, Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, R.string.montant_invalide, Toast.LENGTH_LONG).show();
            }
        });
    }
}
