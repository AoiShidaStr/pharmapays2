-- ============================================================
--  PharmaPays — Base de données MySQL  (version complète E6)
--  Critères E6 couverts :
--    ✓ Serveur distant (MySQL)
--    ✓ Relation 1..1  : users ↔ profils   ET   commandes ↔ ordonnances
--    ✓ Relation N..M  : commandes ↔ medicaments (via ligne_commande)
--    ✓ Mot de passe non stocké en clair (bcrypt côté PHP)
--    ✓ Procédures stockées (11)
--    ✓ Triggers (2) + Vues (4)
--    ✓ Trois rôles : client / professionnel / admin
--    ✓ Upload d'ordonnances + journal (logs)
-- ============================================================

CREATE DATABASE IF NOT EXISTS pharmapays CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pharmapays;

-- Suppression dans l'ordre des dépendances (réexécution propre)
DROP VIEW  IF EXISTS v_top_medicaments;
DROP VIEW  IF EXISTS v_commandes_detail;
DROP VIEW  IF EXISTS v_ordonnances;
DROP VIEW  IF EXISTS v_catalogue;
DROP TABLE IF EXISTS logs;
DROP TABLE IF EXISTS stock_logs;
DROP TABLE IF EXISTS ordonnances;
DROP TABLE IF EXISTS ligne_commande;
DROP TABLE IF EXISTS commandes;
DROP TABLE IF EXISTS medicaments;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS profils;
DROP TABLE IF EXISTS users;

-- ============================================================
-- TABLES
-- ============================================================

-- Table principale des utilisateurs (3 rôles)
CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(60)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,                       -- bcrypt, JAMAIS en clair
    role          ENUM('client','professionnel','admin') NOT NULL DEFAULT 'client',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_role (role)
);

