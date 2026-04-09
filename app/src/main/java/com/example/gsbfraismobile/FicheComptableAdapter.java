package com.example.gsbfraismobile;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FicheComptableAdapter extends RecyclerView.Adapter<FicheComptableAdapter.ViewHolder> {

    private final Context context;
    private final List<FicheComptable> fiches;
    private final SessionManager sessionManager;
    private final Runnable onActionDone;

    public FicheComptableAdapter(Context context, List<FicheComptable> fiches, SessionManager sessionManager, Runnable onActionDone) {
        this.context = context;
        this.fiches = fiches;
        this.sessionManager = sessionManager;
        this.onActionDone = onActionDone;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fiche_comptable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FicheComptable fiche = fiches.get(position);

        holder.textNumero.setText(fiche.getNumeroFiche());
        holder.textUtilisateur.setText(context.getString(R.string.visiteur_label, fiche.getNomComplet()));
        holder.textMois.setText(context.getString(R.string.mois_label, fiche.getMois()));
        holder.textMontant.setText(context.getString(
                R.string.montant_label,
                String.format(Locale.FRANCE, "%.2f", fiche.getMontantTotal())
        ));

        holder.btnValider.setOnClickListener(v -> envoyerDecision(fiche.getId(), "valider", ""));
        holder.btnRefuser.setOnClickListener(v -> demanderRefus(fiche.getId()));
    }

    @Override
    public int getItemCount() {
        return fiches.size();
    }

    private void demanderRefus(int idFiche) {
        EditText input = new EditText(context);
        input.setHint(context.getString(R.string.commentaire_obligatoire));

        new AlertDialog.Builder(context)
                .setTitle(R.string.refuser_fiche)
                .setView(input)
                .setPositiveButton(R.string.refuser, (dialog, which) -> {
                    String commentaire = input.getText().toString().trim();
                    if (commentaire.isEmpty()) {
                        Toast.makeText(context, R.string.commentaire_obligatoire, Toast.LENGTH_LONG).show();
                        return;
                    }
                    envoyerDecision(idFiche, "refuser", commentaire);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void envoyerDecision(int idFiche, String action, String commentaire) {
        try {
            JSONObject body = new JSONObject();
            body.put("id_fiche", idFiche);
            body.put("action", action);
            body.put("commentaire", commentaire);

            String url = ApiConfig.BASE_URL + "fiches_comptable.php";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        Toast.makeText(context, response.optString("message", context.getString(R.string.action_effectuee)), Toast.LENGTH_LONG).show();
                        onActionDone.run();
                    },
                    error -> Toast.makeText(context, R.string.erreur_reseau_comptable, Toast.LENGTH_LONG).show()
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + sessionManager.getToken());
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleySingleton.getInstance(context).addToRequestQueue(request);

        } catch (Exception e) {
            Toast.makeText(context, R.string.erreur_traitement, Toast.LENGTH_LONG).show();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNumero;
        TextView textUtilisateur;
        TextView textMois;
        TextView textMontant;
        Button btnValider;
        Button btnRefuser;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNumero = itemView.findViewById(R.id.textNumeroComptable);
            textUtilisateur = itemView.findViewById(R.id.textUtilisateurComptable);
            textMois = itemView.findViewById(R.id.textMoisComptable);
            textMontant = itemView.findViewById(R.id.textMontantComptable);
            btnValider = itemView.findViewById(R.id.btnValider);
            btnRefuser = itemView.findViewById(R.id.btnRefuser);
        }
    }
}
