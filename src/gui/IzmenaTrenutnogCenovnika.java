package gui;

import enumi.Clanarina;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import manager.CenovnikMetode;
import modeli.*;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class IzmenaTrenutnogCenovnika extends JDialog {

    private static final String FILE_KAZNE = "podaci/kazne.csv";
    private static final String FILE_USLUGE = "podaci/usluge.csv";
    private static final String FILE_CLANARINE = "podaci/clanarine.csv";

    private JTable tableStavke;
    private Cenovnik trenutniCenovnik;
    private List<ClanarinaStavka> clanarine;
    private List<Kazna> kazne;
    private List<Usluga> usluge;
    private DefaultTableModel model;

    public IzmenaTrenutnogCenovnika(JFrame parent, Cenovnik cenovnik) {
        super(parent, "Izmena cenovnika - ID: " + cenovnik.getId(), true);
        this.trenutniCenovnik = cenovnik;
        this.clanarine = new ArrayList<>(cenovnik.getClanarine());
        this.kazne = new ArrayList<>(cenovnik.getKazne());
        this.usluge = new ArrayList<>(cenovnik.getUsluge());

        setBounds(100, 100, 800, 500);
        setLayout(new BorderLayout());


        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Napomene"));
        JLabel infoLabel = new JLabel("<html><b>Izmena cenovnika:</b> Možete dodavati samo postojeće stavke iz fajlova, " +
            "menjati cene i brisati stavke.<br/>" +
            "<b style='color: red;'>Važno:</b> Cenovnik mora imati i MESEČNU i GODIŠNJU članarinu!</html>");
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.NORTH);

        String[] kolone = {"Tip", "Naziv / Opis", "Cena"};
        model = new DefaultTableModel(kolone, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };

        popuniTabelu();

        tableStavke = new JTable(model);
        tableStavke.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        

        tableStavke.setAutoCreateRowSorter(true);
        
        add(new JScrollPane(tableStavke), BorderLayout.CENTER);

        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDodaj = new JButton("Dodaj postojeću stavku");
        JButton btnObrisi = new JButton("Obriši stavku");
        JButton btnSacuvaj = new JButton("Sačuvaj promene");
        JButton btnCancel = new JButton("Odustani");

        btnDodaj.setPreferredSize(new Dimension(180, 30));
        btnObrisi.setPreferredSize(new Dimension(120, 30));
        btnSacuvaj.setPreferredSize(new Dimension(150, 30));
        btnCancel.setPreferredSize(new Dimension(100, 30));

        btnDodaj.addActionListener(e -> dodajPostojecuStavku());
        btnObrisi.addActionListener(e -> obrisiStavku());
        btnSacuvaj.addActionListener(e -> sacuvajPromene());
        btnCancel.addActionListener(e -> dispose());

        buttonPane.add(btnDodaj);
        buttonPane.add(btnObrisi);
        buttonPane.add(btnSacuvaj);
        buttonPane.add(btnCancel);
        add(buttonPane, BorderLayout.SOUTH);

        setLocationRelativeTo(parent);
    }

    private void popuniTabelu() {
        model.setRowCount(0);
        for (ClanarinaStavka cs : clanarine)
            model.addRow(new Object[]{"Članarina", cs.getTip().toString(), cs.getCena()});
        for (Kazna k : kazne)
            model.addRow(new Object[]{"Kazna", k.getOpis(), k.getIznos()});
        for (Usluga u : usluge)
            model.addRow(new Object[]{"Usluga", u.getNaziv(), u.getCena()});
    }

    private void dodajPostojecuStavku() {
        String[] opcije = {"Članarina", "Kazna", "Usluga"};
        String tip = (String) JOptionPane.showInputDialog(this, "Izaberite tip stavke koju želite da dodate:", 
                "Dodaj postojeću stavku", JOptionPane.PLAIN_MESSAGE, null, opcije, opcije[0]);
        if (tip == null) return;

        switch (tip) {
            case "Članarina":
                dodajClanarinu();
                break;
            case "Kazna":
                dodajKaznu();
                break;
            case "Usluga":
                dodajUslugu();
                break;
        }
    }

    private void dodajClanarinu() {

        List<ClanarinaModel> dostupneClanarine = CenovnikMetode.ucitajClanarine(FILE_CLANARINE);
        
        if (dostupneClanarine.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nema dostupnih članarina u fajlu!", 
                "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<ClanarinaModel> moguceClanarine = new ArrayList<>();
        for (ClanarinaModel cm : dostupneClanarine) {
            boolean postoji = false;
            for (ClanarinaStavka postojeca : clanarine) {
                if (postojeca.getTip().equals(cm.getTip())) {
                    postoji = true;
                    break;
                }
            }
            if (!postoji) {
                moguceClanarine.add(cm);
            }
        }

        if (moguceClanarine.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sve dostupne članarine su već u cenovniku!", 
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ClanarinaModel izabrana = (ClanarinaModel) JOptionPane.showInputDialog(
            this,
            "Izaberite članarinu za dodavanje:",
            "Dodaj članarinu",
            JOptionPane.QUESTION_MESSAGE,
            null,
            moguceClanarine.toArray(),
            moguceClanarine.get(0)
        );

        if (izabrana != null) {
            ClanarinaStavka nova = new ClanarinaStavka(izabrana.getTip(), izabrana.getIznos());
            clanarine.add(nova);
            model.addRow(new Object[]{"Članarina", nova.getTip().toString(), nova.getCena()});
            JOptionPane.showMessageDialog(this, "Članarina dodana u cenovnik!");
        }
    }

    private void dodajKaznu() {
        List<Kazna> dostupneKazne = CenovnikMetode.ucitajKazne(FILE_KAZNE);
        
        if (dostupneKazne.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nema dostupnih kazni u fajlu!", 
                "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Kazna> moguceKazne = new ArrayList<>();
        for (Kazna k : dostupneKazne) {
            boolean postoji = false;
            for (Kazna postojeca : kazne) {
                if (postojeca.getOpis().equalsIgnoreCase(k.getOpis())) {
                    postoji = true;
                    break;
                }
            }
            if (!postoji) {
                moguceKazne.add(k);
            }
        }

        if (moguceKazne.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sve dostupne kazne su već u cenovniku!", 
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Kazna izabrana = (Kazna) JOptionPane.showInputDialog(
            this,
            "Izaberite kaznu za dodavanje:",
            "Dodaj kaznu",
            JOptionPane.QUESTION_MESSAGE,
            null,
            moguceKazne.toArray(),
            moguceKazne.get(0)
        );

        if (izabrana != null) {
            Kazna nova = new Kazna(izabrana.getOpis(), izabrana.getIznos());
            kazne.add(nova);
            model.addRow(new Object[]{"Kazna", nova.getOpis(), nova.getIznos()});
            JOptionPane.showMessageDialog(this, "Kazna dodana u cenovnik!");
        }
    }

    private void dodajUslugu() {
        List<Usluga> dostupneUsluge = CenovnikMetode.ucitajUsluge(FILE_USLUGE);
        
        if (dostupneUsluge.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nema dostupnih usluga u fajlu!", 
                "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Usluga> moguceUsluge = new ArrayList<>();
        for (Usluga u : dostupneUsluge) {
            boolean postoji = false;
            for (Usluga postojeca : usluge) {
                if (postojeca.getNaziv().equalsIgnoreCase(u.getNaziv())) {
                    postoji = true;
                    break;
                }
            }
            if (!postoji) {
                moguceUsluge.add(u);
            }
        }

        if (moguceUsluge.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sve dostupne usluge su već u cenovniku!", 
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Usluga izabrana = (Usluga) JOptionPane.showInputDialog(
            this,
            "Izaberite uslugu za dodavanje:",
            "Dodaj uslugu",
            JOptionPane.QUESTION_MESSAGE,
            null,
            moguceUsluge.toArray(),
            moguceUsluge.get(0)
        );

        if (izabrana != null) {
            Usluga nova = new Usluga(izabrana.getNaziv(), izabrana.getCena());
            usluge.add(nova);
            model.addRow(new Object[]{"Usluga", nova.getNaziv(), nova.getCena()});
            JOptionPane.showMessageDialog(this, "Usluga dodana u cenovnik!");
        }
    }

    private void obrisiStavku() {
        int selectedRow = tableStavke.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Odaberite stavku za brisanje!");
            return;
        }

        int modelRow = tableStavke.convertRowIndexToModel(selectedRow);
        
        String tip = (String) model.getValueAt(modelRow, 0);
        String naziv = (String) model.getValueAt(modelRow, 1);

        if (tip.equals("Članarina")) {
            Clanarina tipClanarine = Clanarina.valueOf(naziv);

            boolean imaMesecnu = false;
            boolean imaGodisnju = false;
            
            for (ClanarinaStavka cs : clanarine) {
                if (!cs.getTip().equals(tipClanarine)) {
                    if (cs.getTip() == Clanarina.mesečna) imaMesecnu = true;
                    if (cs.getTip() == Clanarina.godišnja) imaGodisnju = true;
                }
            }
            
            if (!imaMesecnu || !imaGodisnju) {
                JOptionPane.showMessageDialog(this, 
                    "Ne možete obrisati ovu članarinu!\n" +
                    "Cenovnik mora imati i MESEČNU i GODIŠNJU članarinu.", 
                    "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int result = JOptionPane.showConfirmDialog(this,
            "Da li ste sigurni da želite da obrišete stavku: " + naziv + "?",
            "Potvrda brisanja",
            JOptionPane.YES_NO_OPTION);
            
        if (result != JOptionPane.YES_OPTION) return;

        switch (tip) {
            case "Članarina":
                clanarine.removeIf(cs -> cs.getTip().toString().equals(naziv));
                break;
            case "Kazna":
                kazne.removeIf(k -> k.getOpis().equals(naziv));
                break;
            case "Usluga":
                usluge.removeIf(u -> u.getNaziv().equals(naziv));
                break;
        }

        model.removeRow(modelRow);
        JOptionPane.showMessageDialog(this, "Stavka obrisana!");
    }

    private void sacuvajPromene() {

        boolean imaMesecnu = false;
        boolean imaGodisnju = false;

        try {
            for (int row = 0; row < model.getRowCount(); row++) {
                String tip = model.getValueAt(row, 0).toString();
                String nazivOpis = model.getValueAt(row, 1).toString();
                double novaCena = Double.parseDouble(model.getValueAt(row, 2).toString());

                switch (tip) {
                    case "Članarina":
                        Clanarina tipClanarine = Clanarina.valueOf(nazivOpis);
                        for (ClanarinaStavka cs : clanarine) {
                            if (cs.getTip().equals(tipClanarine)) {
                                cs.setCena(novaCena);
                                break;
                            }
                        }
                        if (tipClanarine == Clanarina.mesečna) imaMesecnu = true;
                        if (tipClanarine == Clanarina.godišnja) imaGodisnju = true;
                        break;
                    case "Kazna":
                        for (Kazna k : kazne) {
                            if (k.getOpis().equals(nazivOpis)) {
                                k.setIznos(novaCena);
                                break;
                            }
                        }
                        break;
                    case "Usluga":
                        for (Usluga u : usluge) {
                            if (u.getNaziv().equals(nazivOpis)) {
                                u.setCena(novaCena);
                                break;
                            }
                        }
                        break;
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Neispravna vrednost za cenu!", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!imaMesecnu || !imaGodisnju) {
            JOptionPane.showMessageDialog(this, 
                "Cenovnik mora imati i MESEČNU i GODIŠNJU članarinu!\n" +
                "Dodajte nedostajuće članarine pre čuvanja.", 
                "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {

            trenutniCenovnik.setClanarine(clanarine);
            trenutniCenovnik.setKazne(kazne);
            trenutniCenovnik.setUsluge(usluge);

            List<Cenovnik> sviCenovnici = CenovnikMetode.ucitajCenovnike("podaci/cenovnici.csv");

            for (int i = 0; i < sviCenovnici.size(); i++) {
                if (sviCenovnici.get(i).getId() == trenutniCenovnik.getId()) {
                    sviCenovnici.set(i, trenutniCenovnik);
                    break;
                }
            }

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("podaci/cenovnici.csv", false)))) {
                for (Cenovnik c : sviCenovnici) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(c.getId()).append(";")
                      .append(c.getDatumOd()).append(";")
                      .append(c.getDatumDo()).append(";");

                    for (int i = 0; i < c.getClanarine().size(); i++) {
                        ClanarinaStavka cs = c.getClanarine().get(i);
                        sb.append(cs.getTip()).append(",").append(cs.getCena());
                        if (i < c.getClanarine().size() - 1) sb.append("|");
                    }
                    sb.append(";");

                    for (int i = 0; i < c.getKazne().size(); i++) {
                        Kazna k = c.getKazne().get(i);
                        sb.append(k.getOpis()).append(",").append(k.getIznos());
                        if (i < c.getKazne().size() - 1) sb.append("|");
                    }
                    sb.append(";");

                    for (int i = 0; i < c.getUsluge().size(); i++) {
                        Usluga u = c.getUsluge().get(i);
                        sb.append(u.getNaziv()).append(",").append(u.getCena());
                        if (i < c.getUsluge().size() - 1) sb.append("|");
                    }

                    out.println(sb.toString());
                }
            }

            JOptionPane.showMessageDialog(this, 
                "Cenovnik uspešno ažuriran!\n\n" +
                "Članarine: " + clanarine.size() + "\n" +
                "Kazne: " + kazne.size() + "\n" +
                "Usluge: " + usluge.size(),
                "Uspeh", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Greška pri upisu u fajl: " + ex.getMessage(), 
                "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }
}