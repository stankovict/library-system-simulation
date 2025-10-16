package gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import enumi.Pol;
import manager.UlogovaniKorisnik;
import modeli.Clan;
import modeli.ClanskaKarta;
import modeli.PosebnaKategorija;
import enumi.Clanarina;

public class DodajClanaDialog extends JDialog {

    private JTextField tfIme, tfPrezime, tfTelefon, tfAdresa, tfKorisnickoIme;
    private JPasswordField pfLozinka;
    private JComboBox<Pol> cbPol;
    private JComboBox<PosebnaKategorija> cbKategorija;
    private JComboBox<Clanarina> cbClanarina;
    private JSpinner spDatumRodjenja;

    private String fajl = "podaci/clanovi.csv"; 
    private String fajlKategorije = "podaci/posebnekategorije.csv";

    public DodajClanaDialog(JFrame parent, boolean modal) {
        super(parent, "Dodaj novog člana", modal);
        setSize(400, 520);
        setLocationRelativeTo(parent);
        setLayout(null);

        tfIme = dodajPolje("Ime:", 20);
        tfPrezime = dodajPolje("Prezime:", 60);
        cbPol = dodajComboBox("Pol:", Pol.values(), 100);
        spDatumRodjenja = dodajDatum("Datum rodjenja:", 140);
        tfTelefon = dodajPolje("Telefon:", 180);
        tfAdresa = dodajPolje("Adresa:", 220);
        tfKorisnickoIme = dodajPolje("Korisnicko ime:", 260);
        pfLozinka = dodajPasswordPolje("Lozinka:", 300);

        List<PosebnaKategorija> kategorije = ucitajKategorije();
        cbKategorija = new JComboBox<>(kategorije.toArray(new PosebnaKategorija[0]));
        JLabel lblKategorija = new JLabel("Kategorija:");
        lblKategorija.setBounds(20, 340, 180, 25);
        cbKategorija.setBounds(200, 340, 150, 25);
        add(lblKategorija);
        add(cbKategorija);

        cbClanarina = dodajComboBox("Tip članarine:", Clanarina.values(), 380);

        JButton btnDodaj = new JButton("Dodaj");
        btnDodaj.setBounds(140, 430, 100, 30);
        add(btnDodaj);

        btnDodaj.addActionListener(e -> {
            try {
                LocalDate datum = ((Date) spDatumRodjenja.getValue())
                        .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

                PosebnaKategorija kategorija = (PosebnaKategorija) cbKategorija.getSelectedItem();

                Clan clan = new Clan(
                        tfIme.getText(),
                        tfPrezime.getText(),
                        (Pol) cbPol.getSelectedItem(),
                        datum,
                        tfTelefon.getText(),
                        tfAdresa.getText(),
                        tfKorisnickoIme.getText(),
                        new String(pfLozinka.getPassword()),
                        kategorija,
                        (Clanarina) cbClanarina.getSelectedItem()
                );

                try (OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(fajl, true), StandardCharsets.UTF_8)) {

                    writer.write(clan.toCSV());
                    writer.write(System.lineSeparator());

                    ClanskaKarta karta = new ClanskaKarta(
                            clan.getUsername(),
                            clan.getClanarina(),
                            LocalDate.now()
                    );

                    try (OutputStreamWriter writer1 = new OutputStreamWriter(
                            new FileOutputStream("podaci/clanskekarte.csv", true), StandardCharsets.UTF_8)) {

                        writer1.write(karta.toCSV());
                        writer1.write(System.lineSeparator());
                    }

                    JOptionPane.showMessageDialog(this, "Član i članska karta uspešno dodati!");
                    dispose();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Greška pri upisu u fajl: " + ex.getMessage());
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Greška: " + ex.getMessage());
            }
        });
    }

    private List<PosebnaKategorija> ucitajKategorije() {
        List<PosebnaKategorija> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlKategorije), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(";");
                if (tokens.length >= 1) {
                    String naziv = tokens[0];
                    double popust = tokens.length > 1 ? Double.parseDouble(tokens[1]) : 0;
                    lista.add(new PosebnaKategorija(naziv, popust));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Greška pri učitavanju kategorija: " + e.getMessage());
        }
        return lista;
    }

    private JTextField dodajPolje(String label, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 180, 25);
        add(lbl);

        JTextField tf = new JTextField();
        tf.setBounds(200, y, 150, 25);
        add(tf);
        return tf;
    }

    private JPasswordField dodajPasswordPolje(String label, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 180, 25);
        add(lbl);

        JPasswordField pf = new JPasswordField();
        pf.setBounds(200, y, 150, 25);
        add(pf);
        return pf;
    }

    private <T> JComboBox<T> dodajComboBox(String label, T[] values, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 180, 25);
        add(lbl);

        JComboBox<T> cb = new JComboBox<>(values);
        cb.setBounds(200, y, 150, 25);
        add(cb);
        return cb;
    }

    private JSpinner dodajDatum(String label, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 180, 25);
        add(lbl);

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SpinnerDateModel model = new SpinnerDateModel(today, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setBounds(200, y, 150, 25);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        add(spinner);
        return spinner;
    }
}
