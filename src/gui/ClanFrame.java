package gui;

import javax.swing.*;

import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import manager.ClanMetode;
import manager.DugMetode;
import manager.UlogovaniKorisnik;
import manager.RezervacijaMetode;
import modeli.Clan;
import modeli.Rezervacija;
import modeli.Usluga;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClanFrame extends JFrame {

    private JLabel lblDug;

    public ClanFrame() {
        initializeFrame();
        createComponents();
        osveziDug();
    }

    private void initializeFrame() {
        setTitle("Član panel");
        setSize(485, 322);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);
    }

    private void createComponents() {

        JLabel lblUlogovani = new JLabel("Ulogovani ČLAN: " + UlogovaniKorisnik.getUsername());
        lblUlogovani.setHorizontalAlignment(SwingConstants.CENTER);
        lblUlogovani.setBounds(27, 20, 400, 30);
        getContentPane().add(lblUlogovani);

        lblDug = new JLabel();
        lblDug.setHorizontalAlignment(SwingConstants.CENTER);
        lblDug.setBounds(37, 61, 400, 30);
        getContentPane().add(lblDug);

        JButton btnOtplatiDug = new JButton("Otplati dug");
        btnOtplatiDug.setBounds(144, 155, 180, 30);
        btnOtplatiDug.addActionListener(e -> otplatiDug());
        getContentPane().add(btnOtplatiDug);

        JButton btnPrikaziRez = new JButton("Prikaži rezervacije");
        btnPrikaziRez.setBounds(240, 110, 180, 30);
        btnPrikaziRez.addActionListener(e -> prikaziRezervacije());
        getContentPane().add(btnPrikaziRez);

        JButton btnRezervacija = new JButton("Kreiraj rezervaciju");
        btnRezervacija.setBounds(50, 110, 180, 30);
        btnRezervacija.addActionListener(e -> kreirajRezervaciju());
        getContentPane().add(btnRezervacija);

        JButton btnOdjava = new JButton("Odjava");
        btnOdjava.setBounds(144, 196, 180, 30);
        btnOdjava.addActionListener(e -> {
            UlogovaniKorisnik.setUsername(null);
            dispose();
            new MainFrame().setVisible(true);
        });
        getContentPane().add(btnOdjava);
    }

    private void prikaziRezervacije() {
        String username = UlogovaniKorisnik.getUsername();

        proveraAutomatskogOtkazivanja(username);

        List<Rezervacija> rezervacije = RezervacijaMetode.prikaziRezervacijeZaClana(username);

        JDialog dialog = new JDialog(this, "Rezervacije za korisnika: " + username, true);
        dialog.setSize(900, 550);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setLayout(new BorderLayout());

        JPanel infoPanel = createInfoPanel();
        dialog.getContentPane().add(infoPanel, BorderLayout.NORTH);

        DefaultTableModel model = createReservationTableModel();
        populateReservationTable(model, rezervacije);

        JTable tabela = new JTable(model);
        setupReservationTable(tabela, dialog, rezervacije, model, username);

        JScrollPane scrollPane = new JScrollPane(tabela);

        JPanel buttonPanel = createButtonPanel(dialog);

        dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Napomene"));
        JLabel infoLabel = new JLabel("<html><b>Otkazivanje:</b> Možete otkazati samo POTVRĐENE rezervacije. " +
            "Nakon otkazivanja nećete moći da pravite nove rezervacije 24 sata.<br/>" +
            "<b>Automatsko otkazivanje:</b> Rezervacije se automatski otkazuju ako ne preuzmete knjigu prvi dan.</html>");
        infoPanel.add(infoLabel);
        return infoPanel;
    }

    private DefaultTableModel createReservationTableModel() {
        String[] kolone = {"ID", "Knjiga", "Datum početka", "Datum kraja", "Cena", "Status", "Bibliotekar", "Usluge"};
        return new DefaultTableModel(kolone, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 5) {
                    String trenutniStatus = getValueAt(row, 5).toString();
                    return "POTVRĐENA".equals(trenutniStatus);
                }
                return false;
            }
        };
    }

    private void populateReservationTable(DefaultTableModel model, List<Rezervacija> rezervacije) {
        for (Rezervacija r : rezervacije) {
            String bibliotekarUsername = "";
            if (r.getBibliotekar() != null) {
                bibliotekarUsername = r.getBibliotekar().getUsername();
            }
            
            List<Usluga> usluge = r.getUsluge();
            String uslugeStr = "";
            if (usluge != null && !usluge.isEmpty()) {
                uslugeStr = usluge.stream()
                    .map(Usluga::getNaziv)
                    .reduce((u1, u2) -> u1 + ", " + u2)
                    .orElse("");
            }
            
            model.addRow(new Object[]{
                r.getIdRez(),
                r.getKnjiga().getNaslov(),
                r.getDatumPocetka(),
                r.getDatumKraja(),
                r.getCena(),
                r.getStatus(),
                bibliotekarUsername,
                uslugeStr
            });
        }
    }

    private void setupReservationTable(JTable tabela, JDialog dialog, List<Rezervacija> rezervacije, 
                                     DefaultTableModel model, String username) {

        String[] statusOpcije = {"POTVRĐENA", "OTKAZANA"};
        JComboBox<String> statusComboBox = new JComboBox<>(statusOpcije);
        tabela.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(statusComboBox));

        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                handleTableChange(e, dialog, rezervacije, model, username);
            }
        });
    }

    private void handleTableChange(TableModelEvent e, JDialog dialog, List<Rezervacija> rezervacije,
                                 DefaultTableModel model, String username) {
        if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 5) {
            int row = e.getFirstRow();
            String noviStatus = model.getValueAt(row, 5).toString();
            String stariStatus = rezervacije.get(row).getStatus().toString();
            
            if ("POTVRĐENA".equals(stariStatus) && "OTKAZANA".equals(noviStatus)) {
                int rezervacijaId = (Integer) model.getValueAt(row, 0);
                String naslovKnjige = model.getValueAt(row, 1).toString();
                
                if (confirmCancellation(dialog, naslovKnjige, rezervacijaId)) {
                    if (otkaziRezervaciju(rezervacijaId, username)) {
                        dodajPrivremeniBan(username);
                        showCancellationSuccess(dialog);
                        dialog.dispose();
                        prikaziRezervacije();
                    } else {
                        model.setValueAt(stariStatus, row, 5);
                        showError(dialog, "Greška pri otkazivanju rezervacije!");
                    }
                } else {
                    model.setValueAt(stariStatus, row, 5);
                }
            }
        }
    }

    private boolean confirmCancellation(JDialog dialog, String naslovKnjige, int rezervacijaId) {
        int result = JOptionPane.showConfirmDialog(dialog,
            "Da li ste sigurni da želite da otkažete rezervaciju?\n\n" +
            "Knjiga: " + naslovKnjige + "\n" +
            "ID rezervacije: " + rezervacijaId + "\n\n" +
            "NAPOMENA: Nakon otkazivanja nećete moći da pravite nove rezervacije 24 sata!",
            "Potvrda otkazivanja",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    private void showCancellationSuccess(JDialog dialog) {
        JOptionPane.showMessageDialog(dialog,
            "Rezervacija je uspešno otkazana.\n\n" +
            "VAŽNO: Privremeno ste blokirani od pravljenja novih rezervacija do sutra u isto vreme.",
            "Rezervacija otkazana",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(JDialog dialog, String message) {
        JOptionPane.showMessageDialog(dialog, message, "Greška", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel createButtonPanel(JDialog dialog) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnZatvori = new JButton("Zatvori");
        btnZatvori.addActionListener(ev -> dialog.dispose());
        buttonPanel.add(btnZatvori);
        return buttonPanel;
    }

    public void proveraAutomatskogOtkazivanja(String username) {
        LocalDate danas = LocalDate.now();
        String fajlRezervacije = "podaci/rezervacije.csv";
        String fajlIzdavanje = "podaci/izdavanje.csv";
        String fajlObradjene = "podaci/rezervacije_obradjene.csv";
        
        List<String> linijeSaPromenom = new ArrayList<>();
        List<Integer> otkazaneRezervacije = new ArrayList<>();
        boolean imaPromena = false;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlRezervacije), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length > 6) {
                    try {
                        int rezervacijaId = Integer.parseInt(delovi[0].trim());
                        String korisnik = delovi[1].trim();
                        
                        LocalDate datumPocetka = null;
                        String[] patterns = {"d.M.yyyy", "dd.MM.yyyy", "yyyy-MM-dd"};
                        for (String p : patterns) {
                            try {
                                java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                                datumPocetka = LocalDate.parse(delovi[4].trim(), f);
                                break;
                            } catch (Exception ignored) {}
                        }

                        String status = delovi[6].trim();

                        if (korisnik.equalsIgnoreCase(username) && "POTVRĐENA".equalsIgnoreCase(status)) {

                            if (datumPocetka.isBefore(danas)) {

                                if (!daLiJeKorisnikPreuzeoKnjigu(username, datumPocetka, fajlIzdavanje)) {

                                    delovi[6] = "OTKAZANA";
                                    if (delovi.length > 8) {
                                        delovi[8] = "placeholder";
                                    }
                                    otkazaneRezervacije.add(rezervacijaId);
                                    imaPromena = true;
                                }
                            }
                        }
                    } catch (Exception e) {

                    }
                }
                linijeSaPromenom.add(String.join(";", delovi));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (imaPromena) {
            saveUpdatedReservations(linijeSaPromenom, fajlRezervacije);
            updateProcessedReservations(otkazaneRezervacije, username, fajlObradjene);
            dodajPrivremeniBan(username);
            showAutoCancellationMessage(otkazaneRezervacije);
        }
    }

    private void saveUpdatedReservations(List<String> linije, String fajlRezervacije) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fajlRezervacije, false), StandardCharsets.UTF_8))) {
            for (String linija : linije) {
                bw.write(linija);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProcessedReservations(List<Integer> otkazaneRezervacije, String username, String fajlObradjene) {
        for (Integer rezervacijaId : otkazaneRezervacije) {
            azurirajObradjeneRezervacije(rezervacijaId, username, fajlObradjene);
        }
    }

    private void showAutoCancellationMessage(List<Integer> otkazaneRezervacije) {
        if (otkazaneRezervacije.size() == 1) {
            JOptionPane.showMessageDialog(this,
                "Automatsko otkazivanje rezervacije!\n\n" +
                "Vaša rezervacija (ID: " + otkazaneRezervacije.get(0) + ") je automatski otkazana\n" +
                "jer niste preuzeli knjigu prvi dan rezervacije.\n\n" +
                "VAŽNO: Privremeno ste blokirani od pravljenja novih rezervacija 24 sata.",
                "Automatsko otkazivanje",
                JOptionPane.WARNING_MESSAGE);
        } else if (otkazaneRezervacije.size() > 1) {
            StringBuilder poruka = new StringBuilder();
            poruka.append("Automatsko otkazivanje rezervacija!\n\n");
            poruka.append("Sledeće rezervacije su automatski otkazane jer niste\n");
            poruka.append("preuzeli knjige prvi dan rezervacije:\n\n");
            for (Integer id : otkazaneRezervacije) {
                poruka.append("• Rezervacija ID: ").append(id).append("\n");
            }
            poruka.append("\nVAŽNO: Privremeno ste blokirani od pravljenja novih rezervacija 24 sata.");
            
            JOptionPane.showMessageDialog(this, poruka.toString(),
                "Automatsko otkazivanje", JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean daLiJeKorisnikPreuzeoKnjigu(String username, LocalDate datumRezervacije, String fajlIzdavanje) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlIzdavanje), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length >= 6) {
                    String korisnik = delovi[2].trim();
                    try {
                        LocalDate datumIzdavanja = LocalDate.parse(delovi[4].trim());

                        if (korisnik.equalsIgnoreCase(username) && 
                           (datumIzdavanja.equals(datumRezervacije) )) {
                            return true;
                        }
                    } catch (Exception e) {

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void kreirajRezervaciju() {
        String username = UlogovaniKorisnik.getUsername();

        if (isBanned(username)) {
            JOptionPane.showMessageDialog(this,
                "Privremeno ste blokirani od pravljenja rezervacija!\n\n" +
                "Razlog: Otkazali ste rezervaciju u poslednja 24 sata.\n" +
                "Molimo pokušajte ponovo kasnije.",
                "Privremena zabrana",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Clan clan = ClanMetode.nadjiClanaPoUsername(UlogovaniKorisnik.getUsername());
        NoviZahtevDialog dialog = new NoviZahtevDialog(this, clan);
        dialog.setVisible(true);
    }

    private boolean otkaziRezervaciju(int rezervacijaId, String username) {
        List<String> linije = new ArrayList<>();
        boolean otkazano = false;
        String fajlRezervacije = "podaci/rezervacije.csv";
        String fajlObradjene = "podaci/rezervacije_obradjene.csv";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlRezervacije), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length > 0) {
                    try {
                        int id = Integer.parseInt(delovi[0].trim());
                        if (id == rezervacijaId && delovi[1].trim().equalsIgnoreCase(username)) {
                            delovi[6] = "OTKAZANA";
                            if (delovi.length > 8) {
                                delovi[8] = "placeholder";
                            }
                            line = String.join(";", delovi);
                            otkazano = true;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
                linije.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (otkazano) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fajlRezervacije, false), StandardCharsets.UTF_8))) {
                for (String linija : linije) {
                    bw.write(linija);
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            azurirajObradjeneRezervacije(rezervacijaId, username, fajlObradjene);
        }

        return otkazano;
    }

    private void azurirajObradjeneRezervacije(int rezervacijaId, String username, String fajlObradjene) {
        List<String> obradjeneLinije = new ArrayList<>();
        boolean azurirano = false;
        LocalDate danas = LocalDate.now();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlObradjene), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length > 0) {
                    try {
                        int id = Integer.parseInt(delovi[0].trim());
                        if (id == rezervacijaId && delovi[1].trim().equalsIgnoreCase(username)) {
                            delovi[6] = "OTKAZANA";
                            if (delovi.length > 8) {
                                delovi[8] = "placeholder";
                            }

                            if (delovi.length > 9) {
                                delovi[delovi.length - 1] = danas.toString();
                            } else {
                                line = String.join(";", delovi) + ";" + danas.toString();
                                obradjeneLinije.add(line);
                                azurirano = true;
                                continue;
                            }
                            line = String.join(";", delovi);
                            azurirano = true;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
                obradjeneLinije.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (azurirano) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fajlObradjene, false), StandardCharsets.UTF_8))) {
                for (String linija : obradjeneLinije) {
                    bw.write(linija);
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dodajPrivremeniBan(String username) {
        String fajlBanovi = "podaci/privremeni_banovi.csv";
        LocalDateTime banDo = LocalDateTime.now().plusHours(24);
        
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fajlBanovi, true), StandardCharsets.UTF_8))) {
            bw.write(username + ";" + banDo.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isBanned(String username) {
        String fajlBanovi = "podaci/privremeni_banovi.csv";
        LocalDateTime sada = LocalDateTime.now();
        List<String> aktivniBanovi = new ArrayList<>();
        boolean korisnikBanovan = false;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlBanovi), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] delovi = line.split(";");
                if (delovi.length == 2) {
                    String banUsername = delovi[0].trim();
                    LocalDateTime banDo = LocalDateTime.parse(delovi[1].trim());
                    
                    if (sada.isBefore(banDo)) {
                        aktivniBanovi.add(line);
                        if (banUsername.equalsIgnoreCase(username)) {
                            korisnikBanovan = true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fajlBanovi, false), StandardCharsets.UTF_8))) {
            for (String aktivniBan : aktivniBanovi) {
                bw.write(aktivniBan);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return korisnikBanovan;
    }

    private void osveziDug() {
        double dug = DugMetode.ucitajDug(UlogovaniKorisnik.getUsername());
        lblDug.setText("Vaš trenutni dug je " + dug + " RSD");
    }

    private void otplatiDug() {
        String iznosStr = JOptionPane.showInputDialog(this, "Unesite iznos za otplatu:");
        if (iznosStr == null) return;
        
        double iznos;
        try {
            iznos = Double.parseDouble(iznosStr);
            if (iznos <= 0) {
                JOptionPane.showMessageDialog(this, "Neispravan iznos!");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Neispravan iznos!");
            return;
        }

        DugMetode.otplatiDug(UlogovaniKorisnik.getUsername(), iznos);
        JOptionPane.showMessageDialog(this, "Uplata uspešna!");
        osveziDug();
    }
}