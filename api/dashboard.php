<?php
// ─────────────────────────────────────────
//  api/dashboard.php  —  Stats pro/admin
// ─────────────────────────────────────────
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/response.php';
require_once __DIR__ . '/../includes/session.php';
require_once __DIR__ . '/../includes/helpers.php';

apiHeaders('GET, OPTIONS');
startSecureSession();
requireRole('professionnel', 'admin');

$pdo  = getPDO();
$stmt = $pdo->query('CALL sp_dashboard_pro()');
$data = $stmt->fetch();
$stmt->closeCursor();
jsonSuccess($data);
