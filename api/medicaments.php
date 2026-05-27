<?php
// ─────────────────────────────────────────
//  api/medicaments.php
//  GET              — catalogue (dispo, stock>0)
//  GET  ?all=1      — tout le catalogue (pro/admin, stock 0 inclus)
//  GET  ?id=X       — détail d'un médicament
//  POST             — ajouter (pro/admin)
//  PUT  ?id=X       — modifier stock (pro/admin)
//  DELETE ?id=X     — supprimer (pro/admin)
// ─────────────────────────────────────────
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/response.php';
require_once __DIR__ . '/../includes/session.php';
require_once __DIR__ . '/../includes/helpers.php';

apiHeaders('GET, POST, PUT, DELETE, OPTIONS');
startSecureSession();

$method = $_SERVER['REQUEST_METHOD'];
$id     = isset($_GET['id']) ? (int)$_GET['id'] : null;

// ── DÉTAIL ────────────────────────────────
if ($method === 'GET' && $id) {
    requireAuth();
    $pdo  = getPDO();
    $stmt = $pdo->prepare('SELECT * FROM v_catalogue WHERE id = ?');
    $stmt->execute([$id]);
    $med  = $stmt->fetch();
    if (!$med) jsonError('Médicament introuvable', 404);
    jsonSuccess($med);
}

// ── LISTE ─────────────────────────────────
if ($method === 'GET') {
    $user = requireAuth();
    $q    = !empty($_GET['q']) ? trim($_GET['q']) : null;
    $pdo  = getPDO();

    // Vue "gestion" (pro/admin) → inclut les ruptures de stock
    if (!empty($_GET['all']) && in_array($user['role'], ['professionnel','admin'], true)) {
        $stmt = $pdo->prepare('CALL sp_liste_medicaments_admin(?)');
        $stmt->execute([$q]);
        $rows = $stmt->fetchAll();
        $stmt->closeCursor();
        jsonSuccess($rows);
    }

    // Catalogue public (clients) → stock > 0
    $cat  = !empty($_GET['cat']) ? (int)$_GET['cat'] : null;
    $stmt = $pdo->prepare('CALL sp_liste_medicaments(?, ?)');
    $stmt->execute([$cat, $q]);
    $rows = $stmt->fetchAll();
    $stmt->closeCursor();
    jsonSuccess($rows);
}

// ── AJOUT ─────────────────────────────────
if ($method === 'POST') {
    $user  = requireRole('professionnel', 'admin');
    $b     = getJsonBody();
    $nom   = trim($b['nom'] ?? '');
    $desc  = trim($b['description'] ?? '');
    $prix  = (float)($b['prix'] ?? 0);
    $stock = (int)($b['stock'] ?? 0);
    $ord   = !empty($b['ordonnance_requise']) ? 1 : 0;
    $cat   = !empty($b['categorie_id']) ? (int)$b['categorie_id'] : null;

    if ($nom === '') jsonError('Nom obligatoire');
    if ($prix  <= 0) jsonError('Prix invalide');
    if ($stock <  0) jsonError('Stock invalide');

    $pdo  = getPDO();
    $stmt = $pdo->prepare(
        'INSERT INTO medicaments (nom, description, prix, stock, ordonnance_requise, categorie_id)
         VALUES (?,?,?,?,?,?)'
    );
    $stmt->execute([$nom, $desc, $prix, $stock, $ord, $cat]);
    $newId = (int)$pdo->lastInsertId();
    logAction($pdo, (int)$user['id'], 'med_create', "Ajout médicament '$nom' (#$newId)");
    jsonSuccess(['id' => $newId], 201);
}

// ── MAJ STOCK ─────────────────────────────
if ($method === 'PUT' && $id) {
    $user      = requireRole('professionnel', 'admin');
    $b         = getJsonBody();
    $new_stock = (int)($b['stock'] ?? -1);
    $motif     = trim($b['motif'] ?? 'Mise à jour manuelle');
    if ($new_stock < 0) jsonError('Stock invalide');

    $pdo  = getPDO();
    $stmt = $pdo->prepare('CALL sp_maj_stock(?, ?, ?)');
    $stmt->execute([$id, $new_stock, $motif]);
    $stmt->closeCursor();
    logAction($pdo, (int)$user['id'], 'med_stock', "Stock médicament #$id → $new_stock ($motif)");
    jsonSuccess(['message' => 'Stock mis à jour']);
}

// ── SUPPRESSION ───────────────────────────
if ($method === 'DELETE' && $id) {
    $user = requireRole('professionnel', 'admin');
    $pdo  = getPDO();
    try {
        $stmt = $pdo->prepare('DELETE FROM medicaments WHERE id = ?');
        $stmt->execute([$id]);
        if ($stmt->rowCount() === 0) jsonError('Médicament introuvable', 404);
        logAction($pdo, (int)$user['id'], 'med_delete', "Suppression médicament #$id");
        jsonSuccess(['message' => 'Médicament supprimé']);
    } catch (PDOException $e) {
        // FK RESTRICT : médicament déjà présent dans des commandes
        jsonError('Suppression impossible : ce médicament figure dans des commandes existantes.', 409);
    }
}

jsonError('Route non trouvée', 404);
