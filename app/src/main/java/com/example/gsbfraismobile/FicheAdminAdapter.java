package com.example.gsbfraismobile;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

public class FicheAdminAdapter extends RecyclerView.Adapter<FicheAdminAdapter.ViewHolder> {

    private final Context context;
    private final List<FicheAdmin> fiches;
    private final SessionManager sessionManager;
    private final Runnable onActionDone;

    public FicheAdminAdapter(Context context, List<FicheAdmin> fiches, SessionManager sessionManager, Runnable onActionDone) {
        this.context = context;
        this.fiches = fiches;
        this.sessionManager = sessionManager;
        this.onActionDone = onActionDone;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fiche_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FicheAdmin fiche = fiches.get(position);

        holder.textNumero.setText(fiche.getNumeroFiche());
        holder.textUtilisateur.setText("Visiteur : " + fiche.getNomComplet());
        holder.textMois.setText("Mois : " + fiche.getMois());
        holder.textMontant.setText(String.format(Locale.FRANCE, "Montant : %.2f €", fiche.getMontantTotal()));
        holder.textStatut.setText("Statut : " + fiche.getStatut());

        holder.btnValider.setOnClickListener(v -> envoyerAction(fiche.getId(), "valider", ""));
        holder.btnRefuser.setOnClickListener(v -> demanderRefus(fiche.getId()));
        holder.btnSupprimer.setOnClickListener(v -> confirmerSuppression(fiche.getId()));
    }

    @Override
    public int getItemCount() {
        return fiches.size();
    }

    private void demanderRefus(int idFiche) {
        EditText input = new EditText(context);
        input.setHint(context.getString(R.string.commentaire_obligatoire));

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.refuser_fiche))
                .setView(input)
                .setPositiveButton(context.getString(R.string.refuser), (dialog, which) -> {
                    String commentaire = input.getText().toString().trim();
                    if (commentaire.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.commentaire_obligatoire), Toast.LENGTH_LONG).show();
                        return;
                    }
                    envoyerAction(idFiche, "refuser", commentaire);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void confirmerSuppression(int idFiche) {
        new AlertDialog.Builder(context)
                .setTitle("Supprimer la fiche")
                .setMessage("Confirmer la suppression de cette fiche ?")
                .setPositiveButton(context.getString(R.string.supprimer), (dialog, which) -> envoyerAction(idFiche, "supprimer", ""))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void envoyerAction(int idFiche, String action, String commentaire) {
        try {
            JSONObject body = new JSONObject();
            body.put("id_fiche", idFiche);
            body.put("action", action);
            body.put("commentaire", commentaire);

            String url = ApiConfig.BASE_URL + "admin_fiches.php";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        Toast.makeText(context, response.optString("message", "Action effectuée"), Toast.LENGTH_LONG).show();
                        onActionDone.run();
                    },
                    error -> Toast.makeText(context, "Erreur réseau admin fiches", Toast.LENGTH_LONG).show()
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
            Toast.makeText(context, "Erreur de traitement", Toast.LENGTH_LONG).show();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNumero, textUtilisateur, textMois, textMontant, textStatut;
        Button btnValider, btnRefuser, btnSupprimer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNumero = itemView.findViewById(R.id.textNumeroAdminFiche);
            textUtilisateur = itemView.findViewById(R.id.textUtilisateurAdminFiche);
            textMois = itemView.findViewById(R.id.textMoisAdminFiche);
            textMontant = itemView.findViewById(R.id.textMontantAdminFiche);
            textStatut = itemView.findViewById(R.id.textStatutAdminFiche);
            btnValider = itemView.findViewById(R.id.btnValiderAdmin);
            btnRefuser = itemView.findViewById(R.id.btnRefuserAdmin);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimerFicheAdmin);
        }
    }
}