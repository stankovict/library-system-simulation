package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SviZaposleniDialog extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnIzmeni;
    private JButton btnObrisi;

    public SviZaposleniDialog(JFrame parent) {
        super(parent, "Svi zaposleni", true);
        setSize(1000, 500);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(
            new String[]{"Uloga", "Ime", "Prezime", "Pol", "Datum rođenja", "Telefon", 
                        "Adresa", "Korisničko ime", "Lozinka", "Obrazovanje", 
                        "Iskustvo", "Plata"}, 0) {
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
        btnIzmeni.addActionListener(e -> izmeniZaposlenog());
        
        btnObrisi = new JButton("Obrisi");
        btnObrisi.setEnabled(false);
        btnObrisi.addActionListener(e -> obrisiZaposlenog());
        
        JButton btnNazad = new JButton("Nazad");
        btnNazad.addActionListener(e -> dispose());
        
        buttonPanel.add(btnIzmeni);
        buttonPanel.add(btnObrisi);
        buttonPanel.add(btnNazad);
        
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        ucitajZaposlene();
    }

    private void ucitajZaposlene() {
        tableModel.setRowCount(0);
        ucitajIZapisi("podaci/admini.csv", "Admin");
        ucitajIZapisi("podaci/bibliotekari.csv", "Bibliotekar");
    }

    private void ucitajIZapisi(String fajl, String uloga) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajl), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length >= 11) {
                    tableModel.addRow(new Object[]{
                        uloga, 
                        delovi[0].trim(),
                        delovi[1].trim(),
                        delovi[2].trim(),
                        delovi[3].trim(),
                        delovi[4].trim(),
                        delovi[5].trim(),
                        delovi[6].trim(),
                        delovi[7].trim(),
                        delovi[8].trim(),
                        delovi[9].trim(),         
                        delovi.length > 10 ? delovi[10].trim() : ""
                    });
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Greška pri čitanju fajla " + fajl + ": " + e.getMessage());
        }
    }

    private void izmeniZaposlenog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        String uloga = (String) tableModel.getValueAt(selectedRow, 0);
        String ime = (String) tableModel.getValueAt(selectedRow, 1);
        String prezime = (String) tableModel.getValueAt(selectedRow, 2);
        String pol = (String) tableModel.getValueAt(selectedRow, 3);
        String datumRodjenja = (String) tableModel.getValueAt(selectedRow, 4);
        String telefon = (String) tableModel.getValueAt(selectedRow, 5);
        String adresa = (String) tableModel.getValueAt(selectedRow, 6);
        String staroKorisnickoIme = (String) tableModel.getValueAt(selectedRow, 7);
        String lozinka = (String) tableModel.getValueAt(selectedRow, 8);
        String obrazovanje = (String) tableModel.getValueAt(selectedRow, 9);
        String iskustvo = (String) tableModel.getValueAt(selectedRow, 10);
        String plata = (String) tableModel.getValueAt(selectedRow, 11);

        JTextField txtIme = new JTextField(ime, 20);
        JTextField txtPrezime = new JTextField(prezime, 20);
        JComboBox<String> cmbPol = new JComboBox<>(new String[]{"M", "Ž"});
        cmbPol.setSelectedItem(pol);
        JTextField txtTelefon = new JTextField(telefon, 20);
        JTextField txtAdresa = new JTextField(adresa, 20);
        JTextField txtKorisnickoIme = new JTextField(staroKorisnickoIme, 20);
        JPasswordField txtLozinka = new JPasswordField(lozinka, 20);
        
        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        panel.add(new JLabel("Ime:"));
        panel.add(txtIme);
        panel.add(new JLabel("Prezime:"));
        panel.add(txtPrezime);
        panel.add(new JLabel("Pol:"));
        panel.add(cmbPol);
        panel.add(new JLabel("Telefon:"));
        panel.add(txtTelefon);
        panel.add(new JLabel("Adresa:"));
        panel.add(txtAdresa);
        panel.add(new JLabel("Korisničko ime:"));
        panel.add(txtKorisnickoIme);
        panel.add(new JLabel("Lozinka:"));
        panel.add(txtLozinka);

        int result = JOptionPane.showConfirmDialog(this, panel, "Izmeni zaposlenog", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String novoIme = txtIme.getText().trim();
            String novoPrezime = txtPrezime.getText().trim();
            String noviPol = (String) cmbPol.getSelectedItem();
            String noviTelefon = txtTelefon.getText().trim();
            String novaAdresa = txtAdresa.getText().trim();
            String novoKorisnickoIme = txtKorisnickoIme.getText().trim();
            String novaLozinka = new String(txtLozinka.getPassword()).trim();

            if (novoIme.isEmpty() || novoPrezime.isEmpty() || novoKorisnickoIme.isEmpty() || novaLozinka.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Obavezna polja ne smeju biti prazna!");
                return;
            }

            String fajl = uloga.equals("Admin") ? "podaci/admini.csv" : "podaci/bibliotekari.csv";
            
            if (azurirajUFajlu(fajl, staroKorisnickoIme, novoIme, novoPrezime, noviPol, 
                              datumRodjenja, noviTelefon, novaAdresa, novoKorisnickoIme, 
                              novaLozinka, obrazovanje, iskustvo, plata)) {
                tableModel.setValueAt(novoIme, selectedRow, 1);
                tableModel.setValueAt(novoPrezime, selectedRow, 2);
                tableModel.setValueAt(noviPol, selectedRow, 3);
                tableModel.setValueAt(noviTelefon, selectedRow, 5);
                tableModel.setValueAt(novaAdresa, selectedRow, 6);
                tableModel.setValueAt(novoKorisnickoIme, selectedRow, 7);
                tableModel.setValueAt(novaLozinka, selectedRow, 8);
                JOptionPane.showMessageDialog(this, "Zaposleni uspešno izmenjen!");
            }
        }
    }

    private void obrisiZaposlenog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        String uloga = (String) tableModel.getValueAt(selectedRow, 0);
        String ime = (String) tableModel.getValueAt(selectedRow, 1);
        String prezime = (String) tableModel.getValueAt(selectedRow, 2);
        String korisnickoIme = (String) tableModel.getValueAt(selectedRow, 7);

        int confirm = JOptionPane.showConfirmDialog(this, 
                "Da li ste sigurni da želite obrisati zaposlenog: " + ime + " " + prezime + " (" + korisnickoIme + ")?",
                "Potvrda brisanja", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String fajl = uloga.equals("Admin") ? "podaci/admini.csv" : "podaci/bibliotekari.csv";
            
            if (obrisiIzFajla(fajl, korisnickoIme)) {
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Zaposleni uspešno obrisan!");
            }
        }
    }

    private boolean azurirajUFajlu(String fajl, String staroKorisnickoIme, String ime, 
                                    String prezime, String pol, String datumRodjenja, 
                                    String telefon, String adresa, String korisnickoIme, 
                                    String lozinka, String obrazovanje, 
                                    String iskustvo, String plata) {
        try {
            List<String> linije = Files.readAllLines(Paths.get(fajl), StandardCharsets.UTF_8);
            List<String> noveLinije = new ArrayList<>();
            
            for (String linija : linije) {
                String[] delovi = linija.split(";");
                if (delovi.length >= 7 && delovi[6].trim().equals(staroKorisnickoIme)) {

                    String novaLinija = ime + ";" + prezime + ";" + pol + ";" + datumRodjenja + ";" + 
                                       telefon + ";" + adresa + ";" + korisnickoIme + ";" + 
                                       lozinka + ";" + obrazovanje + ";" + iskustvo + ";" + plata;
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

    private boolean obrisiIzFajla(String fajl, String korisnickoIme) {
        try {
            List<String> linije = Files.readAllLines(Paths.get(fajl), StandardCharsets.UTF_8);
            List<String> noveLinije = new ArrayList<>();
            
            for (String linija : linije) {
                String[] delovi = linija.split(";");
                if (delovi.length >= 7 && !delovi[6].trim().equals(korisnickoIme)) {
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