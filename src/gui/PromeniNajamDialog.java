package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;

import manager.NajamManager;

public class PromeniNajamDialog extends JDialog {

    private JTextField txtBrojDana;

    public PromeniNajamDialog(JFrame parent) {
        super(parent, "Promena default najma", true);
        setSize(300, 150);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel.add(new JLabel("Unesi novi broj dana za default najam:"));

        txtBrojDana = new JTextField(String.valueOf(NajamManager.ucitajDefaultNajam()));
        panel.add(txtBrojDana);

        add(panel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSacuvaj = new JButton("Sačuvaj");
        btnSacuvaj.addActionListener(e -> {
            try {
                int noviNajam = Integer.parseInt(txtBrojDana.getText().trim());
                if (noviNajam <= 0) {
                    JOptionPane.showMessageDialog(this, "Broj dana mora biti pozitivan!", "Greška", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (NajamManager.postaviDefaultNajam(noviNajam)) {
                    JOptionPane.showMessageDialog(this, "Default najam uspešno promenjen!");
                    dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neispravan broj!", "Greška", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("Odustani");
        btnCancel.addActionListener(e -> dispose());

        buttons.add(btnSacuvaj);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);
    }
}
