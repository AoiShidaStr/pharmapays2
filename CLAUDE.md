# PharmaPays - Gestion de pharmacie en ligne

## Projet

Application web de gestion de pharmacie (épreuve E6 BTS SIO).
SPA frontend (HTML/CSS/JS vanilla) + API REST PHP + MySQL/MariaDB.

**Dépôt GitHub** : https://github.com/AoiShidaStr/pharmapays2

## Stack

- **Frontend** : `index.html` (fichier unique SPA, JS vanilla avec Fetch API)
- **Backend** : PHP 8.2 (API REST dans `api/`)
- **Base de données** : MySQL/MariaDB via XAMPP, avec procédures stockées, triggers et vues
- **Serveur** : Apache via XAMPP
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

## Setup environnement complet (Windows)

Quand l'utilisateur demande de mettre en place / lancer / setup le projet, suivre ces étapes dans l'ordre :

### Étape 1 — Installer XAMPP (si absent)

```powershell
# Vérifier si XAMPP est déjà installé
Test-Path "C:\xampp\php\php.exe"

# Si absent, installer via winget
winget install ApacheFriends.Xampp.8.2 --accept-package-agreements --accept-source-agreements
```

Attendre la fin de l'installeur graphique (le user devra cliquer "Next" dans l'installeur XAMPP).

### Étape 2 — Cloner le dépôt dans htdocs

```powershell
# Cloner depuis GitHub directement dans le dossier XAMPP
git clone https://github.com/AoiShidaStr/pharmapays2.git C:\xampp\htdocs\pharmapays
```

Si le projet est déjà cloné ailleurs, copier :
```powershell
Copy-Item -Path "<chemin_du_repo>" -Destination "C:\xampp\htdocs\pharmapays" -Recurse -Force
```

### Étape 3 — Démarrer MySQL

```powershell
# Lancer MySQL en arrière-plan
& "C:\xampp\mysql\bin\mysqld.exe" --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone
# Attendre 3 secondes que MySQL démarre
Start-Sleep -Seconds 3
# Vérifier que MySQL répond
& "C:\xampp\mysql\bin\mysql.exe" -u root -e "SELECT 1 AS test"
```

### Étape 4 — Importer la base de données

```powershell
Get-Content "C:\xampp\htdocs\pharmapays\sql\pharmapays.sql" -Raw | & "C:\xampp\mysql\bin\mysql.exe" -u root
```

Vérifier :
```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root -e "USE pharmapays; SHOW TABLES;"
```

Résultat attendu : 8 tables (users, profils, categories, medicaments, commandes, ligne_commande, ordonnances, logs, stock_logs) + 4 vues (v_catalogue, v_commandes_detail, v_ordonnances, v_top_medicaments).

### Étape 5 — Importer avec le bon encodage (IMPORTANT)

Le fichier SQL est en UTF-8. PowerShell lit par défaut en Cp1252 (Windows). Il faut forcer UTF-8 :

```powershell
$sqlContent = [System.IO.File]::ReadAllText("C:\xampp\htdocs\pharmapays\sql\pharmapays.sql", [System.Text.Encoding]::UTF8)
$tempFile = "C:\xampp\htdocs\pharmapays\sql\pharmapays_utf8_temp.sql"
[System.IO.File]::WriteAllText($tempFile, $sqlContent, [System.Text.Encoding]::UTF8)
& "C:\xampp\mysql\bin\mysql.exe" -u root --default-character-set=utf8mb4 -e "source $tempFile"
Remove-Item $tempFile
```

Vérifier que les accents sont corrects :
```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root --default-character-set=utf8mb4 -e "SELECT nom FROM pharmapays.medicaments LIMIT 3;"
# Doit afficher : Doliprane 1000mg / Ibuprofène 400mg / Efferalgan 500mg (avec accents)
```

### Étape 6 — Démarrer Apache

```powershell
& "C:\xampp\apache\bin\httpd.exe"
# Lancer en arrière-plan (run_in_background)
```

### Étape 7 — Compiler et lancer l'application bureau Java

