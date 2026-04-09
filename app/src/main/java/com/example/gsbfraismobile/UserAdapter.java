package com.example.gsbfraismobile;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface OnUserDeleteListener {
        void onDelete(UserItem user);
    }

    private final List<UserItem> users;
    private final OnUserDeleteListener onUserDeleteListener;

    public UserAdapter(List<UserItem> users, OnUserDeleteListener onUserDeleteListener) {
        this.users = users;
        this.onUserDeleteListener = onUserDeleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserItem user = users.get(position);

        holder.textNom.setText(user.getNomComplet());
        holder.textEmail.setText("Email : " + user.getEmail());
        holder.textRole.setText("Rôle : " + user.getRole());

        holder.btnSupprimer.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle("Supprimer l'utilisateur")
                .setMessage("Confirmer la suppression de " + user.getNomComplet() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> onUserDeleteListener.onDelete(user))
                .setNegativeButton("Annuler", null)
                .show());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNom, textEmail, textRole;
        Button btnSupprimer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNom = itemView.findViewById(R.id.textNomUser);
            textEmail = itemView.findViewById(R.id.textEmailUser);
            textRole = itemView.findViewById(R.id.textRoleUser);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimerUser);
        }
    }
}