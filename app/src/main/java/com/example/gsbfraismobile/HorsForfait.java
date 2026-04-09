package com.example.gsbfraismobile;

public class HorsForfait {
    private final int id;
    private final String typeConsommation;
    private final String date;
    private final String libelle;
    private final double montant;
    private final String commentaire;

    public HorsForfait(int id, String typeConsommation, String date, String libelle, double montant, String commentaire) {
        this.id = id;
        this.typeConsommation = typeConsommation;
        this.date = date;
        this.libelle = libelle;
        this.montant = montant;
        this.commentaire = commentaire;
    }

    public int getId() {
        return id;
    }

    public String getTypeConsommation() {
        return typeConsommation;
    }

    public String getDate() {
        return date;
    }

    public String getLibelle() {
        return libelle;
    }

    public double getMontant() {
        return montant;
    }

    public String getCommentaire() {
        return commentaire;
    }
}