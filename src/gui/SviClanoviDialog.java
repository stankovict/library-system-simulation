package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class SviClanoviDialog extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnIzmeni;
    private JButton btnObrisi;

    public SviClanoviDialog(JFrame parent) {
        super(parent, "Svi članovi", true);
        setSize(1000, 500);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(
            new String[]{"Ime", "Prezime", "Pol", "Datum rođenja", "Telefon",
                        "Adresa", "Username", "Šifra", "Kategorija", "Članarina"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            btnIzmeni.setEnabled(hasSelection);
            btnObrisi.setEnabled(hasSelection);
        });

        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        btnIzmeni = new JButton("Izmeni");
        btnIzmeni.setEnabled(false);
        btnIzmeni.addActionListener(e -> izmeniClana());

        btnObrisi = new JButton("Obrisi");
        btnObrisi.setEnabled(false);
        btnObrisi.addActionListener(e -> obrisiClana());

        JButton btnNazad = new JButton("Nazad");
        btnNazad.addActionListener(e -> dispose());

        buttonPanel.add(btnIzmeni);
        buttonPanel.add(btnObrisi);
        buttonPanel.add(btnNazad);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        ucitajClanove();
    }

    private void ucitajClanove() {
        tableModel.setRowCount(0);
        String fajl = "podaci/clanovi.csv";
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("d.M.yyyy"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        };

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fajl), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length >= 10) {
                    LocalDate datum = null;
                    for (DateTimeFormatter fmt : formatters) {
                        try {
                            datum = LocalDate.parse(delovi[3].trim(), fmt);
                            break;
                        } catch (DateTimeParseException ignored) {}
                    }
                    String datumStr = (datum != null) ? datum.toString() : delovi[3].trim();

                    tableModel.addRow(new Object[]{
                            delovi[0].trim(),
                            delovi[1].trim(),
                            delovi[2].trim(),
                            datumStr,
                            delovi[4].trim(),
                            delovi[5].trim(),
                            delovi[6].trim(),
                            delovi[7].trim(),
                            delovi[8].trim(),
                            delovi[9].trim()
                    });
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Greška pri čitanju fajla " + fajl + ": " + e.getMessage());
        }
    }

    private void izmeniClana() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        String ime = (String) tableModel.getValueAt(selectedRow, 0);
        String prezime = (String) tableModel.getValueAt(selectedRow, 1);
        String pol = (String) tableModel.getValueAt(selectedRow, 2);
        String datumRodjenja = (String) tableModel.getValueAt(selectedRow, 3);
        String telefon = (String) tableModel.getValueAt(selectedRow, 4);
        String adresa = (String) tableModel.getValueAt(selectedRow, 5);
        String staroUsername = (String) tableModel.getValueAt(selectedRow, 6);
        String sifra = (String) tableModel.getValueAt(selectedRow, 7);
        String kategorija = (String) tableModel.getValueAt(selectedRow, 8);
        String clanarina = (String) tableModel.getValueAt(selectedRow, 9);

 
        JTextField txtIme = new JTextField(ime, 20);
        JTextField txtPrezime = new JTextField(prezime, 20);
        JTextField txtTelefon = new JTextField(telefon, 20);
        JTextField txtAdresa = new JTextField(adresa, 20);
        JTextField txtUsername = new JTextField(staroUsername, 20);
        JPasswordField txtSifra = new JPasswordField(sifra, 20);

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.add(new JLabel("Ime:")); panel.add(txtIme);
        panel.add(new JLabel("Prezime:")); panel.add(txtPrezime);
        panel.add(new JLabel("Telefon:")); panel.add(txtTelefon);
        panel.add(new JLabel("Adresa:")); panel.add(txtAdresa);
        panel.add(new JLabel("Username:")); panel.add(txtUsername);
        panel.add(new JLabel("Šifra:")); panel.add(txtSifra);

        int result = JOptionPane.showConfirmDialog(this, panel, "Izmeni člana",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String novoIme = txtIme.getText().trim();
            String novoPrezime = txtPrezime.getText().trim();
            String noviTelefon = txtTelefon.getText().trim();
            String novaAdresa = txtAdresa.getText().trim();
            String noviUsername = txtUsername.getText().trim();
            String novaSifra = new String(txtSifra.getPassword()).trim();

            if (novoIme.isEmpty() || novoPrezime.isEmpty() || noviUsername.isEmpty() || novaSifra.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Obavezna polja ne smeju biti prazna!");
                return;
            }

            if (azurirajUFajlu("podaci/clanovi.csv", staroUsername, novoIme, novoPrezime,
                    pol, datumRodjenja, noviTelefon, novaAdresa, noviUsername, novaSifra,
                    kategorija, clanarina)) {
                tableModel.setValueAt(novoIme, selectedRow, 0);
                tableModel.setValueAt(novoPrezime, selectedRow, 1);
                tableModel.setValueAt(noviTelefon, selectedRow, 4);
                tableModel.setValueAt(novaAdresa, selectedRow, 5);
                tableModel.setValueAt(noviUsername, selectedRow, 6);
                tableModel.setValueAt(novaSifra, selectedRow, 7);
                JOptionPane.showMessageDialog(this, "Član uspešno izmenjen!");
            }
        }
    }


    private void obrisiClana() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        String ime = (String) tableModel.getValueAt(selectedRow, 0);
        String prezime = (String) tableModel.getValueAt(selectedRow, 1);
        String username = (String) tableModel.getValueAt(selectedRow, 6);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Da li ste sigurni da želite obrisati člana: " + ime + " " + prezime + " (" + username + ")?",
                "Potvrda brisanja", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (obrisiIzFajla("podaci/clanovi.csv", username)) {
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Član uspešno obrisan!");
            }
        }
    }

    private boolean azurirajUFajlu(String fajl, String staroUsername, String ime,
                                   String prezime, String pol, String datumRodjenja,
                                   String telefon, String adresa, String username,
                                   String sifra, String kategorija, String clanarina) {
        try {
            List<String> linije = Files.readAllLines(Paths.get(fajl), StandardCharsets.UTF_8);
            List<String> noveLinije = new ArrayList<>();

            for (String linija : linije) {
                String[] delovi = linija.split(";");
                if (delovi.length >= 7 && delovi[6].trim().equals(staroUsername)) {
                    String novaLinija = ime + ";" + prezime + ";" + pol + ";" + datumRodjenja + ";" +
                            telefon + ";" + adresa + ";" + username + ";" + sifra + ";" +
                            kategorija + ";" + clanarina;
                    noveLinije.add(novaLinija);
                } else {
                    noveLinije.add(linija);
                }
            }

            Files.write(Paths.get(fajl), noveLinije, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Greška pri ažuriranju fajla: " + e.getMessage());
            return false;
        }
    }

    private boolean obrisiIzFajla(String fajl, String username) {
        try {
            List<String> linije = Files.readAllLines(Paths.get(fajl), StandardCharsets.UTF_8);
            List<String> noveLinije = new ArrayList<>();

            for (String linija : linije) {
                String[] delovi = linija.split(";");
                if (delovi.length >= 7 && !delovi[6].trim().equals(username)) {
                    noveLinije.add(linija);
                }
            }

            Files.write(Paths.get(fajl), noveLinije, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Greška pri brisanju iz fajla: " + e.getMessage());
            return false;
        }
    }
}
