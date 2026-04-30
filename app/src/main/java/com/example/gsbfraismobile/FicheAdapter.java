package com.example.gsbfraismobile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FicheAdapter extends RecyclerView.Adapter<FicheAdapter.ViewHolder> {

    private final Context context;
    private final List<Fiche> fiches;

    public FicheAdapter(Context context, List<Fiche> fiches) {
        this.context = context;
        this.fiches = fiches;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fiche, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Fiche fiche = fiches.get(position);
        holder.textNumero.setText(fiche.getNumeroFiche());
        holder.textMois.setText(context.getString(R.string.mois_label, fiche.getMois()));
        holder.textMontant.setText(context.getString(R.string.montant_label, UiHelper.formatMontant(fiche.getMontantTotal())));
        holder.textStatut.setText(context.getString(R.string.etat_label, UiHelper.statutAvecEmoji(fiche.getStatut())));
        holder.textAction.setText(UiHelper.estFicheModifiable(fiche.getStatut()) ? "Touchez pour compléter ou transmettre." : "Touchez pour consulter le détail et l'état." );

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailFicheActivity.class);
            intent.putExtra("fiche_id", fiche.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return fiches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNumero, textMois, textMontant, textStatut, textAction;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNumero = itemView.findViewById(R.id.textNumero);
            textMois = itemView.findViewById(R.id.textMois);
            textMontant = itemView.findViewById(R.id.textMontant);
            textStatut = itemView.findViewById(R.id.textStatut);
            textAction = itemView.findViewById(R.id.textAction);
        }
    }
}
