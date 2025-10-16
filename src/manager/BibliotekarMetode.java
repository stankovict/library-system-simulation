package manager;

import java.io.OutputStreamWriter;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import enumi.StrucnaSprema;
import modeli.Administrator;
import modeli.Bibliotekar;
import modeli.Clan;
import modeli.Knjiga;
import modeli.Rezervacija;
import modeli.Usluga;
import enumi.Clanarina;
import enumi.Pol;

import enumi.StatusKnjige;
import enumi.StatusRezervacije;

public class BibliotekarMetode {

	public static void dodajBibliotekara(String fajl,
            String ime,
            String prezime,
            Pol pol,
            LocalDate datumRodjenja,
            String telefon,
            String adresa,
            String korisnickoIme,
            String lozinka,
            StrucnaSprema strucnaSprema,
            int godineStaza) {

	Bibliotekar bibliotekar = new Bibliotekar(
	ime, prezime, pol, datumRodjenja, telefon, adresa,
	korisnickoIme, lozinka, strucnaSprema, godineStaza
	);
	
	bibliotekar.obracunajPlatu();
	
	try (OutputStreamWriter writer = new OutputStreamWriter(
	new FileOutputStream(fajl, true), StandardCharsets.UTF_8)) {
	writer.write(bibliotekar.toCSV());
	writer.write(System.lineSeparator());
	System.out.println("Administrator uspešno dodat i upisan u fajl");
	} catch (IOException e) {
	System.out.println("Greška pri upisu u fajl: " + e.getMessage());
	}
	}
    
    public static void obrisiBibliotekara(String fajl) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Unesite korisničko ime bibliotekara za brisanje: ");
        String korisnickoIme = sc.nextLine();

