package com.example.gsbfraismobile;

public final class ApiConfig {

    private ApiConfig() {
        // Classe utilitaire
    }

    // Option B choisie : l'API PHP est copiée dans public/api sur le serveur.
    // Comme le domaine pointe déjà sur public, l'URL finale devient /api/.
    // Si le SSL n'est pas encore actif, remplace temporairement https par http.
    public static final String BASE_URL = "https://ecornetgsb.fr/api/";
}
