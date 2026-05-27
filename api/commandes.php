<?php
// ─────────────────────────────────────────
//  api/commandes.php
//  POST          — créer une commande
//  GET           — historique du client connecté
//  GET  ?all=1   — toutes les commandes (pro/admin)
//  PUT  ?id=X    — changer le statut (pro/admin)
// ─────────────────────────────────────────
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/response.php';
require_once __DIR__ . '/../includes/session.php';
require_once __DIR__ . '/../includes/helpers.php';

apiHeaders('GET, POST, PUT, OPTIONS');
startSecureSession();

$method = $_SERVER['REQUEST_METHOD'];
$user   = requireAuth();

// ── CRÉER UNE COMMANDE ────────────────────
if ($method === 'POST') {
    $b      = getJsonBody();
    $lignes = $b['lignes'] ?? [];
    if (empty($lignes) || !is_array($lignes)) jsonError('Panier vide');

    foreach ($lignes as $i => $l) {
        if (empty($l['med_id']) || empty($l['qte']) || (int)$l['qte'] <= 0)
            jsonError('Ligne ' . ($i + 1) . ' invalide');
    }

    try {
        $pdo  = getPDO();
        $stmt = $pdo->prepare('CALL sp_creer_commande(?, ?, @cid, @err)');
        $stmt->execute([(int)$user['id'], json_encode(array_values($lignes))]);
        $stmt->closeCursor(); // ESSENTIEL avant la lecture des variables OUT

        $row = $pdo->query('SELECT @cid AS cid, @err AS err')->fetch();
        if (!empty($row['err'])) jsonError($row['err']);

        $cid = (int)$row['cid'];
        logAction($pdo, (int)$user['id'], 'order_create', "Commande #$cid créée");
        jsonSuccess(['commande_id' => $cid], 201);
    } catch (PDOException $e) {
        jsonError('Erreur lors de la création de la commande', 500);
    }
}

// ── LISTER ────────────────────────────────
if ($method === 'GET') {
    $pdo = getPDO();

    // Vue globale (pro/admin)
    if (!empty($_GET['all']) && in_array($user['role'], ['professionnel','admin'], true)) {
        $stmt = $pdo->query('SELECT * FROM v_commandes_detail ORDER BY created_at DESC');
        jsonSuccess($stmt->fetchAll());
    }

    // Historique du client connecté
    $stmt = $pdo->prepare('CALL sp_historique_client(?)');
    $stmt->execute([(int)$user['id']]);
    $rows = $stmt->fetchAll();
    $stmt->closeCursor();
    jsonSuccess($rows);
}

// ── CHANGER LE STATUT ─────────────────────
if ($method === 'PUT') {
    $user   = requireRole('professionnel', 'admin');
    $id     = (int)($_GET['id'] ?? 0);
    $b      = getJsonBody();
    $statut = trim($b['statut'] ?? '');
    if (!$id || !in_array($statut, ['en_attente','validee','livree','annulee'], true))
        jsonError('Paramètres invalides');

    $pdo  = getPDO();
    $stmt = $pdo->prepare('UPDATE commandes SET statut=? WHERE id=?');
    $stmt->execute([$statut, $id]);
    if ($stmt->rowCount() === 0) jsonError('Commande introuvable', 404);

    logAction($pdo, (int)$user['id'], 'order_status', "Commande #$id → $statut");
    jsonSuccess(['message' => 'Statut mis à jour']);
}

jsonError('Route non trouvée', 404);
