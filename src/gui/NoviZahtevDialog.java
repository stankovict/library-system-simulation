package gui;

import manager.CenovnikMetode;
import manager.ClanMetode;
import manager.RezervacijaMetode;
import manager.UlogovaniKorisnik;
import modeli.Cenovnik;
import modeli.Clan;
import modeli.Knjiga;
import modeli.PosebnaKategorija;
import modeli.Rezervacija;
import modeli.Usluga;
import enumi.Clanarina;
import enumi.Pol;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class NoviZahtevDialog extends JDialog {

    private Clan ulogovaniClan;
    private JTable tableKnjige;
    private JList<String> listUsluge;
    private JList<String> listOdabraneUsluge;
    private JLabel lblCena;
    private JButton btnKreiraj;
    private JButton btnDodajUslugu;
    private JButton btnUkloniUslugu;
    private LocalDate datumPocetka, originalDatumKraja, datumRezervacije;
    private Cenovnik cenovnik;
    private double ukupnaCena;

    private JSpinner spDatumOd;
    private JSpinner spDatumDo;
    private JSpinner spDatumRezervacije;

    private JTextField tfNaslov;
    private JTextField tfAutor;
    private JTextField tfZanr;

    private List<String> odabraneUsluge = new ArrayList<>();
    private List<Double> ceneDodanihUsluga = new ArrayList<>();

    private int zadrzavanjeDani = 0;
    private boolean zadrzavanjeSelektovano = false;

    public NoviZahtevDialog(JFrame parent, Clan clan) {
        super(parent, "Nova rezervacija", true);
        this.ulogovaniClan = clan;

        setSize(750, 600);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(null);

        spDatumOd = dodajDatumSpinner("Datum početka:", 20);
        spDatumDo = dodajDatumSpinner("Datum kraja:", 50);
        spDatumRezervacije = dodajDatumSpinner("Datum početka rezervacije:", 80);
        
        JLabel lblNaslov = new JLabel("Naslov:");
        lblNaslov.setBounds(20, 110, 60, 25);
        getContentPane().add(lblNaslov);

        tfNaslov = new JTextField();
        tfNaslov.setBounds(80, 110, 150, 25);
        getContentPane().add(tfNaslov);

        JLabel lblAutor = new JLabel("Autor:");
        lblAutor.setBounds(250, 110, 60, 25);
        getContentPane().add(lblAutor);

        tfAutor = new JTextField();
        tfAutor.setBounds(310, 110, 150, 25);
        getContentPane().add(tfAutor);

        JLabel lblZanr = new JLabel("Žanr:");
        lblZanr.setBounds(480, 110, 60, 25);
        getContentPane().add(lblZanr);

        tfZanr = new JTextField();
        tfZanr.setBounds(530, 110, 150, 25);
        getContentPane().add(tfZanr);

        JButton btnUcitaj = new JButton("Prikaži dostupne knjige");
        btnUcitaj.setBounds(360, 35, 250, 30);
        getContentPane().add(btnUcitaj);

        tableKnjige = new JTable();
        JScrollPane scrollPane = new JScrollPane(tableKnjige);
        scrollPane.setBounds(20, 149, 700, 151);
        getContentPane().add(scrollPane);

        JLabel lblUsluge = new JLabel("Dostupne usluge:");
        lblUsluge.setBounds(20, 320, 120, 25);
        getContentPane().add(lblUsluge);

        listUsluge = new JList<>();
        listUsluge.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollUsluge = new JScrollPane(listUsluge);
        scrollUsluge.setBounds(20, 350, 200, 100);
        getContentPane().add(scrollUsluge);

        btnDodajUslugu = new JButton("Dodaj uslugu");
        btnDodajUslugu.setBounds(240, 360, 120, 30);
        getContentPane().add(btnDodajUslugu);

        JLabel lblOdabrane = new JLabel("Odabrane usluge:");
        lblOdabrane.setBounds(380, 320, 120, 25);
        getContentPane().add(lblOdabrane);

        listOdabraneUsluge = new JList<>();
        listOdabraneUsluge.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollOdabrane = new JScrollPane(listOdabraneUsluge);
        scrollOdabrane.setBounds(380, 350, 200, 100);
        getContentPane().add(scrollOdabrane);

        btnUkloniUslugu = new JButton("Ukloni uslugu");
        btnUkloniUslugu.setBounds(590, 360, 120, 30);
        getContentPane().add(btnUkloniUslugu);

        lblCena = new JLabel("Ukupna cena: 0.0");
        lblCena.setBounds(20, 470, 200, 25);
        getContentPane().add(lblCena);

        btnKreiraj = new JButton("Kreiraj rezervaciju");
        btnKreiraj.setBounds(250, 510, 200, 30);
        getContentPane().add(btnKreiraj);

        cenovnik = CenovnikMetode.ucitajTrenutniCenovnik("podaci/cenovnici.csv");

        btnUcitaj.addActionListener(e -> {
            try {
                datumPocetka = ((Date) spDatumOd.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                originalDatumKraja = ((Date) spDatumDo.getValue()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                
                if (originalDatumKraja.isBefore(datumPocetka)) {
                    JOptionPane.showMessageDialog(this, "Datum kraja mora biti posle datuma početka!");
                    return;
                }

                prikaziDostupneKnjige();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Greška pri parsiranju datuma: " + ex.getMessage());
            }
        });

        btnKreiraj.addActionListener(e -> {
            datumRezervacije = ((Date) spDatumRezervacije.getValue())
                    .toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            int selectedRow = tableKnjige.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Odaberite knjigu!");
                return;
            }

            String naslov = (String) tableKnjige.getValueAt(selectedRow, 0);

            Clan clanObj = ClanMetode.nadjiClanaPoUsername(ulogovaniClan.getUsername());
            double koef = getKoeficijentClana(clanObj);

            double cenaUsluga = 0;
            List<Usluga> uslugeRezervacije = new ArrayList<>();

            for (int i = 0; i < odabraneUsluge.size(); i++) {
                String naziv = odabraneUsluge.get(i);
                for (Usluga u : cenovnik.getUsluge()) {
                    if (u.getNaziv().equals(naziv)) {
                        uslugeRezervacije.add(u);
                        break;
                    }
                }
                cenaUsluga += ceneDodanihUsluga.get(i);
            }
            
            System.out.println(clanObj);
            System.out.println(naslov);
            System.out.println(datumRezervacije);
            System.out.println(cenaUsluga);
            System.out.println(uslugeRezervacije);
            System.out.println("Originalni datum kraja: " + originalDatumKraja);
            System.out.println("Finalni datum kraja: " + getFinalDatumKraja());

            boolean success = RezervacijaMetode.kreirajRezervaciju(
                    clanObj,
                    naslov,
                    datumRezervacije,
                    cenaUsluga,
                    uslugeRezervacije
            );

            System.out.println(success);

            if (success) {
                JOptionPane.showMessageDialog(this, "Rezervacija uspešno kreirana!\nUkupna cena usluga: " + cenaUsluga);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Greška pri kreiranju rezervacije!");
            }
        });
    }

    private JSpinner dodajDatumSpinner(String label, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 200, 25);
        getContentPane().add(lbl);

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SpinnerDateModel model = new SpinnerDateModel(today, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setBounds(220, y, 120, 25);

        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        getContentPane().add(spinner);
        return spinner;
    }

    private void prikaziDostupneKnjige() {
        List<Knjiga> knjige = RezervacijaMetode.pronadjiDostupneKnjige(datumPocetka, originalDatumKraja);

        String filterNaslov = tfNaslov.getText().trim().toLowerCase();
        String filterAutor = tfAutor.getText().trim().toLowerCase();
        String filterZanr = tfZanr.getText().trim().toLowerCase();

        String[] kolone = {"Naslov", "Autor", "Žanr"};
        DefaultTableModel model = new DefaultTableModel(kolone, 0);

        for (Knjiga k : knjige) {
            boolean odgovara = true;

            if (!filterNaslov.isEmpty() && !k.getNaslov().toLowerCase().contains(filterNaslov)) {
                odgovara = false;
            }
            if (!filterAutor.isEmpty() && !k.getAutor().toLowerCase().contains(filterAutor)) {
                odgovara = false;
            }
            if (!filterZanr.isEmpty() && !k.getZanr().toLowerCase().contains(filterZanr)) {
                odgovara = false;
            }

            if (odgovara) {
                model.addRow(new Object[]{k.getNaslov(), k.getAutor(), k.getZanr()});
            }
        }

        tableKnjige.setModel(model);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        cenovnik.getUsluge().forEach(u -> listModel.addElement(u.getNaziv()));
        listUsluge.setModel(listModel);

        ukupnaCena = 0;
        odabraneUsluge.clear();
        ceneDodanihUsluga.clear();
        zadrzavanjeDani = 0;
        zadrzavanjeSelektovano = false;
        azurirajPrikazOdabranihUsluga();
        lblCena.setText("Ukupna cena: " + ukupnaCena);

        btnDodajUslugu.addActionListener(e -> {
            String izabranaUsluga = listUsluge.getSelectedValue();
            if (izabranaUsluga == null) {
                JOptionPane.showMessageDialog(this, "Odaberite uslugu!");
                return;
            }

            if (odabraneUsluge.contains(izabranaUsluga)) {
                JOptionPane.showMessageDialog(this, "Usluga je već dodana!");
                return;
            }
            
            dodajUslugu(izabranaUsluga);
        });

        btnUkloniUslugu.addActionListener(e -> {
            String izabranaUsluga = listOdabraneUsluge.getSelectedValue();
            if (izabranaUsluga == null) {
                JOptionPane.showMessageDialog(this, "Odaberite uslugu za uklanjanje!");
                return;
            }
            
            ukloniUslugu(izabranaUsluga);
        });
    }

    private void dodajUslugu(String nazivUsluge) {
        double koef = getKoeficijentClana(ulogovaniClan);
        double cenaUsluge = 0;

        for (Usluga u : cenovnik.getUsluge()) {
            if (u.getNaziv().equals(nazivUsluge)) {
                if (nazivUsluge.equalsIgnoreCase("Zadržavanje")) {

                    SpinnerNumberModel brojDanaModel = new SpinnerNumberModel(1, 1, 30, 1);
                    JSpinner spDana = new JSpinner(brojDanaModel);
                    int option = JOptionPane.showOptionDialog(
                        this,
                        spDana,
                        "Odaberite broj dana zadržavanja",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        null
                    );
                    if (option == JOptionPane.OK_OPTION) {
                        zadrzavanjeDani = (int) spDana.getValue();
                        zadrzavanjeSelektovano = true;
                        cenaUsluge = u.getCena() * zadrzavanjeDani * koef;
                    } else {
                        return;
                    }
                } else {
                    cenaUsluge = u.getCena() * koef;
                }
                break;
            }
        }

        odabraneUsluge.add(nazivUsluge);
        ceneDodanihUsluga.add(cenaUsluge);

        azurirajPrikazOdabranihUsluga();
        azurirajUkupnuCenu();
    }
    
    private void ukloniUslugu(String nazivUsluge) {
        int index = -1;
        for (int i = 0; i < odabraneUsluge.size(); i++) {
            if (odabraneUsluge.get(i).equals(nazivUsluge)) {
                index = i;
                break;
            }
        }
        
        if (index != -1) {
            odabraneUsluge.remove(index);
            ceneDodanihUsluga.remove(index);

            if (nazivUsluge.equalsIgnoreCase("Zadržavanje")) {
                zadrzavanjeSelektovano = false;
                zadrzavanjeDani = 0;
            }
            
            azurirajPrikazOdabranihUsluga();
            azurirajUkupnuCenu();
        }
    }
    
    private void azurirajPrikazOdabranihUsluga() {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (int i = 0; i < odabraneUsluge.size(); i++) {
            String naziv = odabraneUsluge.get(i);
            double cena = ceneDodanihUsluga.get(i);
            if (naziv.equalsIgnoreCase("Zadržavanje")) {
                model.addElement(naziv + " (" + zadrzavanjeDani + " dana) - " + String.format("%.2f", cena));
            } else {
                model.addElement(naziv + " - " + String.format("%.2f", cena));
            }
        }
        listOdabraneUsluge.setModel(model);
    }
    
    private void azurirajUkupnuCenu() {
        ukupnaCena = 0;
        for (double cena : ceneDodanihUsluga) {
            ukupnaCena += cena;
        }
        lblCena.setText("Ukupna cena: " + String.format("%.2f", ukupnaCena));
    }

    private LocalDate getFinalDatumKraja() {
        LocalDate finalniDatum = originalDatumKraja;
        if (zadrzavanjeSelektovano) {
            finalniDatum = finalniDatum.plusDays(zadrzavanjeDani);
        }
        return finalniDatum;
    }

    private double getKoeficijentClana(Clan clan) {

        return clan.getKategorija().getPopust();
    }
}