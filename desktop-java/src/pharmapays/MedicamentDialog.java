package pharmapays;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Formulaire modal pour ajouter ou modifier un médicament.
 */
public class MedicamentDialog extends JDialog {

    private JTextField nomField;
    private JTextArea descField;
    private JTextField prixField;
    private JTextField stockField;
    private JCheckBox ordonnanceCheck;
    private JComboBox<String> categorieCombo;
    private List<String[]> categories;
    private boolean confirmed = false;

    public MedicamentDialog(JFrame parent, String title, Medicament med, MedicamentDAO dao) {
        super(parent, title, true);
        setSize(500, 480);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Charger les catégories
        try {
            categories = dao.findAllCategories();
        } catch (SQLException e) {
            categories = List.of();
        }

        initForm(med);
    }

    private void initForm(Medicament med) {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(new EmptyBorder(20, 25, 20, 25));
        main.setBackground(Color.WHITE);

        // --- Formulaire ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nom
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(createLabel("Nom *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        nomField = createTextField();
        form.add(nomField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(createLabel("Description"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        descField = new JTextArea(3, 20);
        descField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descField.setLineWrap(true);
        descField.setWrapStyleWord(true);
        descField.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane descScroll = new JScrollPane(descField);
        descScroll.setBorder(null);
        form.add(descScroll, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;

        // Prix
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(createLabel("Prix (EUR) *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        prixField = createTextField();
        form.add(prixField, gbc);

        // Stock
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(createLabel("Stock *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        stockField = createTextField();
        form.add(stockField, gbc);

        // Catégorie
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        form.add(createLabel("Categorie"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        categorieCombo = new JComboBox<>();
        categorieCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        for (String[] cat : categories) {
            categorieCombo.addItem(cat[1]);
        }
        form.add(categorieCombo, gbc);

        // Ordonnance requise
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        ordonnanceCheck = new JCheckBox("Ordonnance requise");
        ordonnanceCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ordonnanceCheck.setOpaque(false);
        form.add(ordonnanceCheck, gbc);

        // Pré-remplir si modification
        if (med != null) {
            nomField.setText(med.getNom());
            descField.setText(med.getDescription());
            prixField.setText(String.valueOf(med.getPrix()));
            stockField.setText(String.valueOf(med.getStock()));
            ordonnanceCheck.setSelected(med.isOrdonnanceRequise());
            // Sélectionner la catégorie
            for (int i = 0; i < categories.size(); i++) {
                if (Integer.parseInt(categories.get(i)[0]) == med.getCategorieId()) {
                    categorieCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // --- Boutons ---
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton cancelBtn = new JButton("Annuler");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(med == null ? "Ajouter" : "Enregistrer");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(new Color(34, 139, 34));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setOpaque(true);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(new EmptyBorder(10, 25, 10, 25));
        saveBtn.addActionListener(e -> {
            if (validateForm()) {
                confirmed = true;
                dispose();
            }
        });

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        main.add(form, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private boolean validateForm() {
        if (nomField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom est obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE);
            nomField.requestFocus();
            return false;
        }
        try {
            double prix = Double.parseDouble(prixField.getText().trim());
            if (prix < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Le prix doit etre un nombre positif.", "Validation", JOptionPane.WARNING_MESSAGE);
            prixField.requestFocus();
            return false;
        }
        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Le stock doit etre un nombre entier positif.", "Validation", JOptionPane.WARNING_MESSAGE);
            stockField.requestFocus();
            return false;
        }
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Medicament getMedicament() {
        Medicament med = new Medicament();
        med.setNom(nomField.getText().trim());
        med.setDescription(descField.getText().trim());
        med.setPrix(Double.parseDouble(prixField.getText().trim()));
        med.setStock(Integer.parseInt(stockField.getText().trim()));
        med.setOrdonnanceRequise(ordonnanceCheck.isSelected());

        int selectedIndex = categorieCombo.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < categories.size()) {
            med.setCategorieId(Integer.parseInt(categories.get(selectedIndex)[0]));
        }

        return med;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }
}
