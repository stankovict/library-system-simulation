package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import enumi.StatusKnjige;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import manager.UlogovaniKorisnik;
import modeli.*;

public class IzdavanjeDialog extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;
    private String fajlRezervacije = "podaci/rezervacije.csv";
    private int idKnjige = 0;
    private Cenovnik trenutniCenovnik;

    private boolean rezervacijaIzabrana = false;

    public IzdavanjeDialog(JFrame parent, boolean modal, Cenovnik cenovnik,int idKnj) {
        super(parent, "Rezervacije za danas", modal);
        
        this.idKnjige = idKnj;

        this.trenutniCenovnik = cenovnik;

        setSize(700, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID","Korisnik", "ID Knjige", "Naslov", "Datum kraja", "Cena","Usluge"}, 0);
        table = new JTable(tableModel);

        ucitajRezervacije();

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnOdaberi = new JButton("Izdaj knjigu članu");
        btnOdaberi.addActionListener(e -> odaberiRezervaciju());
        buttonPanel.add(btnOdaberi);
        
        JButton btnCancel = new JButton("Otkaži");
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnCancel);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void ucitajRezervacije() {
        tableModel.setRowCount(0);
        LocalDate danas = LocalDate.now();

        List<String> izdateRezervacije = new ArrayList<>();
        String fajlIzdavanje = "podaci/izdavanje.csv";

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fajlIzdavanje), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length > 8) {

                    izdateRezervacije.add(delovi[8].trim());
                }
            }
        } catch (IOException e) {

        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fajlRezervacije), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length >= 10) {
                    try {
                        String id = delovi[0].trim();
                        String korisnik = delovi[1].trim();
                        String knjigaId = delovi[2].trim();
                        String naslov = delovi[3].trim();
                        String datumOdStr = delovi[4].trim();
                        String datumDoStr = delovi[5].trim();
                        String status = delovi[6].trim();
                        String cenaStr = delovi[7].trim();
                        String usluge = delovi[9].trim();

                        LocalDate datumOd = LocalDate.parse(datumOdStr);
                        LocalDate datumDo = LocalDate.parse(datumDoStr);

                        if (izdateRezervacije.contains(id)) {
                            continue;
                        }
                        


                        boolean danasURasponu = !danas.isBefore(datumOd) && !danas.isAfter(datumDo);
                        boolean idOdgovara = (this.idKnjige == Integer.parseInt(knjigaId));
                        boolean aktivna = !"ODBIJENA".equalsIgnoreCase(status) && !"OTKAZANA".equalsIgnoreCase(status) && !"NA_ČEKANJU".equalsIgnoreCase(status);

                        if (danasURasponu && idOdgovara && aktivna) {
                            double cena = 0.0;
                            try {
                                cena = Double.parseDouble(cenaStr);
                            } catch (Exception ex) {
                                if (trenutniCenovnik != null && !trenutniCenovnik.getClanarine().isEmpty()) {
                                    cena = trenutniCenovnik.getClanarine().get(0).getCena();
                                }
                            }

                            tableModel.addRow(new Object[]{
                                    id,
                                    korisnik,
                                    knjigaId,
                                    naslov,
                                    datumDo,
                                    cena,
                                    usluge.isEmpty() ? "Nema" : usluge
                            });
                        }
                    } catch (Exception ex) {

                        continue;
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Greška pri učitavanju rezervacija: " + e.getMessage());
        }
    }


    private void odaberiRezervaciju() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Izaberite rezervaciju koju želite da izdate.");
            return;
        }

        String rezervacijaId = tableModel.getValueAt(row, 0).toString();
        String idKnjige = tableModel.getValueAt(row, 2).toString();
        String naslov = tableModel.getValueAt(row, 3).toString();
        String korisnik = (String) tableModel.getValueAt(row, 1);
        LocalDate datumKraja = (LocalDate) tableModel.getValueAt(row, 4);
        double osnovnaCena = Double.parseDouble(tableModel.getValueAt(row, 5).toString());
        String postojeceUslugeStr = tableModel.getValueAt(row, 6).toString();

        List<String> postojeceUsluge = new ArrayList<>();
        if (!postojeceUslugeStr.equals("Nema") && !postojeceUslugeStr.isEmpty()) {
            String[] uslugeArray = postojeceUslugeStr.split(",");
            for (String usluga : uslugeArray) {
                postojeceUsluge.add(usluga.trim());
            }
        }

        String poruka = String.format(
            "Izdavanje knjige:\n" +
            "Član: %s\n" +
            "Knjiga: %s (ID: %s)\n" +
            "Datum vraćanja: %s\n" +
            "Osnovna cena: %.2f RSD\n" +
            "Postojeće usluge: %s\n\n" +
            "Da li želite da dodate dodatne usluge?",
            korisnik, naslov, idKnjige, datumKraja, osnovnaCena, 
            postojeceUsluge.isEmpty() ? "Nema" : String.join(", ", postojeceUsluge)
        );

        int izbor = JOptionPane.showConfirmDialog(this, poruka, "Potvrda izdavanja", JOptionPane.YES_NO_CANCEL_OPTION);

        if (izbor == JOptionPane.CANCEL_OPTION) {
            return;
        }

        double konacnaCena = osnovnaCena;
        List<String> dodateUsluge = new ArrayList<>();

        if (izbor == JOptionPane.YES_OPTION) {
            List<Usluga> dostupneUsluge = trenutniCenovnik.getUsluge();
            List<String> noveUsluge = new ArrayList<>();

            for (Usluga usluga : dostupneUsluge) {

                boolean vecPostoji = false;
                for (String postojeca : postojeceUsluge) {
                    if (postojeca.trim().equalsIgnoreCase(usluga.getNaziv().trim())) {
                        vecPostoji = true;
                        break;
                    }
                }
                
                if (!vecPostoji) {
                    noveUsluge.add(usluga.getNaziv() + " - " + usluga.getCena() + " RSD");
                }
            }

            if (noveUsluge.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Sve dostupne usluge su već uključene u rezervaciju.",
                    "Informacija", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String[] uslugeArray = noveUsluge.toArray(new String[0]);
                JList<String> listaUsluga = new JList<>(uslugeArray);
                listaUsluga.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

                JScrollPane scrollPane = new JScrollPane(listaUsluga);
                scrollPane.setPreferredSize(new Dimension(300, 200));

                int potvrdi = JOptionPane.showConfirmDialog(this,
                        scrollPane,
                        "Izaberite dodatne usluge (već uključene usluge su uklonjene)",
                        JOptionPane.OK_CANCEL_OPTION);

                if (potvrdi == JOptionPane.OK_OPTION) {
                    for (String uslugaStr : listaUsluga.getSelectedValuesList()) {
                        String[] delovi = uslugaStr.split(" - ");
                        if (delovi.length >= 2) {
                            try {
                                String nazivUsluge = delovi[0].trim();
                                double cenaUsluge = Double.parseDouble(delovi[1].replace(" RSD", "").trim());
                                konacnaCena += cenaUsluge;
                                dodateUsluge.add(nazivUsluge);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(this, 
                                    "Greška pri obradi cene usluge: " + uslugaStr,
                                    "Greška", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                }
            }
        }

        StringBuilder finalnaPoruka = new StringBuilder();
        finalnaPoruka.append("FINALNA POTVRDA IZDAVANJA:\n\n");
        finalnaPoruka.append("Član: ").append(korisnik).append("\n");
        finalnaPoruka.append("Knjiga: ").append(naslov).append(" (ID: ").append(idKnjige).append(")\n");
        finalnaPoruka.append("Datum vraćanja: ").append(datumKraja).append("\n");
        finalnaPoruka.append("Osnovna cena: ").append(String.format("%.2f RSD", osnovnaCena)).append("\n");
        
        if (!dodateUsluge.isEmpty()) {
            finalnaPoruka.append("Dodatne usluge: ").append(String.join(", ", dodateUsluge)).append("\n");
        }
        
        finalnaPoruka.append("UKUPNO ZA NAPLATU: ").append(String.format("%.2f RSD", konacnaCena)).append("\n\n");
        finalnaPoruka.append("Da li potvrđujete izdavanje knjige?");

        int finalnaPotvr = JOptionPane.showConfirmDialog(this, 
            finalnaPoruka.toString(), 
            "Finalna potvrda", 
            JOptionPane.YES_NO_OPTION);

        if (finalnaPotvr != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String prihodLinija = String.format("%s;%.2f;%s;%s\n", 
                korisnik, konacnaCena, LocalDate.now().toString(), "Izdavanje knjige: " + naslov);
            
            Files.write(Paths.get("podaci/prihodi.csv"),
                    prihodLinija.getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);

            String bibliotekar = UlogovaniKorisnik.getUsername();
            String danasStr = LocalDate.now().toString();
            String izdavanjeLinija = String.format("%s;%s;%s;%s;%s;%s;%.2f;false;%s\n",
                idKnjige, naslov, korisnik, bibliotekar, danasStr, datumKraja.toString(), konacnaCena, rezervacijaId);
            
            Files.write(Paths.get("podaci/izdavanje.csv"),
                    izdavanjeLinija.getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);

            updateBookStatus(idKnjige, StatusKnjige.IZDATA);

            JOptionPane.showMessageDialog(this, 
                String.format("Knjiga uspešno izdata!\n\nNaplaćeno: %.2f RSD\nDatum vraćanja: %s\n\nPrihod i izdavanje zabeleženi.", 
                             konacnaCena, datumKraja),
                "Uspeh", JOptionPane.INFORMATION_MESSAGE);
            
            rezervacijaIzabrana = true;
            dispose();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Greška pri upisu u fajl: " + e.getMessage(),
                "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void updateBookStatus(String bookId, StatusKnjige noviStatus) throws IOException {
        String fajlKnjige = "podaci/knjige.csv";
        List<String> linije = Files.readAllLines(Paths.get(fajlKnjige), StandardCharsets.UTF_8);
        
        for (int i = 0; i < linije.size(); i++) {
            String linija = linije.get(i);
            String[] delovi = linija.split(";");

            if (delovi.length >= 5 && delovi[3].trim().equals(bookId)) {

                delovi[4] = String.valueOf(noviStatus);
                linije.set(i, String.join(";", delovi));
                break;
            }
        }
        
        Files.write(Paths.get(fajlKnjige), linije, StandardCharsets.UTF_8);
    }

    public static void markBookAsReturned(String bookId) throws IOException {
        String fajlIzdavanje = "podaci/izdavanje.csv";
        List<String> linije = Files.readAllLines(Paths.get(fajlIzdavanje), StandardCharsets.UTF_8);
        
        for (int i = 0; i < linije.size(); i++) {
            String linija = linije.get(i);
            String[] delovi = linija.split(";");

            if (delovi.length >= 8) {
                String fileBookId = delovi[0].trim();
                String fileUser = delovi[2].trim();
                String fileIssueDate = delovi[4].trim();
                
                if (fileBookId.trim().equals(bookId.trim()) &&
                	    delovi[7].trim().equalsIgnoreCase("false")) {
                	    
                	    delovi[7] = "true";
                	    linije.set(i, String.join(";", delovi));
                	    updateBookStatusToAvailable(bookId);
                	    break;
                	}

            }
        }
        
        Files.write(Paths.get(fajlIzdavanje), linije, StandardCharsets.UTF_8);
    }

    private static void updateBookStatusToAvailable(String bookId) throws IOException {
        String fajlKnjige = "podaci/knjige.csv";
        List<String> linije = Files.readAllLines(Paths.get(fajlKnjige), StandardCharsets.UTF_8);
        
        for (int i = 0; i < linije.size(); i++) {
            String linija = linije.get(i);
            String[] delovi = linija.split(";");
            
            if (delovi.length >= 5 && delovi[3].trim().equals(bookId)) {

                delovi[4] = "DOSTUPNA";
                linije.set(i, String.join(";", delovi));
                break;
            }
        }
        
        Files.write(Paths.get(fajlKnjige), linije, StandardCharsets.UTF_8);
    }


    public static void processBookReturn(String bookId, String username) throws IOException {

        String fajlIzdavanje = "podaci/izdavanje.csv";
        List<String> linije = Files.readAllLines(Paths.get(fajlIzdavanje), StandardCharsets.UTF_8);
        
        for (int i = 0; i < linije.size(); i++) {
            String linija = linije.get(i);
            String[] delovi = linija.split(";");
            

            if (delovi.length >= 8) {
                String fileBookId = delovi[0].trim();
                String fileUser = delovi[2].trim();
                String returnedStatus = delovi[7].trim();
                

                if (fileBookId.equals(bookId) && 
                    fileUser.equalsIgnoreCase(username) && 
                    "false".equalsIgnoreCase(returnedStatus)) {
                    

                    delovi[7] = "true";
                    linije.set(i, String.join(";", delovi));
                    

                    Files.write(Paths.get(fajlIzdavanje), linije);
                    return;
                }
            }
        }
    }

    public void setIdKnjige(int id) {
        this.idKnjige = id;
        ucitajRezervacije();
    }


    public boolean isRezervacijaIzabrana() {
        return rezervacijaIzabrana;
    }
}