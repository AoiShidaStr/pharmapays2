package pharmapays;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Application bureau PharmaPays — Interface graphique Swing.
 * Permet la consultation, la recherche et la gestion (CRUD) des médicaments
 * via une connexion JDBC à la base de données MySQL pharmapays.
 */
public class PharmaPaysApp extends JFrame {

    // --- Couleurs du thème (cohérent avec l'app web) ---
    private static final Color PRIMARY = new Color(30, 70, 50);
    private static final Color PRIMARY_LIGHT = new Color(45, 100, 70);
    private static final Color ACCENT = new Color(34, 139, 34);
    private static final Color BG = new Color(245, 245, 240);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT_DARK = new Color(33, 33, 33);
    private static final Color TEXT_MUTED = new Color(120, 120, 120);
    private static final Color DANGER = new Color(200, 50, 50);

    // --- Composants ---
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statusLabel;
    private JLabel countLabel;

    // --- DAO ---
    private final MedicamentDAO dao = new MedicamentDAO();

    public PharmaPaysApp() {
        setTitle("PharmaPays — Gestion de Pharmacie");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);

        // Icône de la fenêtre
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        initComponents();
        loadData();
    }

    /**
     * Initialise tous les composants de l'interface.
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // --- Header ---
        add(createHeader(), BorderLayout.NORTH);

        // --- Table centrale ---
        add(createTablePanel(), BorderLayout.CENTER);

        // --- Barre de statut ---
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    /**
     * Crée le header avec titre, recherche et boutons.
     */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Titre
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Pharma");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(WHITE);

        JLabel titleAccent = new JLabel("Pays");
        titleAccent.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleAccent.setForeground(new Color(144, 238, 144));

        JLabel subtitle = new JLabel("   Gestion des medicaments");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(200, 220, 200));

        titlePanel.add(titleLabel);
        titlePanel.add(titleAccent);
        titlePanel.add(subtitle);

        // Barre de recherche + boutons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(100, 150, 100), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.setToolTipText("Rechercher par nom ou description...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                performSearch();
            }
        });

        JButton addBtn = createButton("+ Ajouter", ACCENT, WHITE);
        addBtn.addActionListener(e -> showAddDialog());

        JButton refreshBtn = createButton("Actualiser", PRIMARY_LIGHT, WHITE);
        refreshBtn.addActionListener(e -> loadData());

        actionsPanel.add(new JLabel("  "));
        actionsPanel.add(searchField);
        actionsPanel.add(addBtn);
        actionsPanel.add(refreshBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionsPanel, BorderLayout.EAST);

        return header;
    }

    /**
     * Crée le panneau central avec le tableau des médicaments.
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(15, 25, 10, 25));

        // Colonnes du tableau
        String[] columns = {"ID", "Nom", "Description", "Prix (EUR)", "Stock", "Ordonnance", "Categorie"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Lecture seule — édition via formulaire
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(35);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(200, 235, 210));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Header du tableau
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(PRIMARY);
        tableHeader.setForeground(WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setBorder(new LineBorder(PRIMARY));

        // Largeurs des colonnes
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(200);  // Nom
        table.getColumnModel().getColumn(2).setPreferredWidth(300);  // Description
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Prix
        table.getColumnModel().getColumn(4).setPreferredWidth(70);   // Stock
        table.getColumnModel().getColumn(5).setPreferredWidth(90);   // Ordonnance
        table.getColumnModel().getColumn(6).setPreferredWidth(130);  // Catégorie

        // Centrer certaines colonnes
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Double-clic pour modifier
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEditDialog();
                }
            }
        });

        // Menu contextuel (clic droit)
        JPopupMenu popup = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Modifier");
        editItem.addActionListener(e -> showEditDialog());
        JMenuItem deleteItem = new JMenuItem("Supprimer");
        deleteItem.setForeground(DANGER);
        deleteItem.addActionListener(e -> deleteSelected());
        JMenuItem stockItem = new JMenuItem("Modifier le stock");
        stockItem.addActionListener(e -> showStockDialog());
        popup.add(editItem);
        popup.add(stockItem);
        popup.addSeparator();
        popup.add(deleteItem);
        table.setComponentPopupMenu(popup);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        scrollPane.getViewport().setBackground(WHITE);

        // Boutons sous le tableau
        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomButtons.setOpaque(false);

        JButton editBtn = createButton("Modifier", PRIMARY_LIGHT, WHITE);
        editBtn.addActionListener(e -> showEditDialog());

        JButton deleteBtn = createButton("Supprimer", DANGER, WHITE);
        deleteBtn.addActionListener(e -> deleteSelected());

        JButton stockBtn = createButton("Modifier stock", new Color(200, 150, 30), WHITE);
        stockBtn.addActionListener(e -> showStockDialog());

        bottomButtons.add(editBtn);
        bottomButtons.add(stockBtn);
        bottomButtons.add(deleteBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomButtons, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crée la barre de statut en bas.
     */
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(235, 235, 230));
        bar.setBorder(new EmptyBorder(5, 25, 5, 25));

        statusLabel = new JLabel("Pret");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_MUTED);

        countLabel = new JLabel("0 medicament(s)");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_MUTED);

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(countLabel, BorderLayout.EAST);

        return bar;
    }

    // ================================================================
    //  ACTIONS — CRUD
    // ================================================================

    /**
     * Charge tous les médicaments dans le tableau.
     */
    private void loadData() {
        try {
            List<Medicament> medicaments = dao.findAll();
            refreshTable(medicaments);
            setStatus("Donnees chargees depuis la base MySQL");
        } catch (SQLException e) {
            showError("Erreur de connexion a la base de donnees :\n" + e.getMessage());
            setStatus("Erreur de connexion");
        }
    }

    /**
     * Recherche en temps réel.
     */
    private void performSearch() {
        String terme = searchField.getText().trim();
        try {
            List<Medicament> results;
            if (terme.isEmpty()) {
                results = dao.findAll();
            } else {
                results = dao.search(terme);
            }
            refreshTable(results);
            setStatus("Recherche : \"" + terme + "\" — " + results.size() + " resultat(s)");
        } catch (SQLException e) {
            showError("Erreur lors de la recherche :\n" + e.getMessage());
        }
    }

    /**
     * Met à jour le tableau avec une liste de médicaments.
     */
    private void refreshTable(List<Medicament> medicaments) {
        tableModel.setRowCount(0);
        for (Medicament m : medicaments) {
            tableModel.addRow(new Object[]{
                m.getId(),
                m.getNom(),
                m.getDescription(),
                String.format("%.2f", m.getPrix()),
                m.getStock(),
                m.isOrdonnanceRequise() ? "Oui" : "Non",
                m.getCategorie() != null ? m.getCategorie() : "N/A"
            });
        }
        countLabel.setText(medicaments.size() + " medicament(s)");
    }

    /**
     * Dialogue d'ajout d'un médicament.
     */
    private void showAddDialog() {
        MedicamentDialog dialog = new MedicamentDialog(this, "Ajouter un medicament", null, dao);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                Medicament med = dialog.getMedicament();
                if (dao.create(med)) {
                    loadData();
                    setStatus("Medicament \"" + med.getNom() + "\" ajoute avec succes");
                }
            } catch (SQLException e) {
                showError("Erreur lors de l'ajout :\n" + e.getMessage());
            }
        }
    }

    /**
     * Dialogue de modification du médicament sélectionné.
     */
    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez un medicament a modifier.",
                "Aucune selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        try {
            Medicament med = dao.findById(id);
            if (med == null) return;

            MedicamentDialog dialog = new MedicamentDialog(this, "Modifier le medicament", med, dao);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                Medicament updated = dialog.getMedicament();
                updated.setId(id);
                if (dao.update(updated)) {
                    loadData();
                    setStatus("Medicament \"" + updated.getNom() + "\" modifie");
                }
            }
        } catch (SQLException e) {
            showError("Erreur lors de la modification :\n" + e.getMessage());
        }
    }

    /**
     * Supprime le médicament sélectionné après confirmation.
     */
    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez un medicament a supprimer.",
                "Aucune selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nom = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer le medicament \"" + nom + "\" ?\nCette action est irreversible.",
            "Confirmer la suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) tableModel.getValueAt(row, 0);
            try {
                if (dao.delete(id)) {
                    loadData();
                    setStatus("Medicament \"" + nom + "\" supprime");
                }
            } catch (SQLException e) {
                showError("Erreur lors de la suppression :\n" + e.getMessage());
            }
        }
    }

    /**
     * Dialogue rapide pour modifier le stock (appel procédure stockée).
     */
    private void showStockDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez un medicament.",
                "Aucune selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nom = (String) tableModel.getValueAt(row, 1);
        int currentStock = (int) tableModel.getValueAt(row, 4);
        int id = (int) tableModel.getValueAt(row, 0);

        String input = JOptionPane.showInputDialog(this,
            "Stock actuel de \"" + nom + "\" : " + currentStock + "\nNouvelle quantite :",
            "Modifier le stock", JOptionPane.PLAIN_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int newStock = Integer.parseInt(input.trim());
                if (newStock < 0) {
                    showError("Le stock ne peut pas etre negatif.");
                    return;
                }
                dao.majStock(id, newStock);
                loadData();
                setStatus("Stock de \"" + nom + "\" mis a jour : " + newStock);
            } catch (NumberFormatException e) {
                showError("Veuillez entrer un nombre valide.");
            } catch (SQLException e) {
                showError("Erreur lors de la mise a jour du stock :\n" + e.getMessage());
            }
        }
    }

    // ================================================================
    //  UTILITAIRES
    // ================================================================

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    // ================================================================
    //  POINT D'ENTRÉE
    // ================================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PharmaPaysApp app = new PharmaPaysApp();
            app.setVisible(true);
        });
    }
}
