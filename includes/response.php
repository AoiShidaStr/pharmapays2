<?php
// ─────────────────────────────────────────
//  includes/response.php  —  Helpers JSON
// ─────────────────────────────────────────
function jsonSuccess(mixed $data = null, int $code = 200): void {
    http_response_code($code);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['success' => true, 'data' => $data], JSON_UNESCAPED_UNICODE);
    exit;
}

function jsonError(string $msg, int $code = 400): void {
    http_response_code($code);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['success' => false, 'error' => $msg], JSON_UNESCAPED_UNICODE);
    exit;
}

function getJsonBody(): array {
    $raw  = file_get_contents('php://input');
    $data = json_decode($raw, true);
    return is_array($data) ? $data : [];
}
