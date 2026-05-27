# PharmaPays — Épreuve E6 BTS SIO

Application web distribuée de **gestion de pharmacie en ligne**.
Architecture **client–serveur** avec interface dynamique JavaScript, authentification sécurisée,
base de données relationnelle distante et procédures stockées SQL.

**Stack :** PHP 8.x · MySQL / MariaDB 8.x · HTML5 / CSS3 / JavaScript (Fetch API)

---

## Structure du projet

```
pharmapays/
├── index.html              ← SPA Frontend (point d'entrée unique)
├── .htaccess               ← Sécurité Apache (racine)
├── README.md
├── .vscode/
│   ├── settings.json
│   └── extensions.json
├── config/
│   ├── db.php              ← Connexion PDO + constantes (ADMIN_CODE, upload)
│   └── .htaccess           ← Bloque l'accès direct
├── includes/
│   ├── response.php        ← Helpers JSON (jsonSuccess / jsonError)
│   ├── session.php         ← Session sécurisée + contrôle des rôles
│   ├── helpers.php         ← CORS, journalisation, upload d'ordonnances
│   └── .htaccess
├── api/
│   ├── auth.php            ← Inscription / Connexion / Déconnexion / Me
│   ├── medicaments.php     ← CRUD médicaments + gestion du stock
│   ├── commandes.php       ← Création / historique / changement de statut
│   ├── ordonnances.php     ← Upload + validation des ordonnances
│   ├── dashboard.php       ← Statistiques pro/admin
│   ├── users.php           ← Gestion des utilisateurs (admin)
│   └── logs.php            ← Journal d'activité (admin)
├── uploads/                ← Ordonnances téléversées (PDF/PNG/JPG)
│   └── .htaccess           ← Interdit l'exécution de scripts
└── sql/
    └── pharmapays.sql      ← Script SQL complet (testé sur MariaDB 10.11)
```

---

## Installation

### 1. Prérequis
- PHP 8.1+ (extensions `pdo_mysql`, `fileinfo`)
- MySQL 8.0+ ou MariaDB 10.4+
- Apache avec `mod_rewrite` (ou XAMPP / WAMP / Laragon)

### 2. Base de données
```bash
mysql -u root -p < sql/pharmapays.sql
```
Le script crée la base `pharmapays`, toutes les tables, vues, triggers,
procédures stockées **et les données de démonstration**.

