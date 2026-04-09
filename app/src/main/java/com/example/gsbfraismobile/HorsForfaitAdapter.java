package com.example.gsbfraismobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class HorsForfaitAdapter extends RecyclerView.Adapter<HorsForfaitAdapter.HorsForfaitViewHolder> {

    private final List<HorsForfait> items;

    public HorsForfaitAdapter(List<HorsForfait> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public HorsForfaitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hors_forfait, parent, false);
        return new HorsForfaitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorsForfaitViewHolder holder, int position) {
        HorsForfait item = items.get(position);

        holder.textType.setText("Type : " + item.getTypeConsommation());
        holder.textDate.setText("Date : " + item.getDate());
        holder.textLibelle.setText("Libellé : " + item.getLibelle());
        holder.textMontant.setText(String.format(Locale.FRANCE, "Montant : %.2f €", item.getMontant()));
        holder.textCommentaire.setText("Commentaire : " + item.getCommentaire());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HorsForfaitViewHolder extends RecyclerView.ViewHolder {
        TextView textType;
        TextView textDate;
        TextView textLibelle;
        TextView textMontant;
        TextView textCommentaire;

        public HorsForfaitViewHolder(@NonNull View itemView) {
            super(itemView);
            textType = itemView.findViewById(R.id.textType);
            textDate = itemView.findViewById(R.id.textDate);
            textLibelle = itemView.findViewById(R.id.textLibelle);
            textMontant = itemView.findViewById(R.id.textMontant);
            textCommentaire = itemView.findViewById(R.id.textCommentaire);
        }
    }
}