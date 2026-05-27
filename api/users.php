<?php
// ─────────────────────────────────────────
//  api/users.php   (réservé ADMIN)
//  GET           — liste de tous les utilisateurs
//  PUT  ?id=X    — modifier le rôle      body: {"role":"professionnel"}
//  DELETE ?id=X  — supprimer un utilisateur
// ─────────────────────────────────────────
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/response.php';
require_once __DIR__ . '/../includes/session.php';
require_once __DIR__ . '/../includes/helpers.php';

apiHeaders('GET, PUT, DELETE, OPTIONS');
startSecureSession();

$method = $_SERVER['REQUEST_METHOD'];
$admin  = requireRole('admin');
$id     = isset($_GET['id']) ? (int)$_GET['id'] : null;

// ── LISTE ─────────────────────────────────
if ($method === 'GET') {
    $pdo  = getPDO();
    $stmt = $pdo->query(
        'SELECT u.id, u.username, u.role, u.created_at,
                p.nom, p.prenom, p.telephone, p.adresse
         FROM users u
         LEFT JOIN profils p ON u.id = p.user_id
         ORDER BY u.created_at DESC'
    );
    jsonSuccess($stmt->fetchAll());
}

// ── MODIFIER LE RÔLE ──────────────────────
if ($method === 'PUT' && $id) {
    $b    = getJsonBody();
    $role = trim($b['role'] ?? '');
    if (!in_array($role, ['client','professionnel','admin'], true)) jsonError('Rôle invalide');
    if ($id === (int)$admin['id']) jsonError('Vous ne pouvez pas modifier votre propre rôle', 409);

    $pdo  = getPDO();
    $stmt = $pdo->prepare('UPDATE users SET role = ? WHERE id = ?');
    $stmt->execute([$role, $id]);
    if ($stmt->rowCount() === 0) jsonError('Utilisateur introuvable', 404);

    logAction($pdo, (int)$admin['id'], 'user_role', "Utilisateur #$id → rôle $role");
    jsonSuccess(['message' => 'Rôle mis à jour']);
}

// ── SUPPRIMER ─────────────────────────────
if ($method === 'DELETE' && $id) {
    if ($id === (int)$admin['id']) jsonError('Vous ne pouvez pas supprimer votre propre compte', 409);

    $pdo  = getPDO();
    try {
        $stmt = $pdo->prepare('DELETE FROM users WHERE id = ?');
        $stmt->execute([$id]);
        if ($stmt->rowCount() === 0) jsonError('Utilisateur introuvable', 404);
        logAction($pdo, (int)$admin['id'], 'user_delete', "Suppression utilisateur #$id");
        jsonSuccess(['message' => 'Utilisateur supprimé']);
    } catch (PDOException $e) {
        jsonError('Suppression impossible (données liées).', 409);
    }
}

jsonError('Route non trouvée', 404);
