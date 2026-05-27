<?php
// ─────────────────────────────────────────
//  api/auth.php
//  POST ?action=register  — inscription
//  POST ?action=login     — connexion
//  POST ?action=logout    — déconnexion
//  GET  ?action=me        — utilisateur en session
// ─────────────────────────────────────────
require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../includes/response.php';
require_once __DIR__ . '/../includes/session.php';
require_once __DIR__ . '/../includes/helpers.php';

apiHeaders('GET, POST, OPTIONS');
startSecureSession();

$action = $_GET['action'] ?? '';

match ($action) {
    'register' => handleRegister(),
    'login'    => handleLogin(),
    'logout'   => handleLogout(),
    'me'       => handleMe(),
    default    => jsonError('Action inconnue', 404),
};

// ── REGISTER ──────────────────────────────
function handleRegister(): void {
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') jsonError('Méthode non autorisée', 405);

    $b         = getJsonBody();
    $username  = trim($b['username']   ?? '');
    $password  = (string)($b['password'] ?? '');
    $role      = trim($b['role']       ?? 'client');
    $nom       = trim($b['nom']        ?? '');
    $prenom    = trim($b['prenom']     ?? '');
    $telephone = trim($b['telephone']  ?? '');
    $adresse   = trim($b['adresse']    ?? '');
    $adminCode = (string)($b['admin_code'] ?? '');

    // --- Validations serveur ---
    if (strlen($username) < 3)               jsonError('Identifiant trop court (min 3 caractères)');
    if (strlen($password) < 8)               jsonError('Mot de passe trop court (min 8 caractères)');
    if (!preg_match('/[A-Z]/', $password))   jsonError('Le mot de passe doit contenir au moins une majuscule');
    if (!preg_match('/[0-9]/', $password))   jsonError('Le mot de passe doit contenir au moins un chiffre');
    if ($nom === '' || $prenom === '')       jsonError('Nom et prénom obligatoires');
    if (!in_array($role, ['client','professionnel','admin'], true)) jsonError('Rôle invalide');

    // --- Vérification du code admin CÔTÉ SERVEUR (autorité unique) ---
    if ($role === 'admin' && !hash_equals(ADMIN_CODE, $adminCode)) {
        jsonError('Code d\'activation administrateur incorrect', 403);
    }

    // Hash bcrypt — mot de passe JAMAIS stocké en clair ✓
    $hash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);

    try {
        $pdo  = getPDO();
        $stmt = $pdo->prepare('CALL sp_inscrire_utilisateur(?,?,?,?,?,?,?)');
        $stmt->execute([$username, $hash, $role, $nom, $prenom, $telephone, $adresse]);
        $row = $stmt->fetch();
        $stmt->closeCursor();

        $newId = (int)($row['new_user_id'] ?? 0);
        logAction($pdo, $newId, 'register', "Nouvel utilisateur '$username' (rôle: $role)");
        jsonSuccess(['user_id' => $newId], 201);
    } catch (PDOException $e) {
        if (str_contains($e->getMessage(), 'Duplicate entry')) jsonError('Cet identifiant est déjà pris', 409);
        jsonError('Erreur serveur lors de l\'inscription', 500);
    }
}

// ── LOGIN ─────────────────────────────────
function handleLogin(): void {
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') jsonError('Méthode non autorisée', 405);

    $b        = getJsonBody();
    $username = trim($b['username'] ?? '');
    $password = (string)($b['password'] ?? '');
    if ($username === '' || $password === '') jsonError('Identifiants manquants');

    try {
        $pdo  = getPDO();
        $stmt = $pdo->prepare('CALL sp_get_user_by_username(?)');
        $stmt->execute([$username]);
        $user = $stmt->fetch();
        $stmt->closeCursor(); // libère le curseur de la procédure stockée

        if (!$user || !password_verify($password, $user['password_hash'])) {
            jsonError('Identifiants incorrects', 401);
        }

        // Re-hash automatique si le coût a changé
        if (password_needs_rehash($user['password_hash'], PASSWORD_BCRYPT, ['cost' => 12])) {
            $newHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
            $up = $pdo->prepare('UPDATE users SET password_hash=? WHERE id=?');
            $up->execute([$newHash, $user['id']]);
        }

        unset($user['password_hash']);   // NE JAMAIS renvoyer le hash
        $user['id'] = (int)$user['id'];
        $_SESSION['user'] = $user;

        logAction($pdo, $user['id'], 'login', "Connexion de '$username'");
        jsonSuccess($user);
    } catch (PDOException $e) {
        jsonError('Erreur serveur', 500);
    }
}

// ── LOGOUT ────────────────────────────────
function handleLogout(): void {
    if (!empty($_SESSION['user'])) {
        try { logAction(getPDO(), (int)$_SESSION['user']['id'], 'logout', ''); } catch (Throwable $e) {}
    }
    $_SESSION = [];
    if (ini_get('session.use_cookies')) {
        $p = session_get_cookie_params();
        setcookie(session_name(), '', time() - 42000, $p['path'], $p['domain'], $p['secure'], $p['httponly']);
    }
    session_destroy();
    jsonSuccess(['message' => 'Déconnecté']);
}

// ── ME ────────────────────────────────────
function handleMe(): void {
    if (empty($_SESSION['user'])) jsonError('Non authentifié', 401);
    jsonSuccess($_SESSION['user']);
}