        List<String> linije = new ArrayList<>();
        boolean obrisan = false;


        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajl), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                

                String[] delovi = line.split(";");
                if (delovi.length > 6) {
                    String korisnikIzFajla = delovi[6].trim(); 
                    if (korisnikIzFajla.equalsIgnoreCase(korisnickoIme.trim())) {
                        obrisan = true;
                        continue;
                    }
                }
                linije.add(line);
            }
        } catch (IOException e) {
            System.out.println("Greška pri čitanju fajla: " + e.getMessage());
            return;
        }


        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fajl, false), StandardCharsets.UTF_8))) {
            for (String l : linije) {
                bw.write(l);
                bw.newLine();
            }

            if (obrisan) {
                System.out.println("Bibliotekar sa korisničkim imenom '" + korisnickoIme + "' je uspešno obrisan.");
            } else {
                System.out.println("Nema administratora sa korisničkim imenom '" + korisnickoIme + "'.");
            }

        } catch (IOException e) {
            System.out.println("Greška pri upisu u fajl: " + e.getMessage());
        }
    }
    
    public static void obradiRezervacijeGUI(JFrame parent) {
        String fajlRez = "podaci/rezervacije.csv";
        String fajlObradjene = "podaci/rezervacije_obradjene.csv";
        String fajlKnjige = "podaci/knjige.csv";

        List<Rezervacija> sveRezervacije = RezervacijaMetode.ucitajRezervacije(fajlRez);
        if (sveRezervacije.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Nema rezervacija za obradu.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] kolone = {"ID", "Korisnik", "Knjiga", "Datum početka", "Datum kraja", "Cena", "Status", "Bibliotekar","Usluge"};
        Object[][] podaci = new Object[sveRezervacije.size()][kolone.length];

        for (int i = 0; i < sveRezervacije.size(); i++) {
            Rezervacija r = sveRezervacije.get(i);
            podaci[i][0] = r.getIdRez();
            podaci[i][1] = r.getClan().getUsername();
            podaci[i][2] = r.getKnjiga().getNaslov();
            podaci[i][3] = r.getDatumPocetka();
            podaci[i][4] = r.getDatumKraja();
            podaci[i][5] = r.getCena();
            podaci[i][6] = r.getStatus();
            podaci[i][7] = r.getBibliotekar() != null ? r.getBibliotekar().getUsername() : "";

            List<Usluga> usluge = r.getUsluge();
            String uslugeStr = "";
            if (usluge != null && !usluge.isEmpty()) {
                uslugeStr = usluge.stream()
                                  .map(u -> u.getNaziv())
                                  .reduce((u1, u2) -> u1 + ", " + u2)
                                  .orElse("");
            }
            podaci[i][8] = uslugeStr;
        }

        DefaultTableModel model = new DefaultTableModel(podaci, kolone) {
            @Override
            public boolean isCellEditable(int row, int column) {

                if (column == 6) {
                    Object value = getValueAt(row, column);
                    if (value == StatusRezervacije.NA_ČEKANJU) { 
                        return true;
                    }
                }
                return false;
            }
        };

        JTable tabela = new JTable(model);
        JComboBox<StatusRezervacije> comboBox = new JComboBox<>(
                new StatusRezervacije[]{StatusRezervacije.POTVRĐENA, StatusRezervacije.ODBIJENA}
        );

        tabela.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(comboBox));

        JScrollPane scrollPane = new JScrollPane(tabela);

        JButton btnSacuvaj = new JButton("Sačuvaj obrade");


        btnSacuvaj.addActionListener(e -> {
            String bibliotekar = UlogovaniKorisnik.getUsername();
            LocalDate danas = LocalDate.now();

            List<String> nedostupneKnjige = new ArrayList<>();
            List<String> prebaceneKnjige = new ArrayList<>();
            
            for (int i = 0; i < model.getRowCount(); i++) {
                String noviStatus = model.getValueAt(i, 6).toString();

                if ("POTVRĐENA".equals(noviStatus)) {
                    Rezervacija r = sveRezervacije.get(i);
                    String stariStatus = r.getStatus().toString();

                    if (!"POTVRĐENA".equals(stariStatus)) {
                        int originalniId = r.getKnjiga().getId();

                        if (!proveriIDodelKnjigu(r, fajlKnjige, fajlRez)) {
                            nedostupneKnjige.add(String.format("'%s' (ID: %d) - period %s do %s - rezervacija #%s", 
                                r.getKnjiga().getNaslov(), originalniId, r.getDatumPocetka(), r.getDatumKraja(), r.getIdRez()));
                        } else {

                            if (r.getKnjiga().getId() != originalniId) {
                                prebaceneKnjige.add(String.format("Rezervacija #%s: '%s' prebačena sa kopije ID %d na kopiju ID %d", 
                                    r.getIdRez(), r.getKnjiga().getNaslov(), originalniId, r.getKnjiga().getId()));
                            }
                        }
                    }
                }
            }

            if (!nedostupneKnjige.isEmpty()) {
                StringBuilder poruka = new StringBuilder();
                poruka.append("❌ Sledeće rezervacije ne mogu biti potvrđene zbog nedostupnosti knjiga:\n\n");
                for (String knjiga : nedostupneKnjige) {
                    poruka.append("• ").append(knjiga).append("\n");
                }
                poruka.append("\nRazlozi nedostupnosti:\n");
                poruka.append("- Nema dovoljno kopija knjige u biblioteci\n");
                poruka.append("- Sve kopije su već rezervisane u tom periodu\n");
                poruka.append("- Knjiga je trenutno izneta iz biblioteke\n\n");
                poruka.append("Molimo proverite dostupnost ili odložite potvrdu rezervacije.");
                
                JOptionPane.showMessageDialog(parent, poruka.toString(), 
                    "Nedostupne knjige", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!prebaceneKnjige.isEmpty()) {
                StringBuilder poruka = new StringBuilder();
                poruka.append("Automatski su dodeljene alternative kopije za sledeće rezervacije:\n\n");
                for (String info : prebaceneKnjige) {
                    poruka.append("• ").append(info).append("\n");
                }
                poruka.append("\nDa li želite da nastavite sa čuvanjem?");
                
                int rezultat = JOptionPane.showConfirmDialog(parent, poruka.toString(), 
                    "Alternative kopije dodeljene", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                
                if (rezultat != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try (
                BufferedWriter bwRez = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fajlRez, false), StandardCharsets.UTF_8));
                BufferedWriter bwObradjene = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fajlObradjene, true), StandardCharsets.UTF_8))
            ) {
                int potvrdjeneRezervacije = 0;
                
                for (int i = 0; i < model.getRowCount(); i++) {
                    Rezervacija r = sveRezervacije.get(i);

                    String statusNovo = model.getValueAt(i, 6).toString();
                    StatusRezervacije stariStatus = r.getStatus();

                    if (!statusNovo.equals(stariStatus.toString())) {

                        r.setStatus(StatusRezervacije.valueOf(statusNovo));

                        if ((statusNovo.equals("POTVRĐENA") || statusNovo.equals("ODBIJENA")) &&
                            (r.getBibliotekar() == null || "placeholder".equalsIgnoreCase(r.getBibliotekar().getUsername()))) {

                            Bibliotekar b = new Bibliotekar("placeholder", "", Pol.DRUGO, LocalDate.now(), "", "", "placeholder", "", StrucnaSprema.III, 1);
                            b.setUsername(bibliotekar);
                            r.setBibliotekar(b);
                        }

                        bwRez.write(r.toCSV());
                        bwRez.newLine();

                        bwObradjene.write(r.toCSV() + ";" + danas);
                        bwObradjene.newLine();

                    } else {

                        bwRez.write(r.toCSV());
                        bwRez.newLine();
                    }
                }


                StringBuilder poruka = new StringBuilder("Rezervacije uspešno obrađene!");
                if (potvrdjeneRezervacije > 0) {
                    poruka.append("\n\nPotvrđeno rezervacija: ").append(potvrdjeneRezervacije);
                }
                if (!prebaceneKnjige.isEmpty()) {
                    poruka.append("\nAlternative kopije dodeljene: ").append(prebaceneKnjige.size());
                }
                
                JOptionPane.showMessageDialog(parent, poruka.toString(), "Uspeh", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Greška pri upisu: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
            }
        });

        JDialog dialog = new JDialog(parent, "Obrada rezervacija", true);
        dialog.setSize(1000, 500);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(parent);

        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Napomene"));
        JLabel infoLabel = new JLabel("<html><b>Važno:</b> Sistem automatski proverava dostupnost knjiga pre potvrde rezervacije.<br/>" +
            "Rezervacija može biti potvrđena samo ako postoji slobodna kopija knjige u traženom periodu.</html>");
        infoPanel.add(infoLabel);

        dialog.add(infoPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(btnSacuvaj, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }


    private static boolean proveriDostupnostKnjige(String naslovKnjige, LocalDate datumOd, LocalDate datumDo, 
                                                 int trenutnaRezId, String fajlKnjige, String fajlRezervacije) {
        try {

            List<Integer> sveKopijeKnjige = new ArrayList<>();
            
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fajlKnjige), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] delovi = line.split(";");

                    if (delovi.length >= 2) {
                        String naslov = delovi[1].trim();
                        if (naslov.equalsIgnoreCase(naslovKnjige)) {
                            try {
                                int idKnjige = Integer.parseInt(delovi[0].trim());
                                sveKopijeKnjige.add(idKnjige);
                            } catch (NumberFormatException e) {

                            }
                        }
                    }
                }
            }

            if (sveKopijeKnjige.isEmpty()) {
                return false;
            }

            Set<Integer> zauzeteKopije = new HashSet<>();
            
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fajlRezervacije), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] delovi = line.split(";");

                    if (delovi.length >= 7) {
                        try {
                            int rezervacijaId = Integer.parseInt(delovi[0].trim());
                            int knjigeId = Integer.parseInt(delovi[2].trim());
                            String naslov = delovi[3].trim();
                            LocalDate rezDatumOd = LocalDate.parse(delovi[4].trim());
                            LocalDate rezDatumDo = LocalDate.parse(delovi[5].trim());
                            String status = delovi[6].trim();

                            if (rezervacijaId == trenutnaRezId || !"POTVRĐENA".equalsIgnoreCase(status)) {
                                continue;
                            }

                            if (naslov.equalsIgnoreCase(naslovKnjige)) {

                                boolean overlap = !(datumDo.isBefore(rezDatumOd) || datumOd.isAfter(rezDatumDo));
                                if (overlap) {
                                    zauzeteKopije.add(knjigeId);
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }

            for (Integer kopijaId : sveKopijeKnjige) {
                if (!zauzeteKopije.contains(kopijaId)) {
                    return true;
                }
            }

            return false;

        } catch (IOException e) {

            return false;
        }
    }
    
    /**
     * Proverava da li je knjiga dostupna u datom periodu i automatski dodeljuje dostupnu kopiju
     * @param rezervacija Rezervacija koja se potvrđuje
     * @param fajlKnjige Putanja do fajla sa knjigama
     * @param fajlRezervacije Putanja do fajla sa rezervacijama
     * @return true ako je neka kopija knjige dostupna, false inače
     */
    private static boolean proveriIDodelKnjigu(Rezervacija rezervacija, String fajlKnjige, String fajlRezervacije) {
        try {
            int originalnoKnjigeId = rezervacija.getKnjiga().getId();
            String naslovKnjige = rezervacija.getKnjiga().getNaslov();
            LocalDate datumOd = rezervacija.getDatumPocetka();
            LocalDate datumDo = rezervacija.getDatumKraja();
            int trenutnaRezId = rezervacija.getIdRez();

           
            if (proveritDostupnostKopije(originalnoKnjigeId, datumOd, datumDo, trenutnaRezId, fajlKnjige, fajlRezervacije)) {

                return true;
            }

            List<Integer> alternativeKopije = nadjiAlternativeKopije(naslovKnjige, originalnoKnjigeId, fajlKnjige);
            
            for (Integer alternativaId : alternativeKopije) {
                if (proveritDostupnostKopije(alternativaId, datumOd, datumDo, trenutnaRezId, fajlKnjige, fajlRezervacije)) {

                    System.out.println("Originalna kopija (ID: " + originalnoKnjigeId + ") nije dostupna.");
                    System.out.println("Dodeljena je alternativna kopija (ID: " + alternativaId + ") istog naslova.");

                    Knjiga novaKnjiga = ucitajKnjigu(alternativaId, fajlKnjige);
                    if (novaKnjiga != null) {
                        rezervacija.setKnjiga(novaKnjiga);
                        return true;
                    }
                }
            }

            return false;

        } catch (IOException e) {
            System.err.println("Greška pri proveri dostupnosti: " + e.getMessage());
            return false;
        }
    }

    private static boolean proveritDostupnostKopije(int knjigeId, LocalDate datumOd, LocalDate datumDo, 
                                                  int trenutnaRezId, String fajlKnjige, String fajlRezervacije) 
                                                  throws IOException {

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fajlRezervacije), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");

                if (delovi.length >= 7) {
                    try {
                        int rezervacijaId = Integer.parseInt(delovi[0].trim());
                        int bookId = Integer.parseInt(delovi[2].trim());
                        LocalDate rezDatumOd = LocalDate.parse(delovi[4].trim());
                        LocalDate rezDatumDo = LocalDate.parse(delovi[5].trim());
                        String status = delovi[6].trim();

                        if (rezervacijaId == trenutnaRezId || !"POTVRĐENA".equalsIgnoreCase(status)) {
                            continue;
                        }

                        if (bookId == knjigeId) {
                            boolean overlap = !(datumDo.isBefore(rezDatumOd) || datumOd.isAfter(rezDatumDo));
                            if (overlap) {
                                return false;
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }

        return true;
    }

    private static List<Integer> nadjiAlternativeKopije(String naslovKnjige, int originalniId, String fajlKnjige) 
                                                       throws IOException {
        List<Integer> alternative = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fajlKnjige), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");

                if (delovi.length >= 4) {
                    try {
                        String naslov = delovi[0].trim();
                        int idKnjige = Integer.parseInt(delovi[3].trim());

                        if (naslov.equalsIgnoreCase(naslovKnjige) && idKnjige != originalniId) {
                            alternative.add(idKnjige);
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            }
        }
        
        return alternative;
    }

    private static Knjiga ucitajKnjigu(int knjigeId, String fajlKnjige) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fajlKnjige), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");

                if (delovi.length >= 4) {
                    try {
                        int idKnjige = Integer.parseInt(delovi[3].trim());
                        if (idKnjige == knjigeId) {
                            String naslov = delovi[0].trim();
                            String autor = delovi[1].trim();
                            String zanr = delovi[2].trim();
                            StatusKnjige status = delovi.length >= 5 && !delovi[4].trim().isEmpty() 
                                                ? StatusKnjige.valueOf(delovi[4].trim()) 
                                                : StatusKnjige.DOSTUPNA;
                            
                            return new Knjiga(naslov, autor, zanr, idKnjige, status);
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
        
        return null;
    }

    public static void odbijNepotvrdeneRezervacije() {
        String fajlRez = "podaci/rezervacije.csv";
        String fajlObradjene = "podaci/rezervacije_obradjene.csv";
        
        LocalDate danas = LocalDate.now();
        List<String> linije = new ArrayList<>();
        List<String> odbijeneRezervacije = new ArrayList<>();
        boolean imaPromena = false;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlRez), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");

                if (delovi.length >= 7) {
                    try {
                        String status = delovi[6].trim();
                        LocalDate datumPocetka = null;
                        LocalDate datumObrade = null;

                        String[] patterns = {"d.M.yyyy", "dd.MM.yyyy", "yyyy-MM-dd"};
                        for (String p : patterns) {
                            try {
                                java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                                datumPocetka = LocalDate.parse(delovi[4].trim());
                                break;
                            } catch (Exception ignored) {}
                        }

                        if ("NA_ČEKANJU".equals(status) && datumPocetka.isBefore(danas)) {

                            delovi[6] = "ODBIJENA";

                            if (delovi.length > 8) {
                                delovi[8] = "SISTEM";
                            }
                            
                            String novaLinija = String.join(";", delovi);
                            linije.add(novaLinija);

                            odbijeneRezervacije.add(String.format("ID: %s, Korisnik: %s, Knjiga: %s, Datum: %s", 
                                delovi[0], delovi[1], delovi[3], delovi[4]));
                            
                            imaPromena = true;
                            
                            System.out.println("Automatski odbačena rezervacija ID: " + delovi[0] + 
                                             " - datum početka: " + datumPocetka);
                        } else {

                            linije.add(line);
                        }
                    } catch (Exception e) {

                        linije.add(line);
                        System.err.println("Greška pri obradi linije: " + line + " - " + e.getMessage());
                    }
                } else {

                    linije.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Greška pri čitanju rezervacija: " + e.getMessage());
            return;
        }

        if (imaPromena) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fajlRez, false), StandardCharsets.UTF_8))) {
                
                for (String linija : linije) {
                    bw.write(linija);
                    bw.newLine();
                }
                
                System.out.println("Automatski odbačeno " + odbijeneRezervacije.size() + " rezervacija.");
                
            } catch (IOException e) {
                System.err.println("Greška pri upisu rezervacija: " + e.getMessage());
                return;
            }

            if (!odbijeneRezervacije.isEmpty()) {
                try (BufferedWriter bwObradjene = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(fajlObradjene, true), StandardCharsets.UTF_8))) {
                    
                    for (String linija : linije) {
                        String[] delovi = linija.split(";");
                        if (delovi.length >= 7 && "ODBIJENA".equals(delovi[6]) && "SISTEM".equals(delovi[8])) {
                            bwObradjene.write(linija + ";" + danas.toString());
                            bwObradjene.newLine();
                        }
                    }
                    
                } catch (IOException e) {
                    System.err.println("Greška pri dodavanju u obrađene rezervacije: " + e.getMessage());
                }
            }

            if (!odbijeneRezervacije.isEmpty()) {
                System.out.println("\n=== AUTOMATSKI ODBAČENE REZERVACIJE ===");
                for (String rez : odbijeneRezervacije) {
                    System.out.println("- " + rez);
                }
                System.out.println("========================================\n");
            }
        }
    }
    
}