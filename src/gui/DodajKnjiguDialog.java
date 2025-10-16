package gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import manager.ZanrMetode;
import modeli.Knjiga;
import modeli.Zanr;
import enumi.StatusKnjige;

public class DodajKnjiguDialog extends JDialog {

    private JTextField tfNaslov, tfAutor;
    private JComboBox<Zanr> cbZanr;
    private String fajl = "podaci/knjige.csv";

    public DodajKnjiguDialog(JFrame parent, boolean modal) {
        super(parent, "Dodaj novu knjigu", modal);
        setSize(450, 300);
        setLocationRelativeTo(parent);
        setLayout(null);

        tfNaslov = dodajPolje("Naslov:", 20);
        tfAutor = dodajPolje("Autor:", 60);

        List<Zanr> zanrovi = ZanrMetode.ucitajZanrove();
        cbZanr = dodajComboBox("Žanr:", zanrovi, 100);

        JButton btnDodajZanr = new JButton("Dodaj žanr");
        btnDodajZanr.setBounds(180, 140, 120, 25);
        add(btnDodajZanr);
        btnDodajZanr.addActionListener(e -> {
            String noviZanr = JOptionPane.showInputDialog(this, "Unesite naziv novog žanra:");
            if (noviZanr != null && !noviZanr.trim().isEmpty()) {
                Zanr z = new Zanr(noviZanr.trim());
                ZanrMetode.dodajZanr(z);
                cbZanr.addItem(z);
                cbZanr.setSelectedItem(z);
            }
        });

        JButton btnDodaj = new JButton("Dodaj");
        btnDodaj.setBounds(180, 180, 100, 30);
        add(btnDodaj);

        btnDodaj.addActionListener(e -> {
            try {
                String naslov = tfNaslov.getText().trim();
                String autor = tfAutor.getText().trim();
                Zanr zanr = (Zanr) cbZanr.getSelectedItem();

                if (naslov.isEmpty() || autor.isEmpty() || zanr == null) {
                    JOptionPane.showMessageDialog(this, "Sva polja moraju biti popunjena!");
                    return;
                }

                int id = 1;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(fajl), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] delovi = line.split(";");
                        int postojećiId = Integer.parseInt(delovi[3]);
                        if (postojećiId >= id) {
                            id = postojećiId + 1;
                        }
                    }
                } catch (IOException ignored) {}

                Knjiga knjiga = new Knjiga(naslov, autor, zanr.getNaziv(), id, StatusKnjige.DOSTUPNA);

                try (OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(fajl, true), StandardCharsets.UTF_8)) {
                    writer.write(knjiga.toCSV());
                    writer.write(System.lineSeparator());
                    JOptionPane.showMessageDialog(this, "Knjiga uspešno dodata!");
                    dispose();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Greška pri upisu u fajl: " + ex.getMessage());
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Greška: " + ex.getMessage());
            }
        });
    }

    private JTextField dodajPolje(String label, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 150, 25);
        add(lbl);

        JTextField tf = new JTextField();
        tf.setBounds(180, y, 180, 25);
        add(tf);
        return tf;
    }

    private JComboBox<Zanr> dodajComboBox(String label, List<Zanr> lista, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 150, 25);
        add(lbl);

        JComboBox<Zanr> cb = new JComboBox<>(lista.toArray(new Zanr[0]));
        cb.setBounds(180, y, 180, 25);
        add(cb);
        return cb;
    }
}
