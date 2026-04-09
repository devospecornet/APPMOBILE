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
import java.util.Locale;

public class FicheAdapter extends RecyclerView.Adapter<FicheAdapter.FicheViewHolder> {

    private final Context context;
    private final List<Fiche> fiches;

    public FicheAdapter(Context context, List<Fiche> fiches) {
        this.context = context;
        this.fiches = fiches;
    }

    @NonNull
    @Override
    public FicheViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fiche, parent, false);
        return new FicheViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FicheViewHolder holder, int position) {
        Fiche fiche = fiches.get(position);

        holder.textNumero.setText(fiche.getNumeroFiche());
        holder.textMois.setText("Mois : " + fiche.getMois());
        holder.textMontant.setText(String.format(Locale.FRANCE, "Montant : %.2f €", fiche.getMontantTotal()));
        holder.textStatut.setText("Statut : " + fiche.getStatut());

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

    static class FicheViewHolder extends RecyclerView.ViewHolder {
        TextView textNumero;
        TextView textMois;
        TextView textMontant;
        TextView textStatut;

        public FicheViewHolder(@NonNull View itemView) {
            super(itemView);
            textNumero = itemView.findViewById(R.id.textNumero);
            textMois = itemView.findViewById(R.id.textMois);
            textMontant = itemView.findViewById(R.id.textMontant);
            textStatut = itemView.findViewById(R.id.textStatut);
        }
    }
}