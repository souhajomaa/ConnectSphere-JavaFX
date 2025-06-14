# Fonctionnalité de Following - Connect Sphere

## Vue d'ensemble

Cette fonctionnalité permet aux utilisateurs de suivre d'autres utilisateurs via un système de demandes d'amitié. Lorsqu'un utilisateur clique sur "Follow", une demande est envoyée à l'utilisateur cible qui peut l'accepter ou la refuser.

## Fichiers créés/modifiés

### Nouveaux fichiers créés :

1. **FollowRequest.java** - Modèle de données pour les demandes de suivi
   - Représente une demande de suivi avec les champs : id, senderId, receiverId, status, requestDate
   - Statuts possibles : PENDING, ACCEPTED, REJECTED

2. **FollowRequestDAO.java** - Couche d'accès aux données
   - Gère toutes les opérations de base de données pour les demandes de suivi
   - Méthodes principales :
     - `sendFollowRequest()` - Envoie une nouvelle demande
     - `acceptFollowRequest()` - Accepte une demande
     - `rejectFollowRequest()` - Rejette une demande
     - `getPendingRequests()` - Récupère les demandes en attente
     - `isFollowing()` - Vérifie si un utilisateur suit un autre
     - `getFollowerCount()` / `getFollowingCount()` - Compteurs

3. **FollowRequestController.java** - Contrôleur pour l'interface des demandes
   - Gère l'affichage et les interactions avec les demandes de suivi
   - Charge dynamiquement les demandes en attente
   - Permet d'accepter/rejeter les demandes

4. **FollowRequests.fxml** - Interface utilisateur pour les demandes
   - Page dédiée à la gestion des demandes de suivi
   - Affichage des demandes avec photos de profil, noms et actions

### Fichiers modifiés :

1. **SocialController.java**
   - Ajout de `FollowRequestDAO` comme variable d'instance
   - Modification de `handleFollowButton()` pour implémenter la logique de demande
   - Ajout de méthodes utilitaires : `showError()`, `showSuccess()`, `showInfo()`
   - Ajout de `onFollowRequestsClick()` pour la navigation
   - Ajout de l'import `Platform` pour les alertes

2. **UserProfile.fxml**
   - Ajout du bouton "Follow Requests" dans la barre de navigation
   - Lien vers la page de gestion des demandes

## Fonctionnalités implémentées

### 1. Envoi de demandes de suivi
- Cliquer sur "Follow" envoie une demande à l'utilisateur cible
- Vérifications automatiques :
  - Empêche de se suivre soi-même
  - Détecte si l'utilisateur est déjà suivi
  - Évite les demandes en double

### 2. Gestion des demandes
- Page dédiée accessible via "Follow Requests" dans la navigation
- Affichage des demandes en attente avec :
  - Photo de profil du demandeur
  - Nom d'utilisateur et email
  - Date de la demande
  - Boutons "Accept" et "Reject"

### 3. Base de données
- Table `follow_requests` créée automatiquement
- Gestion des relations utilisateur-suiveur
- Suivi des statuts des demandes

## Structure de la base de données

La fonctionnalité utilise la table existante `UserFollowers` avec la structure suivante:

```sql
create table UserFollowers 
( 
    id          int auto_increment 
        primary key, 
    follower_id int                           not null, 
    followed_id int                           not null, 
    created_at  datetime                      not null, 
    status      varchar(20) default 'pending' not null, 
    constraint fk_followed 
        foreign key (followed_id) references users (id) 
            on delete cascade, 
    constraint fk_follower 
        foreign key (follower_id) references users (id) 
            on delete cascade 
);
```

## Utilisation

1. **Pour suivre un utilisateur :**
   - Naviguer vers le profil de l'utilisateur
   - Cliquer sur le bouton "Follow"
   - Une demande sera envoyée

2. **Pour gérer les demandes reçues :**
   - Cliquer sur "Follow Requests" dans la navigation
   - Voir toutes les demandes en attente
   - Accepter ou rejeter selon votre choix

3. **Retour au feed :**
   - Utiliser le bouton "← Back" pour revenir au feed principal

## Sécurité et validations

- Vérification de l'authentification utilisateur
- Prévention des demandes en double
- Validation des IDs utilisateur
- Gestion des erreurs de base de données
- Interface utilisateur responsive avec messages d'erreur/succès

## Améliorations futures possibles

1. Notifications en temps réel pour les nouvelles demandes
2. Compteurs de followers/following sur les profils
3. Feed personnalisé basé sur les utilisateurs suivis