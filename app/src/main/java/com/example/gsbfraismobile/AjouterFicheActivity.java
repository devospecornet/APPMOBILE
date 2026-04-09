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

public class AjouterFicheActivity extends AppCompatActivity {

    private static final Pattern MOIS_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");

    private EditText editMois;
    private EditText editEssence;
    private EditText editPetitDej;
    private EditText editRepasMidi;
    private EditText editRepasSoir;
    private EditText editHotel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_fiche);

        sessionManager = new SessionManager(this);

        editMois = findViewById(R.id.editMois);
        editEssence = findViewById(R.id.editEssence);
        editPetitDej = findViewById(R.id.editPetitDej);
        editRepasMidi = findViewById(R.id.editRepasMidi);
        editRepasSoir = findViewById(R.id.editRepasSoir);
        editHotel = findViewById(R.id.editHotel);
        Button btnEnregistrer = findViewById(R.id.btnEnregistrerFiche);

        btnEnregistrer.setOnClickListener(v -> enregistrerFiche());
    }

    private void enregistrerFiche() {
        String mois = editMois.getText().toString().trim();

        if (mois.isEmpty()) {
            Toast.makeText(this, R.string.mois_obligatoire, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!MOIS_PATTERN.matcher(mois).matches()) {
            Toast.makeText(this, R.string.mois_format_invalide, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("mois", mois);
            body.put("frais_essence", parseDouble(editEssence.getText().toString()));
            body.put("frais_petit_dejeuner", parseDouble(editPetitDej.getText().toString()));
            body.put("frais_repas_midi", parseDouble(editRepasMidi.getText().toString()));
            body.put("frais_repas_soir", parseDouble(editRepasSoir.getText().toString()));
            body.put("frais_hotel", parseDouble(editHotel.getText().toString()));

            String url = ApiConfig.BASE_URL + "fiches.php";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        boolean succes = response.optBoolean("succes", true);
                        String message = response.optString("message", getString(R.string.fiche_creation_ok));

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                        if (succes) {
                            finish();
                        }
                    },
                    error -> Toast.makeText(this, R.string.erreur_creation_fiche, Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, R.string.erreur_saisie, Toast.LENGTH_LONG).show();
        }
    }

    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        return Double.parseDouble(value.replace(',', '.'));
    }
}
