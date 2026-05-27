<?php
// ─────────────────────────────────────────
//  config/db.php  —  Connexion PDO MySQL + config
// ─────────────────────────────────────────

// --- Paramètres base de données (serveur distant) ---
// Astuce déploiement : ces valeurs peuvent être surchargées par des
// variables d'environnement (DB_HOST, DB_USER, DB_PASSWORD, DB_NAME).
define('DB_HOST',    getenv('DB_HOST')     ?: 'localhost');
define('DB_NAME',    getenv('DB_NAME')     ?: 'pharmapays');
define('DB_USER',    getenv('DB_USER')     ?: 'root');
define('DB_PASS',    getenv('DB_PASSWORD') ?: '');
define('DB_CHARSET', 'utf8mb4');

// --- Code d'activation administrateur (vérifié CÔTÉ SERVEUR) ---
// À changer en production / via variable d'environnement.
define('ADMIN_CODE', getenv('ADMIN_CODE') ?: 'PharmAdmin2024!');

// --- Dossier d'upload des ordonnances ---
define('UPLOAD_DIR', __DIR__ . '/../uploads/');
define('UPLOAD_MAX', 5 * 1024 * 1024); // 5 Mo
define('UPLOAD_EXT', ['pdf', 'png', 'jpg', 'jpeg']);

function getPDO(): PDO {
    static $pdo = null;
    if ($pdo === null) {
        $dsn = sprintf('mysql:host=%s;dbname=%s;charset=%s', DB_HOST, DB_NAME, DB_CHARSET);
        $pdo = new PDO($dsn, DB_USER, DB_PASS, [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES   => false,
        ]);
    }
    return $pdo;
}
