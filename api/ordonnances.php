<?php
// ─────────────────────────────────────────
//  api/ordonnances.php
//  POST          — uploader une ordonnance (client) [multipart/form-data]
//  GET           — ordonnances du client connecté
//  GET  ?all=1   — toutes les ordonnances (pro/admin)
//  PUT  ?id=X    — valider / rejeter (pro/admin)  body: {"statut":"validee"}
// ─────────────────────────────────────────
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/response.php';
require_once __DIR__ . '/../includes/session.php';
require_once __DIR__ . '/../includes/helpers.php';

apiHeaders('GET, POST, PUT, OPTIONS');
startSecureSession();

$method = $_SERVER['REQUEST_METHOD'];
$user   = requireAuth();

// ── UPLOAD (client) ───────────────────────
if ($method === 'POST') {
    if (empty($_FILES['ordonnance'])) jsonError('Aucun fichier reçu');
    try {
        $up   = handleUploadOrdonnance($_FILES['ordonnance']);
        $pdo  = getPDO();
        $stmt = $pdo->prepare('CALL sp_ajouter_ordonnance(?, ?, ?)');
        $stmt->execute([(int)$user['id'], $up['fichier'], $up['nom_original']]);
        $row  = $stmt->fetch();
        $stmt->closeCursor();
        $newId = (int)($row['new_id'] ?? 0);
        logAction($pdo, (int)$user['id'], 'ordonnance_upload', "Ordonnance #$newId : {$up['nom_original']}");
        jsonSuccess(['id' => $newId, 'fichier' => $up['fichier']], 201);
    } catch (RuntimeException $e) {
        jsonError($e->getMessage());
    } catch (PDOException $e) {
        jsonError('Erreur serveur lors de l\'enregistrement', 500);
    }
}

// ── LISTER ────────────────────────────────
if ($method === 'GET') {
    $pdo = getPDO();
    if (!empty($_GET['all']) && in_array($user['role'], ['professionnel','admin'], true)) {
        $stmt = $pdo->query('SELECT * FROM v_ordonnances ORDER BY created_at DESC');
        jsonSuccess($stmt->fetchAll());
    }
    $stmt = $pdo->prepare('SELECT * FROM v_ordonnances WHERE user_id = ? ORDER BY created_at DESC');
    $stmt->execute([(int)$user['id']]);
    jsonSuccess($stmt->fetchAll());
}

// ── VALIDER / REJETER (pro/admin) ─────────
if ($method === 'PUT') {
    $user   = requireRole('professionnel', 'admin');
    $id     = (int)($_GET['id'] ?? 0);
    $b      = getJsonBody();
    $statut = trim($b['statut'] ?? '');
    if (!$id || !in_array($statut, ['en_attente','validee','rejetee'], true))
        jsonError('Paramètres invalides');

    $pdo  = getPDO();
    $stmt = $pdo->prepare('CALL sp_maj_ordonnance(?, ?)');
    $stmt->execute([$id, $statut]);
    $stmt->closeCursor();
    logAction($pdo, (int)$user['id'], 'ordonnance_status', "Ordonnance #$id → $statut");
    jsonSuccess(['message' => 'Ordonnance mise à jour']);
}

jsonError('Route non trouvée', 404);
