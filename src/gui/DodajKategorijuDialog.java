package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class DodajKategorijuDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTextField tfNaziv;
    private JTextField tfPopust;

    private String naziv;
    private double popust;
    private boolean potvrđeno = false;

    public static void main(String[] args) {
        try {
            DodajKategorijuDialog dialog = new DodajKategorijuDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);

            if (dialog.isPotvrđeno()) {
                System.out.println("Naziv: " + dialog.getNaziv());
                System.out.println("Popust: " + dialog.getPopust());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DodajKategorijuDialog() {
        setTitle("Dodaj novu kategoriju");
        setModal(true);
        setBounds(100, 100, 350, 200);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(null);

        JLabel lblNaziv = new JLabel("Naziv kategorije:");
        lblNaziv.setBounds(10, 20, 120, 25);
        contentPanel.add(lblNaziv);

        tfNaziv = new JTextField();
        tfNaziv.setBounds(140, 20, 150, 25);
        contentPanel.add(tfNaziv);

        JLabel lblPopust = new JLabel("Popust (%):");
        lblPopust.setBounds(10, 60, 120, 25);
        contentPanel.add(lblPopust);

        tfPopust = new JTextField();
        tfPopust.setBounds(140, 60, 150, 25);
        contentPanel.add(tfPopust);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    naziv = tfNaziv.getText().trim();
                    popust = Double.parseDouble(tfPopust.getText().trim());
                    potvrđeno = true;
                    dispose();
                } catch (NumberFormatException ex) {
                    tfPopust.setText("");
                    tfPopust.requestFocus();
                }
            }
        });
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPane.add(cancelButton);
    }

    public String getNaziv() {
        return naziv;
    }

    public double getPopust() {
        return popust;
    }

    public boolean isPotvrđeno() {
        return potvrđeno;
    }
}