```powershell
Set-Location "C:\xampp\htdocs\pharmapays\desktop-java"
javac -encoding UTF-8 -cp "lib\mysql-connector-j-8.3.0.jar" -d out src\pharmapays\*.java
Start-Process java -ArgumentList "-Dfile.encoding=UTF-8", "-cp", "out;lib\mysql-connector-j-8.3.0.jar", "pharmapays.PharmaPaysApp"
```

Prérequis : JDK 17+ installé (java/javac dans le PATH).

### Étape 8 — Vérifier

- **App web** : http://localhost/pharmapays/
- **App bureau Java** : fenêtre Swing "PharmaPays — Gestion de Pharmacie"
- Les deux partagent la même base MySQL `pharmapays`

Config DB par défaut dans `config/db.php` :
- Host : `localhost`
- User : `root`
- Password : `` (vide)
- Database : `pharmapays`

Ces valeurs correspondent au XAMPP par défaut, aucune modification nécessaire.

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

| Méthode | Endpoint | Description | Accès |
|---------|----------|-------------|-------|
| POST | api/auth.php?action=register | Inscription | Public |
| POST | api/auth.php?action=login | Connexion | Public |
| POST | api/auth.php?action=logout | Déconnexion | Connecté |
| GET | api/auth.php?action=me | Utilisateur courant | Connecté |
| GET | api/medicaments.php | Catalogue (stock > 0) | Connecté |
| GET | api/medicaments.php?all=1 | Catalogue complet | Pro/Admin |
| POST | api/medicaments.php | Ajouter médicament | Pro/Admin |
| PUT | api/medicaments.php?id=X | Modifier stock | Pro/Admin |
| DELETE | api/medicaments.php?id=X | Supprimer | Pro/Admin |
| POST | api/commandes.php | Créer commande | Client |
| GET | api/commandes.php | Historique client | Client |
| GET | api/commandes.php?all=1 | Toutes les commandes | Pro/Admin |
| PUT | api/commandes.php?id=X | Changer statut | Pro/Admin |
| POST | api/ordonnances.php | Upload ordonnance | Client |
| GET | api/ordonnances.php?all=1 | Lister ordonnances | Pro/Admin |
| PUT | api/ordonnances.php?id=X | Valider/rejeter | Pro/Admin |
| GET | api/dashboard.php | Statistiques | Pro/Admin |
| GET | api/users.php | Lister utilisateurs | Admin |
| PUT | api/users.php?id=X | Changer rôle | Admin |
| DELETE | api/users.php?id=X | Supprimer user | Admin |
| GET | api/logs.php | Journal activité | Admin |

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

## Application bureau Java (desktop-java/)

Architecture du code Java :

```
desktop-java/
├── lib/
│   └── mysql-connector-j-8.3.0.jar   ← Driver JDBC MySQL
├── src/pharmapays/
│   ├── DatabaseConnection.java        ← Singleton connexion JDBC (pattern Singleton)
│   ├── Medicament.java                ← Classe métier (POJO)
│   ├── MedicamentDAO.java             ← Couche d'accès aux données (pattern DAO)
│   ├── MedicamentDialog.java          ← Formulaire modal ajout/modification
│   └── PharmaPaysApp.java             ← Interface graphique Swing (point d'entrée)
└── out/                               ← Classes compilées (gitignored)
```

Fonctionnalités :
- Affichage du catalogue en tableau (JTable)
- Recherche en temps réel par nom/description
- CRUD complet (ajout, modification, suppression)
- Modification du stock via procédure stockée sp_maj_stock (CallableStatement)
- Menu contextuel clic droit
- Connexion JDBC avec PreparedStatement (sécurité SQL)

Patterns utilisés : Singleton (connexion), DAO (accès données), MVC (séparation UI/données)

## Après modification du code

1. **App web** : tester sur http://localhost/pharmapays/ — pas de restart Apache nécessaire
2. **App Java** : recompiler avec `javac -encoding UTF-8 -cp "lib\mysql-connector-j-8.3.0.jar" -d out src\pharmapays\*.java`
3. Les fichiers modifiés dans le repo sont automatiquement servis par Apache (le dossier htdocs EST le repo)
4. Pour les changements SQL, ré-importer avec encodage UTF-8 (voir Étape 5)
5. Commit et push vers https://github.com/AoiShidaStr/pharmapays2
