package com.example.gsbfraismobile;

public class FicheComptable {
    private final int id;
    private final String numeroFiche;
    private final String mois;
    private final double montantTotal;
    private final String nomComplet;

    public FicheComptable(int id, String numeroFiche, String mois, double montantTotal, String nomComplet) {
        this.id = id;
        this.numeroFiche = numeroFiche;
        this.mois = mois;
        this.montantTotal = montantTotal;
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

    public String getNomComplet() {
        return nomComplet;
    }
}