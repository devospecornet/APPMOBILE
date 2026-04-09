# Modifications mobile - option B

## URL d'API utilisée par l'application Android

Le code Android modifié pointe maintenant vers :

```text
https://ecornetgsb.fr/api/
```

## Conséquence côté serveur

Pour que l'APK fonctionne en production, il faut exposer l'API PHP dans :

```text
public/api/
```

Comme ton domaine pointe déjà sur `public`, l'API sera alors accessible publiquement via :

```text
https://ecornetgsb.fr/api/
```

## Ce que j'ai ajouté côté mobile

- bouton **Transmettre la fiche** pour le visiteur
- blocage de l'ajout de hors forfait si la fiche n'est plus en brouillon
- sélection du rôle par **Spinner** dans l'espace administrateur
- nettoyage de l'écran d'ajout de fiche
- amélioration de la version comptable avec message si aucune fiche transmise
- configuration Android prête pour HTTPS

## Attention backend

Le bouton de transmission mobile envoie un `POST` vers :

```text
/api/fiches.php
```

avec ce JSON :

```json
{
  "action": "transmettre",
  "id_fiche": 123
}
```

Si ton endpoint `fiches.php` ne gère pas encore cette action, il faudra l'ajouter côté PHP.
