package com.example.gsbfraismobile;

import android.content.Context;

import java.util.Locale;

public final class UiHelper {
    private UiHelper() {}

    public static String formatMontant(double montant) {
        return String.format(Locale.FRANCE, "%.2f €", montant);
    }

    public static String statutLisible(String statut) {
        if (statut == null) return "Inconnu";
        switch (statut.toLowerCase(Locale.ROOT)) {
            case "saisie": return "Saisie";
            case "transmise": return "Transmise";
            case "validee": return "Validée";
            case "refusee": return "Refusée";
            case "remboursee": return "Remboursée";
            default: return statut.substring(0,1).toUpperCase(Locale.ROOT) + statut.substring(1);
        }
    }

    public static String statutAvecEmoji(String statut) {
        String libelle = statutLisible(statut);
        if (statut == null) return libelle;
        switch (statut.toLowerCase(Locale.ROOT)) {
            case "saisie": return "✏️ " + libelle;
            case "transmise": return "📤 " + libelle;
            case "validee": return "✅ " + libelle;
            case "refusee": return "⛔ " + libelle;
            case "remboursee": return "💶 " + libelle;
            default: return "• " + libelle;
        }
    }

    public static boolean estFicheModifiable(String statut) {
        if (statut == null) return false;
        String s = statut.toLowerCase(Locale.ROOT);
        return s.equals("saisie") || s.equals("refusee");
    }

    public static String nomOuRole(String nom, String role) {
        String n = nom == null ? "" : nom.trim();
        if (!n.isEmpty()) return n;
        return statutLisible(role);
    }
}
