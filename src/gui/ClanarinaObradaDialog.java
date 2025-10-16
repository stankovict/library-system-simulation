package gui;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import enumi.Clanarina;

import manager.CenovnikMetode;
import modeli.Cenovnik;
import modeli.Clan;
import modeli.ClanarinaStavka;
import modeli.ClanskaKarta;

public class ClanarinaObradaDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTable tabela;
    private DefaultTableModel tableModel;

    private String fajlZahteva = "podaci/zahtevi_produzenja.csv";
    private String fajlKarte = "podaci/clanskekarte.csv";
    private String fajlClanovi = "podaci/clanovi.csv";
    private String fajlKategorije = "podaci/posebnekategorije.csv";
    private String fajlPrihodi = "podaci/prihodi.csv";
    private String fajlCenovnici = "podaci/cenovnici.csv";

    public ClanarinaObradaDialog() {
        setTitle("Obrada zahteva za članarinu");
        setBounds(100, 100, 700, 450);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(15,15,15,15));
        getContentPane().add(contentPanel, BorderLayout.CENTER);


        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Napomene"));
        JLabel infoLabel = new JLabel("<html><b>Obrada zahteva:</b> Možete odobriti ili odbaciti zahtev za članarinu.<br/>" +
            "Kod odobravanja se automatski računa popust na osnovu kategorije člana.</html>");
        infoPanel.add(infoLabel);
        contentPanel.add(infoPanel, BorderLayout.NORTH);


        String[] kolone = {"Korisnik", "Datum zahteva", "Vrsta članarine", "Trenutna kategorija"};
        tableModel = new DefaultTableModel(kolone, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        contentPanel.add(new JScrollPane(tabela), BorderLayout.CENTER);


        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnOdobri = new JButton("Odobri zahtev");
        btnOdobri.setBackground(new Color(76, 175, 80));
        btnOdobri.setForeground(Color.WHITE);
        btnOdobri.setPreferredSize(new Dimension(130, 30));
        btnOdobri.addActionListener(e -> odobriZahtev());
        
        JButton btnOdbaci = new JButton("Odbaci zahtev");
        btnOdbaci.setBackground(new Color(244, 67, 54));
        btnOdbaci.setForeground(Color.WHITE);
        btnOdbaci.setPreferredSize(new Dimension(130, 30));
        btnOdbaci.addActionListener(e -> odbaciZahtev());
        
        JButton btnZatvori = new JButton("Zatvori");
        btnZatvori.setPreferredSize(new Dimension(100, 30));
        btnZatvori.addActionListener(e -> dispose());
        
        buttonPane.add(btnOdobri);
        buttonPane.add(btnOdbaci);
        buttonPane.add(btnZatvori);
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        ucitajZahteve();
        setLocationRelativeTo(null);
    }

    private void ucitajZahteve() {
        tableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlZahteva), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] delovi = line.split(";");
                if (delovi.length == 3) {
                    String username = delovi[0];
                    String datum = delovi[1];
                    String vrsta = delovi[2];
                    

                    String kategorija = pronadjiKategorijuClana(username);
                    
                    tableModel.addRow(new Object[]{username, datum, vrsta, kategorija});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Greška pri učitavanju zahteva: " + e.getMessage());
        }
    }

    private String pronadjiKategorijuClana(String username) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlClanovi), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] delovi = line.split(";");
                if (delovi.length >= 10) {
                    String email = delovi[6].trim();
                    if (email.equalsIgnoreCase(username)) {
                        return delovi[8].trim();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "NEPOZNATO";
    }

    private void odbaciZahtev() {
        int row = tabela.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Izaberite zahtev iz tabele.");
            return;
        }

        String korisnik = tableModel.getValueAt(row, 0).toString();
        String vrsta = tableModel.getValueAt(row, 2).toString();

        int result = JOptionPane.showConfirmDialog(this,
            "Da li ste sigurni da želite da odbacite zahtev za " + vrsta.toLowerCase() + 
            " članarinu korisnika: " + korisnik + "?",
            "Potvrda odbacivanja",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (result != JOptionPane.YES_OPTION) return;


        ukloniZahtev(korisnik);

        JOptionPane.showMessageDialog(this, "Zahtev je odbačen. Korisnik neće dobiti članarinu.");
        ucitajZahteve();
    }

    private void odobriZahtev() {
        int row = tabela.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Izaberite zahtev iz tabele.");
            return;
        }

        String korisnik = tableModel.getValueAt(row, 0).toString();
        String vrstaStr = tableModel.getValueAt(row, 2).toString();
        String kategorija = tableModel.getValueAt(row, 3).toString();
        
        try {
            Clanarina vrsta = Clanarina.valueOf(vrstaStr);
            LocalDate danas = LocalDate.now();
            LocalDate datumKraja;

            if (vrsta == Clanarina.mesečna) {
                datumKraja = danas.plusMonths(1);
            } else {
                datumKraja = danas.plusYears(1);
            }


            double popust = dobijPopustZaKategoriju(kategorija);            

            double osnovnaCena = dobijCenuClanarine(vrsta);

            double finalnaCena = osnovnaCena * popust;
            
            azurirajClanskaKarta(korisnik, vrsta, danas, datumKraja);

            azurirajClanovuClanarinu(korisnik, vrsta);

            dodajUPrihode(korisnik, vrsta, finalnaCena, danas);

            ukloniZahtev(korisnik);

            JOptionPane.showMessageDialog(this, 
                String.format("Zahtev odobren!\n\n" +
                    "Korisnik: %s\n" +
                    "Članarina: %s\n" +
                    "Kategorija: %s (popust: %.0f%%)\n" +
                    "Osnovna cena: %.2f RSD\n" +
                    "Finalna cena: %.2f RSD\n" +
                    "Važi do: %s",
                    korisnik, vrsta, kategorija, (1-popust)*100, 
                    osnovnaCena, finalnaCena, datumKraja),
                "Uspeh", JOptionPane.INFORMATION_MESSAGE);
            
            ucitajZahteve();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Greška pri obradi zahteva: " + e.getMessage(), 
                "Greška", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private double dobijPopustZaKategoriju(String kategorija) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlKategorije), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] delovi = line.split(";");
                if (delovi.length == 2) {
                    String kat = delovi[0].trim();
                    if (kat.equalsIgnoreCase(kategorija)) {
                        return Double.parseDouble(delovi[1].trim());
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 1.0;
    }

    private double dobijCenuClanarine(Clanarina vrsta) {
        try {

            List<Cenovnik> cenovnici = CenovnikMetode.ucitajCenovnike(fajlCenovnici);
            LocalDate danas = LocalDate.now();
            
            Cenovnik trenutni = null;
            for (Cenovnik c : cenovnici) {
                if (!danas.isBefore(c.getDatumOd()) && !danas.isAfter(c.getDatumDo())) {
                    trenutni = c;
                    break;
                }
            }
            
            if (trenutni == null) {
                throw new RuntimeException("Nema aktivnog cenovnika za današnji datum!");
            }

            for (ClanarinaStavka cs : trenutni.getClanarine()) {
                if (cs.getTip().equals(vrsta)) {
                    return cs.getCena();
                }
            }
            
            throw new RuntimeException("Cena za " + vrsta + " članarinu nije pronađena u cenovniku!");
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Greška pri dobijanju cene članarine: " + e.getMessage());
        }
    }

    private void azurirajClanskaKarta(String korisnik, Clanarina vrsta, LocalDate datumUplate, LocalDate datumIsteka) {
        List<ClanskaKarta> karte = ucitajKarte();

        boolean nadjena = false;
        for (ClanskaKarta k : karte) {
            if (k.getKorisnickoIme().equalsIgnoreCase(korisnik)) {
                k.setTip(vrsta);
                k.setDatumUplate(datumUplate);
                k.setDatumIsteka(datumIsteka);
                nadjena = true;
                break;
            }
        }

        if (!nadjena) {
            ClanskaKarta nova = new ClanskaKarta(korisnik, vrsta, datumUplate);
            nova.setDatumIsteka(datumIsteka);
            karte.add(nova);
        }

        sacuvajKarte(karte);
    }

    private void azurirajClanovuClanarinu(String korisnik, Clanarina novaVrsta) {
        List<String> linije = new ArrayList<>();
        boolean azurirano = false;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlClanovi), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    linije.add(line);
                    continue;
                }
                
                String[] delovi = line.split(";");
                if (delovi.length >= 10) {
                    String email = delovi[6].trim();
                    if (email.equalsIgnoreCase(korisnik)) {
                        delovi[9] = novaVrsta.toString();
                        line = String.join(";", delovi);
                        azurirano = true;
                    }
                }
                linije.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (azurirano) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fajlClanovi), StandardCharsets.UTF_8))) {
                for (String linija : linije) {
                    bw.write(linija);
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dodajUPrihode(String korisnik, Clanarina vrsta, double cena, LocalDate datum) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fajlPrihodi, true), StandardCharsets.UTF_8))) {
            bw.write(korisnik + ";" + String.format("%.2f", cena) + ";" + datum);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<ClanskaKarta> ucitajKarte() {
        List<ClanskaKarta> karte = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlKarte), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] delovi = line.split(";");
                if (delovi.length == 4) {
                    String username = delovi[0];
                    Clanarina tip = Clanarina.valueOf(delovi[1]);

                    LocalDate datumUplate = null;
                    LocalDate datumIsteka = null;
                    String[] patterns = {"d.M.yyyy", "dd.MM.yyyy", "yyyy-MM-dd"};

                    for (String p : patterns) {
                        try {
                            DateTimeFormatter f = DateTimeFormatter.ofPattern(p);
                            datumUplate = LocalDate.parse(delovi[2].trim(), f);
                            datumIsteka = LocalDate.parse(delovi[3].trim(), f);
                            break;
                        } catch (Exception ignored) {}
                    }

                    if (datumUplate == null || datumIsteka == null) {
                        throw new IllegalArgumentException("Nepoznat format datuma: " + line);
                    }

                    ClanskaKarta k = new ClanskaKarta(username, tip, datumUplate);
                    k.setDatumIsteka(datumIsteka);
                    karte.add(k);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return karte;
    }


    private void sacuvajKarte(List<ClanskaKarta> karte) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fajlKarte), StandardCharsets.UTF_8))) {
            for (ClanskaKarta k : karte) {
                bw.write(k.getKorisnickoIme() + ";" + k.getTipClanarine() + ";" + 
                        k.getDatumUplate() + ";" + k.getDatumIsteka());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ukloniZahtev(String korisnik) {
        List<String> linije = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlZahteva), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(korisnik + ";")) {
                    linije.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fajlZahteva), StandardCharsets.UTF_8))) {
            for (String l : linije) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}