package com.example.gsbfraismobile;

public class FicheAdmin {
    private final int id;
    private final String numeroFiche;
    private final String mois;
    private final double montantTotal;
    private final String statut;
    private final String nomComplet;

    public FicheAdmin(int id, String numeroFiche, String mois, double montantTotal, String statut, String nomComplet) {
        this.id = id;
        this.numeroFiche = numeroFiche;
        this.mois = mois;
        this.montantTotal = montantTotal;
        this.statut = statut;
        this.nomComplet = nomComplet;
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

    public String getNomComplet() {
        return nomComplet;
    }
}