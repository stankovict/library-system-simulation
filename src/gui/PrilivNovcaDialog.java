package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class PrilivNovcaDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTextField tfIznos;

    public static void main(String[] args) {
        try {
            PrilivNovcaDialog dialog = new PrilivNovcaDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PrilivNovcaDialog() {
        setTitle("Priliv novca");
        setBounds(100, 100, 350, 150);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new FlowLayout());

        JLabel lblIznos = new JLabel("Unesite iznos (RSD):");
        contentPanel.add(lblIznos);

        tfIznos = new JTextField(15);
        contentPanel.add(tfIznos);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        JButton btnSacuvaj = new JButton("Sačuvaj");
        btnSacuvaj.addActionListener((ActionEvent e) -> {
            try {
                double iznos = Double.parseDouble(tfIznos.getText().trim());
                LocalDate danas = LocalDate.now();

                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("podaci/sredstva.csv", true), StandardCharsets.UTF_8))) {
                    bw.write(iznos + ";" + danas);
                    bw.newLine();
                }

                dispose();
            } catch (NumberFormatException ex) {
                tfIznos.setText("Nevažeći unos!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        buttonPane.add(btnSacuvaj);
        getRootPane().setDefaultButton(btnSacuvaj);

        JButton btnOtkazi = new JButton("Otkaži");
        btnOtkazi.addActionListener(e -> dispose());
        buttonPane.add(btnOtkazi);
    }
}
