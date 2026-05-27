# PharmaPays - Gestion de pharmacie en ligne

## Projet

Application web de gestion de pharmacie (épreuve E6 BTS SIO).
SPA frontend (HTML/CSS/JS vanilla) + API REST PHP + MySQL/MariaDB.

## Stack

- **Frontend** : `index.html` (fichier unique SPA, JS vanilla avec Fetch API)
- **Backend** : PHP 8.1+ (API REST dans `api/`)
- **Base de données** : MySQL 8.0+ / MariaDB 10.4+ avec procédures stockées, triggers et vues
- **Pas de framework** : pas de Composer, pas de npm, tout est vanilla

## Structure

```
index.html          ← SPA (point d'entrée unique, tout le frontend)
config/db.php       ← Connexion PDO + constantes (ADMIN_CODE, upload)
includes/
  response.php      ← Helpers JSON (jsonSuccess / jsonError)
  session.php       ← Session sécurisée + contrôle des rôles
  helpers.php       ← CORS, journalisation, upload d'ordonnances
api/
  auth.php          ← Inscription / Connexion / Déconnexion / Me
  medicaments.php   ← CRUD médicaments + stock
  commandes.php     ← Création / historique / statut
  ordonnances.php   ← Upload + validation
  dashboard.php     ← Statistiques pro/admin
  users.php         ← Gestion utilisateurs (admin)
  logs.php          ← Journal d'activité (admin)
uploads/            ← Ordonnances (PDF/PNG/JPG)
sql/pharmapays.sql  ← Script SQL complet (tables, vues, triggers, procédures, données démo)
```

## Lancer le projet

```bash
# 1. Importer la base de données
mysql -u root -p < sql/pharmapays.sql

# 2. Configurer config/db.php (ou variables d'env DB_HOST, DB_USER, DB_PASSWORD, DB_NAME)

# 3. Démarrer le serveur
php -S localhost:8000
```

## Comptes de test

| Rôle | Login | Mot de passe |
|------|-------|-------------|
| Admin | admin | Admin1234! |
| Pro | pro | Pro12345! |
| Client | client | Client12! |

Code admin d'inscription : `PharmAdmin2024!`

## Rôles et permissions

- **Client** : catalogue, panier, commander, uploader ordonnance, historique
- **Professionnel** : tout client + CRUD médicaments, gestion stock, validation commandes/ordonnances, dashboard stats
- **Admin** : tout pro + gestion utilisateurs (rôles, suppression), logs

## API

Toutes les réponses : `{ "success": true, "data": ... }` ou `{ "success": false, "error": "..." }`.
Les endpoints utilisent `?action=` pour auth.php et les méthodes HTTP (GET/POST/PUT/DELETE) pour le reste.

## Conventions de code

- PHP : requêtes PDO préparées uniquement (jamais de concaténation SQL)
- Mots de passe : `password_hash()` bcrypt coût 12
- Sessions : HttpOnly + SameSite=Lax
- Uploads : validation extension + MIME + taille max 5 Mo
- Toute action sensible est journalisée via `logAction()` / procédure `sp_log`
- Frontend : manipulation DOM directe, pas de framework JS

## Base de données

- 11 procédures stockées (sp_inscrire_utilisateur, sp_creer_commande, sp_maj_stock, etc.)
- 2 triggers (trg_stock_sortie, trg_stock_retour)
- 4 vues (v_catalogue, v_commandes_detail, v_ordonnances, v_top_medicaments)
- Relations : users ↔ profils (1:1), commandes ↔ medicaments (N:M via ligne_commande), commandes ↔ ordonnances (1:1)

## Sécurité

- Ne jamais renvoyer password_hash au client
- Toujours utiliser requireAuth() / requireRole() pour protéger les endpoints
- Échapper les sorties côté client (XSS)
- Les .htaccess bloquent l'accès direct à config/, includes/, sql/