-- Relation 1..1 : un user possède exactement un profil (même PK)
CREATE TABLE profils (
    user_id    INT          PRIMARY KEY,                       -- même PK que users → 1..1 strict
    nom        VARCHAR(100) NOT NULL,
    prenom     VARCHAR(100) NOT NULL,
    telephone  VARCHAR(30),
    adresse    TEXT,
    CONSTRAINT fk_profil_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Catégories de médicaments
CREATE TABLE categories (
    id  INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE
);

-- Médicaments
CREATE TABLE medicaments (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nom                 VARCHAR(150) NOT NULL,
    description         TEXT,
    prix                DECIMAL(8,2) NOT NULL CHECK (prix >= 0),
    stock               INT          NOT NULL DEFAULT 0 CHECK (stock >= 0),
    ordonnance_requise  TINYINT(1)   NOT NULL DEFAULT 0,       -- médicament soumis à ordonnance
    categorie_id        INT,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_med_categorie FOREIGN KEY (categorie_id) REFERENCES categories(id)
        ON DELETE SET NULL,
    INDEX idx_med_nom (nom),
    INDEX idx_med_cat (categorie_id)
);

-- Commandes (entête)
CREATE TABLE commandes (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT  NOT NULL,
    statut     ENUM('en_attente','validee','livree','annulee') NOT NULL DEFAULT 'en_attente',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_commande_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    INDEX idx_cmd_user (user_id),
    INDEX idx_cmd_statut (statut)
);

-- Table pivot N..M : une commande contient plusieurs médicaments,
-- un médicament peut apparaître dans plusieurs commandes
CREATE TABLE ligne_commande (
    commande_id   INT            NOT NULL,
    medicament_id INT            NOT NULL,
    quantite      INT            NOT NULL CHECK (quantite > 0),
    prix_unitaire DECIMAL(8,2)   NOT NULL,                     -- snapshot du prix au moment de la commande
    PRIMARY KEY (commande_id, medicament_id),
    CONSTRAINT fk_lc_commande   FOREIGN KEY (commande_id)   REFERENCES commandes(id)   ON DELETE CASCADE,
    CONSTRAINT fk_lc_medicament FOREIGN KEY (medicament_id) REFERENCES medicaments(id) ON DELETE RESTRICT
);

-- Ordonnances (upload de fichier)  +  Relation 1..1 commande ↔ ordonnance
CREATE TABLE ordonnances (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT          NOT NULL,
    commande_id   INT          UNIQUE,                          -- UNIQUE → 1..1 avec une commande
    fichier       VARCHAR(255) NOT NULL,                        -- chemin du fichier stocké
    nom_original  VARCHAR(255) NOT NULL,
    statut        ENUM('en_attente','validee','rejetee') NOT NULL DEFAULT 'en_attente',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ord_user     FOREIGN KEY (user_id)     REFERENCES users(id)     ON DELETE CASCADE,
    CONSTRAINT fk_ord_commande FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE SET NULL,
    INDEX idx_ord_user (user_id),
    INDEX idx_ord_statut (statut)
);

-- Journal de mouvements de stock
CREATE TABLE stock_logs (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    medicament_id INT  NOT NULL,
    variation     INT  NOT NULL,                                -- positif = entrée, négatif = sortie
    motif         VARCHAR(255),
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_med FOREIGN KEY (medicament_id) REFERENCES medicaments(id) ON DELETE CASCADE
);

-- Journal d'activité système (logs applicatifs)
CREATE TABLE logs (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT,                                             -- NULL si action anonyme
    action     VARCHAR(80)  NOT NULL,
    details    TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_logs_date (created_at)
);

-- ============================================================
-- VUES
-- ============================================================

-- Vue : catalogue complet avec nom + id de catégorie
CREATE OR REPLACE VIEW v_catalogue AS
    SELECT
        m.id,
        m.nom,
        m.description,
        m.prix,
        m.stock,
        m.ordonnance_requise,
        m.categorie_id,
        c.nom AS categorie,
        m.created_at
    FROM medicaments m
    LEFT JOIN categories c ON m.categorie_id = c.id;

-- Vue : détail des commandes avec infos client + total
CREATE OR REPLACE VIEW v_commandes_detail AS
    SELECT
        co.id            AS commande_id,
        co.statut,
        co.created_at,
        co.user_id,
        u.username,
        p.nom            AS client_nom,
        p.prenom         AS client_prenom,
        SUM(lc.quantite * lc.prix_unitaire) AS total,
        COUNT(lc.medicament_id)             AS nb_lignes
    FROM commandes co
    JOIN users          u  ON co.user_id      = u.id
    LEFT JOIN profils   p  ON u.id            = p.user_id
    JOIN ligne_commande lc ON co.id           = lc.commande_id
    GROUP BY co.id, co.statut, co.created_at, co.user_id, u.username, p.nom, p.prenom;

-- Vue : ordonnances avec infos client
CREATE OR REPLACE VIEW v_ordonnances AS
    SELECT
        o.id,
        o.user_id,
        o.commande_id,
        o.fichier,
        o.nom_original,
        o.statut,
        o.created_at,
        u.username,
        p.nom    AS client_nom,
        p.prenom AS client_prenom
    FROM ordonnances o
    JOIN users        u ON o.user_id = u.id
    LEFT JOIN profils p ON u.id      = p.user_id;

-- Vue bonus : top médicaments vendus
CREATE OR REPLACE VIEW v_top_medicaments AS
    SELECT
        m.id,
        m.nom,
        COALESCE(SUM(lc.quantite), 0) AS total_vendu
    FROM medicaments m
    LEFT JOIN ligne_commande lc ON m.id = lc.medicament_id
    GROUP BY m.id, m.nom
    ORDER BY total_vendu DESC;

-- ============================================================
-- TRIGGERS
-- ============================================================

DELIMITER $$

-- Trigger : décrémente le stock quand une ligne_commande est insérée
CREATE TRIGGER trg_stock_sortie
AFTER INSERT ON ligne_commande
FOR EACH ROW
BEGIN
    UPDATE medicaments
    SET stock = stock - NEW.quantite
    WHERE id = NEW.medicament_id;

    INSERT INTO stock_logs (medicament_id, variation, motif)
    VALUES (NEW.medicament_id, -NEW.quantite, CONCAT('Commande #', NEW.commande_id));
END$$

-- Trigger : re-crédite le stock si une commande est annulée
CREATE TRIGGER trg_stock_retour
AFTER UPDATE ON commandes
FOR EACH ROW
BEGIN
    IF NEW.statut = 'annulee' AND OLD.statut != 'annulee' THEN
        UPDATE medicaments m
        JOIN ligne_commande lc ON lc.medicament_id = m.id
        SET m.stock = m.stock + lc.quantite
        WHERE lc.commande_id = NEW.id;

        INSERT INTO stock_logs (medicament_id, variation, motif)
        SELECT lc.medicament_id, lc.quantite, CONCAT('Annulation commande #', NEW.id)
        FROM ligne_commande lc
        WHERE lc.commande_id = NEW.id;
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- PROCÉDURES STOCKÉES
-- ============================================================

DELIMITER $$

-- SP 1 : Inscription d'un utilisateur avec son profil (transaction atomique)
CREATE PROCEDURE sp_inscrire_utilisateur(
    IN p_username      VARCHAR(60),
    IN p_password_hash VARCHAR(255),
    IN p_role          VARCHAR(20),
    IN p_nom           VARCHAR(100),
    IN p_prenom        VARCHAR(100),
    IN p_telephone     VARCHAR(30),
    IN p_adresse       TEXT
)
BEGIN
    DECLARE v_user_id INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
        INSERT INTO users (username, password_hash, role)
        VALUES (p_username, p_password_hash, p_role);

        SET v_user_id = LAST_INSERT_ID();

        INSERT INTO profils (user_id, nom, prenom, telephone, adresse)
        VALUES (v_user_id, p_nom, p_prenom, p_telephone, p_adresse);
    COMMIT;

    SELECT v_user_id AS new_user_id;
END$$


-- SP 2 : Récupérer un utilisateur par son username (login)
CREATE PROCEDURE sp_get_user_by_username(
    IN p_username VARCHAR(60)
)
BEGIN
    SELECT u.id, u.username, u.password_hash, u.role,
           p.nom, p.prenom, p.telephone, p.adresse
    FROM users u
    LEFT JOIN profils p ON u.id = p.user_id
    WHERE u.username = p_username
    LIMIT 1;
END$$


-- SP 3 : Créer une commande complète (entête + lignes) en transaction
-- p_lignes = JSON : [{"med_id":1,"qte":2},{"med_id":3,"qte":1}]
CREATE PROCEDURE sp_creer_commande(
    IN  p_user_id     INT,
    IN  p_lignes      JSON,
    OUT p_commande_id INT,
    OUT p_erreur      VARCHAR(255)
)
sp_creer_commande:BEGIN
    DECLARE v_idx   INT DEFAULT 0;
    DECLARE v_nb    INT;
    DECLARE v_med_id INT;
    DECLARE v_qte    INT;
    DECLARE v_prix   DECIMAL(8,2);
    DECLARE v_stock  INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_commande_id = 0;
        SET p_erreur = 'Erreur SQL lors de la création de la commande.';
    END;

    SET p_erreur = '';
    SET p_commande_id = 0;
    SET v_nb = JSON_LENGTH(p_lignes);

    IF v_nb IS NULL OR v_nb = 0 THEN
        SET p_erreur = 'Panier vide.';
        LEAVE sp_creer_commande;
    END IF;

    -- Vérification des stocks AVANT toute insertion
    WHILE v_idx < v_nb DO
        SET v_med_id = JSON_UNQUOTE(JSON_EXTRACT(p_lignes, CONCAT('$[', v_idx, '].med_id')));
        SET v_qte    = JSON_UNQUOTE(JSON_EXTRACT(p_lignes, CONCAT('$[', v_idx, '].qte')));

        SELECT stock INTO v_stock FROM medicaments WHERE id = v_med_id;

        IF v_stock IS NULL THEN
            SET p_erreur = CONCAT('Médicament introuvable : id=', v_med_id);
            LEAVE sp_creer_commande;
        END IF;
        IF v_stock < v_qte THEN
            SET p_erreur = CONCAT('Stock insuffisant (médicament id=', v_med_id,
                                  ', dispo: ', v_stock, ', demandé: ', v_qte, ')');
            LEAVE sp_creer_commande;
        END IF;
        SET v_idx = v_idx + 1;
    END WHILE;

    START TRANSACTION;
        INSERT INTO commandes (user_id, statut) VALUES (p_user_id, 'en_attente');
        SET p_commande_id = LAST_INSERT_ID();

        SET v_idx = 0;
        WHILE v_idx < v_nb DO
            SET v_med_id = JSON_UNQUOTE(JSON_EXTRACT(p_lignes, CONCAT('$[', v_idx, '].med_id')));
            SET v_qte    = JSON_UNQUOTE(JSON_EXTRACT(p_lignes, CONCAT('$[', v_idx, '].qte')));

            SELECT prix INTO v_prix FROM medicaments WHERE id = v_med_id;

            INSERT INTO ligne_commande (commande_id, medicament_id, quantite, prix_unitaire)
            VALUES (p_commande_id, v_med_id, v_qte, v_prix);
            -- trg_stock_sortie met à jour le stock automatiquement

            SET v_idx = v_idx + 1;
        END WHILE;
    COMMIT;
END$$


-- SP 4 : Lister les médicaments disponibles (stock > 0) avec filtres optionnels
CREATE PROCEDURE sp_liste_medicaments(
    IN p_categorie_id INT,
    IN p_recherche    VARCHAR(100)
)
BEGIN
    SELECT m.id, m.nom, m.description, m.prix, m.stock,
           m.ordonnance_requise, m.categorie_id, c.nom AS categorie
    FROM medicaments m
    LEFT JOIN categories c ON m.categorie_id = c.id
    WHERE m.stock > 0
      AND (p_categorie_id IS NULL OR m.categorie_id = p_categorie_id)
      AND (p_recherche IS NULL OR p_recherche = '' OR m.nom LIKE CONCAT('%', p_recherche, '%'))
    ORDER BY m.nom;
END$$


-- SP 5 : Lister TOUS les médicaments (gestion pro/admin, stock 0 inclus)
CREATE PROCEDURE sp_liste_medicaments_admin(
    IN p_recherche VARCHAR(100)
)
BEGIN
    SELECT m.id, m.nom, m.description, m.prix, m.stock,
           m.ordonnance_requise, m.categorie_id, c.nom AS categorie
    FROM medicaments m
    LEFT JOIN categories c ON m.categorie_id = c.id
    WHERE (p_recherche IS NULL OR p_recherche = '' OR m.nom LIKE CONCAT('%', p_recherche, '%'))
    ORDER BY m.nom;
END$$


-- SP 6 : Historique des commandes d'un client
CREATE PROCEDURE sp_historique_client(
    IN p_user_id INT
)
BEGIN
    SELECT
        co.id            AS commande_id,
        co.statut,
        co.created_at,
        m.nom            AS medicament,
        lc.quantite,
        lc.prix_unitaire,
        (lc.quantite * lc.prix_unitaire) AS sous_total
    FROM commandes co
    JOIN ligne_commande lc ON co.id            = lc.commande_id
    JOIN medicaments    m  ON lc.medicament_id = m.id
    WHERE co.user_id = p_user_id
    ORDER BY co.created_at DESC, co.id;
END$$


-- SP 7 : Tableau de bord professionnel — stats globales
CREATE PROCEDURE sp_dashboard_pro()
BEGIN
    SELECT
        (SELECT COUNT(*) FROM users WHERE role = 'client')              AS nb_clients,
        (SELECT COUNT(*) FROM medicaments WHERE stock > 0)              AS nb_meds_dispo,
        (SELECT COUNT(*) FROM medicaments WHERE stock = 0)              AS nb_meds_rupture,
        (SELECT COUNT(*) FROM commandes WHERE statut = 'en_attente')    AS nb_commandes_attente,
        (SELECT COUNT(*) FROM ordonnances WHERE statut = 'en_attente')  AS nb_ordonnances_attente,
        (SELECT COALESCE(SUM(lc.quantite * lc.prix_unitaire), 0)
         FROM commandes co JOIN ligne_commande lc ON co.id = lc.commande_id
         WHERE co.statut IN ('validee','livree'))                       AS chiffre_affaires;
END$$


-- SP 8 : Mettre à jour le stock manuellement
CREATE PROCEDURE sp_maj_stock(
    IN p_medicament_id INT,
    IN p_nouveau_stock INT,
    IN p_motif         VARCHAR(255)
)
BEGIN
    DECLARE v_ancien_stock INT;

    SELECT stock INTO v_ancien_stock FROM medicaments WHERE id = p_medicament_id;
    IF v_ancien_stock IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Médicament introuvable';
    END IF;

    UPDATE medicaments SET stock = p_nouveau_stock WHERE id = p_medicament_id;

    INSERT INTO stock_logs (medicament_id, variation, motif)
    VALUES (p_medicament_id, (p_nouveau_stock - v_ancien_stock), p_motif);
END$$


-- SP 9 : Enregistrer une ordonnance uploadée
CREATE PROCEDURE sp_ajouter_ordonnance(
    IN p_user_id      INT,
    IN p_fichier      VARCHAR(255),
    IN p_nom_original VARCHAR(255)
)
BEGIN
    INSERT INTO ordonnances (user_id, fichier, nom_original, statut)
    VALUES (p_user_id, p_fichier, p_nom_original, 'en_attente');
    SELECT LAST_INSERT_ID() AS new_id;
END$$


-- SP 10 : Valider / rejeter une ordonnance
CREATE PROCEDURE sp_maj_ordonnance(
    IN p_id     INT,
    IN p_statut VARCHAR(20)
)
BEGIN
    UPDATE ordonnances SET statut = p_statut WHERE id = p_id;
END$$


-- SP 11 : Enregistrer une entrée de log applicatif
CREATE PROCEDURE sp_log(
    IN p_user_id INT,
    IN p_action  VARCHAR(80),
    IN p_details TEXT
)
BEGIN
    INSERT INTO logs (user_id, action, details) VALUES (p_user_id, p_action, p_details);
END$$

DELIMITER ;

-- ============================================================
-- DONNÉES DE DÉMONSTRATION
-- ============================================================

INSERT INTO categories (id, nom) VALUES
    (1, 'Analgésiques'),
    (2, 'Antibiotiques'),
    (3, 'Anti-inflammatoires'),
    (4, 'Vitamines'),
    (5, 'Dermatologie');

-- Comptes de démo — hashes bcrypt VALIDES (cost 12)
--   admin  / Admin1234!
--   pro    / Pro12345!
--   client / Client12!
INSERT INTO users (id, username, password_hash, role) VALUES
    (1, 'admin',  '$2y$12$cPPI76bBsqwvn45NYzhFUeYZAXVLxzzcjurY7PNjMQWgURhI0ybYO', 'admin'),
    (2, 'pro',    '$2y$12$EYR0zaDI6k./v1FL6u/13OXLvlXwVtdi1DwbwcQwXyoP71YfTvBUq', 'professionnel'),
    (3, 'client', '$2y$12$LlBt/ne6EqecPBDKGQYKIuA6AQKO7dalGkLryza79yvPRMqNPI6iy', 'client');

INSERT INTO profils (user_id, nom, prenom, telephone, adresse) VALUES
    (1, 'Admin',   'Super',   '+262 692000001', 'Siège PharmaPays, Saint-Denis'),
    (2, 'Hoarau',  'Marie',   '+262 692000002', '12 rue de la Paix, Saint-Denis'),
    (3, 'Martin',  'Lucas',   '+262 692000003', '5 allée des Fleurs, Saint-Paul');

INSERT INTO medicaments (id, nom, description, prix, stock, ordonnance_requise, categorie_id) VALUES
    (1, 'Doliprane 1000mg',  'Paracétamol, douleurs et fièvre',            3.50, 50, 0, 1),
    (2, 'Ibuprofène 400mg',  'Anti-inflammatoire non stéroïdien',          4.20, 30, 0, 3),
    (3, 'Efferalgan 500mg',  'Paracétamol effervescent',                   3.00, 40, 0, 1),
    (4, 'Amoxicilline 500mg','Antibiotique large spectre (sur ordonnance)',8.90, 20, 1, 2),
    (5, 'Vitamine C 1000',   'Complément alimentaire immunité',            5.50, 60, 0, 4),
    (6, 'Augmentin 1g',      'Antibiotique (sur ordonnance)',             11.40,  8, 1, 2),
    (7, 'Biafine',           'Émulsion pour brûlures et irritations',      6.80, 25, 0, 5),
    (8, 'Aspirine 500mg',    'Acide acétylsalicylique',                    2.90,  0, 0, 1);

-- ============================================================
-- FIN DU SCRIPT
-- ============================================================
