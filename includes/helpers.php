<?php
// ─────────────────────────────────────────
//  includes/helpers.php  —  Utilitaires partagés
// ─────────────────────────────────────────

/** En-têtes communs + préflight CORS. Termine la requête si OPTIONS. */
function apiHeaders(string $methods = 'GET, POST, PUT, DELETE, OPTIONS'): void {
    header('Content-Type: application/json; charset=utf-8');
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: ' . $methods);
    header('Access-Control-Allow-Headers: Content-Type');
    if (($_SERVER['REQUEST_METHOD'] ?? '') === 'OPTIONS') {
        http_response_code(204);
        exit;
    }
}

/** Journalise une action applicative (ne casse jamais la requête en cas d'échec). */
function logAction(PDO $pdo, ?int $userId, string $action, string $details = ''): void {
    try {
        $stmt = $pdo->prepare('CALL sp_log(?, ?, ?)');
        $stmt->execute([$userId, $action, $details]);
        $stmt->closeCursor();
    } catch (Throwable $e) {
        // silencieux : un échec de log ne doit jamais interrompre l'action métier
    }
}

/**
 * Gère l'upload d'un fichier d'ordonnance.
 * Retourne ['fichier' => chemin relatif, 'nom_original' => ...] ou lève une exception.
 */
function handleUploadOrdonnance(array $file): array {
    if (!isset($file['error']) || is_array($file['error'])) {
        throw new RuntimeException('Fichier invalide.');
    }
    switch ($file['error']) {
        case UPLOAD_ERR_OK:            break;
        case UPLOAD_ERR_NO_FILE:       throw new RuntimeException('Aucun fichier envoyé.');
        case UPLOAD_ERR_INI_SIZE:
        case UPLOAD_ERR_FORM_SIZE:     throw new RuntimeException('Fichier trop volumineux.');
        default:                       throw new RuntimeException('Échec de l\'upload.');
    }
    if ($file['size'] > UPLOAD_MAX) {
        throw new RuntimeException('Fichier trop volumineux (max 5 Mo).');
    }

    $orig = $file['name'] ?? 'ordonnance';
    $ext  = strtolower(pathinfo($orig, PATHINFO_EXTENSION));
    if (!in_array($ext, UPLOAD_EXT, true)) {
        throw new RuntimeException('Format non autorisé (PDF, PNG, JPG uniquement).');
    }

    // Vérification du type MIME réel
    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    $mime  = finfo_file($finfo, $file['tmp_name']);
    finfo_close($finfo);
    $allowedMimes = ['application/pdf', 'image/png', 'image/jpeg'];
    if (!in_array($mime, $allowedMimes, true)) {
        throw new RuntimeException('Type de fichier non reconnu.');
    }

    if (!is_dir(UPLOAD_DIR)) {
        mkdir(UPLOAD_DIR, 0775, true);
    }

    // Nom de fichier unique
    $unique   = 'ord_' . bin2hex(random_bytes(8)) . '_' . time() . '.' . $ext;
    $destPath = UPLOAD_DIR . $unique;

    if (!move_uploaded_file($file['tmp_name'], $destPath)) {
        throw new RuntimeException('Impossible d\'enregistrer le fichier.');
    }

    return ['fichier' => 'uploads/' . $unique, 'nom_original' => $orig];
}
