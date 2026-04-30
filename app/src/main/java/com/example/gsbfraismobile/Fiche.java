package com.example.gsbfraismobile;

public class Fiche {
    private final int id;
    private final String numeroFiche;
    private final String mois;
    private final double montantTotal;
    private final String statut;
    private final String dateCreation;
    private final String commentaireVisiteur;
    private final String commentaireComptable;

    public Fiche(int id, String numeroFiche, String mois, double montantTotal, String statut,
                 String dateCreation, String commentaireVisiteur, String commentaireComptable) {
        this.id = id;
        this.numeroFiche = numeroFiche;
        this.mois = mois;
        this.montantTotal = montantTotal;
        this.statut = statut;
        this.dateCreation = dateCreation;
        this.commentaireVisiteur = commentaireVisiteur;
        this.commentaireComptable = commentaireComptable;
    }

    public int getId() {
        return id;
    }

    public String getNumeroFiche() {
        return numeroFiche;
    }

    public String getMois() {
        return mois;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public String getStatut() {
        return statut;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public String getCommentaireVisiteur() {
        return commentaireVisiteur;
    }

    public String getCommentaireComptable() {
        return commentaireComptable;
    }
}