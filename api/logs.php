<?php
// ─────────────────────────────────────────
//  api/logs.php   (réservé ADMIN)
//  GET  ?limit=200   — journal d'activité système
// ─────────────────────────────────────────
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/response.php';
require_once __DIR__ . '/../includes/session.php';
require_once __DIR__ . '/../includes/helpers.php';

apiHeaders('GET, OPTIONS');
startSecureSession();
requireRole('admin');

$limit = isset($_GET['limit']) ? max(1, min(500, (int)$_GET['limit'])) : 200;

$pdo  = getPDO();
$stmt = $pdo->prepare(
    'SELECT l.id, l.action, l.details, l.created_at, u.username
     FROM logs l
     LEFT JOIN users u ON l.user_id = u.id
     ORDER BY l.id DESC
     LIMIT ?'
);
$stmt->bindValue(1, $limit, PDO::PARAM_INT);
$stmt->execute();
jsonSuccess($stmt->fetchAll());