### 3. Configuration
Éditez `config/db.php` (ou définissez les variables d'environnement) :
```php
define('DB_HOST', 'localhost');
define('DB_NAME', 'pharmapays');
define('DB_USER', 'votre_user');
define('DB_PASS', 'votre_mot_de_passe');
```

### 4. Lancement
**XAMPP / Laragon :** copiez le dossier dans `htdocs/` (ou `www/`) puis ouvrez
`http://localhost/pharmapays/`.

**Serveur PHP intégré :**
```bash
cd pharmapays/
php -S localhost:8000
```
Puis `http://localhost:8000`.

> ⚠️ Les appels API nécessitent un serveur **PHP**. L'extension *Live Server*
> seule (HTML statique) ne suffit pas.

---

## Comptes de démonstration

| Rôle | Identifiant | Mot de passe |
|------|-------------|--------------|
| Administrateur | `admin`  | `Admin1234!` |
| Professionnel  | `pro`    | `Pro12345!`  |
| Client         | `client` | `Client12!`  |

---

## Code d'activation administrateur

Pour créer un nouveau compte **administrateur** lors de l'inscription, un code
d'activation est requis :
```
PharmAdmin2024!
```
Ce code est vérifié **côté serveur** (constante `ADMIN_CODE` dans `config/db.php`),
comparé avec `hash_equals()` pour éviter les attaques temporelles.
**À modifier en production.**

---

## Sécurité

- Mots de passe **hachés bcrypt** (coût 12) — jamais stockés en clair, jamais renvoyés au client.
- Requêtes **PDO préparées** → protection contre l'injection SQL.
- Sortie échappée côté client → protection XSS.
- **Sessions** `HttpOnly` + `SameSite=Lax` (`Secure` activé automatiquement en HTTPS).
- Contrôle d'accès **basé sur les rôles** (`requireAuth` / `requireRole`).
- Upload d'ordonnances **filtré** (extension + type MIME réel + taille max 5 Mo),
  noms de fichiers uniques, exécution de scripts désactivée dans `uploads/`.
- **Journalisation** de toutes les actions sensibles (table `logs`).

---

## Les trois vues (rôles)

| Rôle | Vue après connexion | Fonctionnalités |
|------|--------------------|-----------------|
| **Client** | Catalogue | Parcourir / rechercher, panier, commander, téléverser une ordonnance, suivre l'historique et le statut |
| **Professionnel** | Tableau de bord | Stats, CRUD médicaments, gestion des stocks, validation des commandes, validation des ordonnances |
| **Administrateur** | Tableau de bord + admin | Tout le rôle pro **+** gestion des utilisateurs (rôles, suppression) et consultation des journaux |

> Remarque : le document maître mentionne le rôle « pharmacien ». L'application
> utilise le libellé **« professionnel de santé »** (`professionnel`) de façon
> cohérente sur toute la chaîne (UI, API, base). Le périmètre fonctionnel est identique.

---

## Critères E6 couverts

| Critère | Implémentation |
|---------|----------------|
| Serveur distant | Connexion PDO MySQL/MariaDB (`config/db.php`, paramétrable par variables d'environnement) |
| Dynamique & réactif | Fetch API, mise à jour du DOM sans rechargement (SPA) |
| JavaScript client | Panier, filtres/recherche, validation de formulaire, indicateur de force du mot de passe, upload |
| Relation 1..1 | `users` ↔ `profils` (même clé primaire) **et** `commandes` ↔ `ordonnances` (FK `UNIQUE`) |
| Relation N..M | `commandes` ↔ `medicaments` via `ligne_commande` |
| Mot de passe haché | `password_hash()` bcrypt coût 12 + `password_verify()` + `password_needs_rehash()` |
| Procédures stockées | 11 procédures (`sp_inscrire_utilisateur`, `sp_creer_commande`, `sp_maj_stock`, …) |
| Triggers | `trg_stock_sortie` (décrément à la commande), `trg_stock_retour` (recrédit à l'annulation) |
| Vues SQL | `v_catalogue`, `v_commandes_detail`, `v_ordonnances`, `v_top_medicaments` |
| Upload de fichiers | Ordonnances PDF/PNG/JPG, stockées sur le serveur et liées en base |
| Journalisation | Table `logs` alimentée par `sp_log` / helper `logAction()` |

---

## API REST (résumé)

| Méthode | Endpoint | Description | Accès |
|---------|----------|-------------|-------|
| POST | `api/auth.php?action=register` | Inscription | Public |
| POST | `api/auth.php?action=login` | Connexion | Public |
| POST | `api/auth.php?action=logout` | Déconnexion | Connecté |
| GET  | `api/auth.php?action=me` | Utilisateur courant | Connecté |
| GET  | `api/medicaments.php` | Catalogue (stock > 0) | Connecté |
| GET  | `api/medicaments.php?all=1` | Catalogue complet | Pro / Admin |
| POST | `api/medicaments.php` | Ajouter | Pro / Admin |
| PUT  | `api/medicaments.php?id=X` | Modifier le stock | Pro / Admin |
| DELETE | `api/medicaments.php?id=X` | Supprimer | Pro / Admin |
| POST | `api/commandes.php` | Créer une commande | Client |
| GET  | `api/commandes.php` | Historique | Client |
| GET  | `api/commandes.php?all=1` | Toutes les commandes | Pro / Admin |
| PUT  | `api/commandes.php?id=X` | Changer le statut | Pro / Admin |
| POST | `api/ordonnances.php` | Téléverser une ordonnance | Client |
| GET  | `api/ordonnances.php?all=1` | Lister les ordonnances | Pro / Admin |
| PUT  | `api/ordonnances.php?id=X` | Valider / rejeter | Pro / Admin |
| GET  | `api/dashboard.php` | Statistiques | Pro / Admin |
| GET  | `api/users.php` | Lister les utilisateurs | Admin |
| PUT  | `api/users.php?id=X` | Changer le rôle | Admin |
| DELETE | `api/users.php?id=X` | Supprimer | Admin |
| GET  | `api/logs.php` | Journal d'activité | Admin |

Toutes les réponses sont au format JSON : `{ "success": true, "data": ... }`
ou `{ "success": false, "error": "..." }`.
