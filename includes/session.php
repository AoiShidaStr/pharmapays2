<?php
// ─────────────────────────────────────────
//  includes/session.php  —  Session sécurisée + rôles
// ─────────────────────────────────────────
function startSecureSession(): void {
    if (session_status() === PHP_SESSION_NONE) {
        // Le flag "secure" n'est activé que si l'on est réellement en HTTPS,
        // sinon les sessions ne fonctionneraient pas en développement
        // local (http://localhost) — c'était un bug dans la version initiale.
        $isHttps = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off')
                 || (($_SERVER['SERVER_PORT'] ?? null) == 443)
                 || (($_SERVER['HTTP_X_FORWARDED_PROTO'] ?? '') === 'https');

        session_set_cookie_params([
            'lifetime' => 0,
            'path'     => '/',
            'secure'   => $isHttps,
            'httponly' => true,
            'samesite' => 'Lax',     // 'Lax' tolère mieux le dev local que 'Strict'
        ]);
        session_start();
    }
}

function requireAuth(): array {
    startSecureSession();
    if (empty($_SESSION['user'])) {
        jsonError('Non authentifié', 401);
    }
    return $_SESSION['user'];
}

/**
 * Accepte un ou plusieurs rôles autorisés.
 * Ex : requireRole('admin', 'professionnel')
 */
function requireRole(string ...$roles): array {
    $user = requireAuth();
    if (!in_array($user['role'], $roles, true)) {
        jsonError('Accès refusé', 403);
    }
    return $user;
}
