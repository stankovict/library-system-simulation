package gui;

import enumi.Clanarina;

import manager.CenovnikMetode;
import modeli.Cenovnik;
import modeli.ClanarinaModel;
import modeli.ClanarinaStavka;
import modeli.Kazna;
import modeli.Usluga;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class NoviCenovnikDialog extends JDialog {

    private static final String FILE_CENOVNICI = "podaci/cenovnici.csv";
    private static final String FILE_KAZNE = "podaci/kazne.csv";
    private static final String FILE_USLUGE = "podaci/usluge.csv";
    private static final String FILE_CLANARINE = "podaci/clanarine.csv";

    private Cenovnik noviCenovnik;

    private JSpinner spnId;
    private JSpinner spnDatumOd;
    private JSpinner spnDatumDo;

    private DefaultListModel<ClanarinaModel> listaSveClanarine = new DefaultListModel<>();
    private DefaultListModel<Kazna> listaSveKazne = new DefaultListModel<>();
    private DefaultListModel<Usluga> listaSveUsluge = new DefaultListModel<>();

    private DefaultListModel<ClanarinaStavka> listaOdabraneClanarine = new DefaultListModel<>();
    private DefaultListModel<Kazna> listaOdabraneKazne = new DefaultListModel<>();
    private DefaultListModel<Usluga> listaOdabraneUsluge = new DefaultListModel<>();

    public NoviCenovnikDialog(JFrame parent) {
        super(parent, "Novi cenovnik", true);
        setBounds(100, 100, 800, 600);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        ucitajPostojeceStavke();

        JPanel unosPanel = napraviUnosPanel();
        contentPanel.add(unosPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = napraviTabbedPane();
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPane = napraviButtonPanel();
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        setLocationRelativeTo(parent);
    }

    private JPanel napraviUnosPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Osnovni podaci cenovnika"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("ID cenovnika:"), gbc);
        gbc.gridx = 1;
        spnId = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spnId.setPreferredSize(new Dimension(100, 25));
        panel.add(spnId, gbc);

        gbc.gridx = 2;
        JLabel idInfo = new JLabel("(mora biti jedinstven)");
        idInfo.setFont(idInfo.getFont().deriveFont(Font.ITALIC, 11f));
        idInfo.setForeground(Color.GRAY);
        panel.add(idInfo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Važi od:"), gbc);
        gbc.gridx = 1;
        spnDatumOd = napraviDateSpinner();
        panel.add(spnDatumOd, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Važi do:"), gbc);
        gbc.gridx = 1;
        spnDatumDo = napraviDateSpinner();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        spnDatumDo.setValue(cal.getTime());
        panel.add(spnDatumDo, gbc);

        return panel;
    }

    private JSpinner napraviDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(120, 25));
        return spinner;
    }

    private JTabbedPane napraviTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel clanarinePanelWrapper = new JPanel(new BorderLayout());
        clanarinePanelWrapper.add(napraviClanarinePanel(), BorderLayout.CENTER);
        
        JPanel clanarineInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clanarineInfo.setBorder(BorderFactory.createTitledBorder("Napomena"));
        JLabel infoLabel = new JLabel("<html><b style='color: red;'>OBAVEZNO:</b> Morate dodati i MESEČNU i GODIŠNJU članarinu u cenovnik!</html>");
        clanarineInfo.add(infoLabel);
        clanarinePanelWrapper.add(clanarineInfo, BorderLayout.NORTH);
        
        tabbedPane.add("Članarine *", clanarinePanelWrapper);
        tabbedPane.add("Kazne", napraviListPanel(listaSveKazne, listaOdabraneKazne, "kaznu"));
        tabbedPane.add("Usluge", napraviListPanel(listaSveUsluge, listaOdabraneUsluge, "uslugu"));

        return tabbedPane;
    }

    private JPanel napraviButtonPanel() {
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPane.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnSacuvaj = new JButton("Sačuvaj cenovnik");
        btnSacuvaj.setPreferredSize(new Dimension(150, 30));
        btnSacuvaj.setBackground(new Color(76, 175, 80));
        btnSacuvaj.setForeground(Color.WHITE);
        btnSacuvaj.addActionListener(e -> sacuvaj());
        
        JButton btnCancel = new JButton("Odustani");
        btnCancel.setPreferredSize(new Dimension(100, 30));
        btnCancel.addActionListener(e -> dispose());
        
        buttonPane.add(btnSacuvaj);
        buttonPane.add(btnCancel);
        return buttonPane;
    }

    private void ucitajPostojeceStavke() {
        listaSveKazne.clear();
        listaSveUsluge.clear();
        listaSveClanarine.clear();
        
        CenovnikMetode.ucitajKazne(FILE_KAZNE).forEach(listaSveKazne::addElement);
        CenovnikMetode.ucitajUsluge(FILE_USLUGE).forEach(listaSveUsluge::addElement);
        CenovnikMetode.ucitajClanarine(FILE_CLANARINE).forEach(listaSveClanarine::addElement);
    }

    private JPanel napraviClanarinePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JPanel leviPanel = new JPanel(new BorderLayout());
        leviPanel.setBorder(BorderFactory.createTitledBorder("Dostupne članarine"));
        JList<ClanarinaModel> jlistSve = new JList<>(listaSveClanarine);
        jlistSve.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leviPanel.add(new JScrollPane(jlistSve), BorderLayout.CENTER);

        JButton btnDodaj = new JButton("Dodaj u cenovnik →");
        btnDodaj.addActionListener(e -> {
            ClanarinaModel selected = jlistSve.getSelectedValue();
            if (selected != null) {

                boolean postoji = false;
                for (int i = 0; i < listaOdabraneClanarine.size(); i++) {
                    ClanarinaStavka postojeca = listaOdabraneClanarine.getElementAt(i);
                    if (postojeca.getTip().equals(selected.getTip())) {
                        postoji = true;
                        break;
                    }
                }
                
                if (postoji) {
                    JOptionPane.showMessageDialog(this, 
                        "Članarina tipa '" + selected.getTip() + "' već postoji u cenovniku!", 
                        "Upozorenje", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                ClanarinaStavka nova = new ClanarinaStavka(selected.getTip(), selected.getIznos());
                listaOdabraneClanarine.addElement(nova);
                JOptionPane.showMessageDialog(this, "Članarina dodana u cenovnik!");
            } else {
                JOptionPane.showMessageDialog(this, "Izaberite članarinu za dodavanje!");
            }
        });
        leviPanel.add(btnDodaj, BorderLayout.SOUTH);

        JPanel desniPanel = new JPanel(new BorderLayout());
        desniPanel.setBorder(BorderFactory.createTitledBorder("Članarine u cenovniku"));
        JList<ClanarinaStavka> jlistOdabrane = new JList<>(listaOdabraneClanarine);
        desniPanel.add(new JScrollPane(jlistOdabrane), BorderLayout.CENTER);

        JButton btnUkloni = new JButton("← Ukloni iz cenovnika");
        btnUkloni.addActionListener(e -> {
            int index = jlistOdabrane.getSelectedIndex();
            if (index >= 0) {
                listaOdabraneClanarine.removeElementAt(index);
                JOptionPane.showMessageDialog(this, "Članarina uklonjena iz cenovnika!");
            } else {
                JOptionPane.showMessageDialog(this, "Izaberite članarinu za uklanjanje!");
            }
        });
        desniPanel.add(btnUkloni, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leviPanel);
        splitPane.setRightComponent(desniPanel);
        splitPane.setDividerLocation(350);
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private <T> JPanel napraviListPanel(DefaultListModel<T> listaSve, DefaultListModel<T> listaOdabrane, String tipStavke) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JPanel leviPanel = new JPanel(new BorderLayout());
        leviPanel.setBorder(BorderFactory.createTitledBorder("Dostupne " + tipStavke.substring(0, tipStavke.length()-1) + "e"));
        JList<T> jlistSve = new JList<>(listaSve);
        leviPanel.add(new JScrollPane(jlistSve), BorderLayout.CENTER);

        JButton btnDodaj = new JButton("Dodaj " + tipStavke + " →");
        btnDodaj.addActionListener(e -> {
            int index = jlistSve.getSelectedIndex();
            if (index >= 0) {
                T stavka = listaSve.getElementAt(index);

                boolean postoji = false;
                if (stavka instanceof Usluga) {
                    String naziv = ((Usluga) stavka).getNaziv();
                    for (int i = 0; i < listaOdabrane.size(); i++) {
                        if (listaOdabrane.getElementAt(i) instanceof Usluga) {
                            if (((Usluga) listaOdabrane.getElementAt(i)).getNaziv().equalsIgnoreCase(naziv)) {
                                postoji = true;
                                break;
                            }
                        }
                    }
                } else if (stavka instanceof Kazna) {
                    String opis = ((Kazna) stavka).getOpis();
                    for (int i = 0; i < listaOdabrane.size(); i++) {
                        if (listaOdabrane.getElementAt(i) instanceof Kazna) {
                            if (((Kazna) listaOdabrane.getElementAt(i)).getOpis().equalsIgnoreCase(opis)) {
                                postoji = true;
                                break;
                            }
                        }
                    }
                }

                if (postoji) {
                    JOptionPane.showMessageDialog(this, "Ova stavka već postoji u cenovniku!", 
                        "Upozorenje", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                listaOdabrane.addElement(stavka);
                JOptionPane.showMessageDialog(this, "Stavka dodana u cenovnik!");
            } else {
                JOptionPane.showMessageDialog(this, "Izaberite stavku za dodavanje!");
            }
        });
        leviPanel.add(btnDodaj, BorderLayout.SOUTH);

        JPanel desniPanel = new JPanel(new BorderLayout());
        desniPanel.setBorder(BorderFactory.createTitledBorder(
            (tipStavke.equals("kaznu") ? "Kazne" : "Usluge") + " u cenovniku"));
        JList<T> jlistOdabrane = new JList<>(listaOdabrane);
        desniPanel.add(new JScrollPane(jlistOdabrane), BorderLayout.CENTER);

        JButton btnUkloni = new JButton("← Ukloni " + tipStavke);
        btnUkloni.addActionListener(e -> {
            int index = jlistOdabrane.getSelectedIndex();
            if (index >= 0) {
                listaOdabrane.removeElementAt(index);
                JOptionPane.showMessageDialog(this, "Stavka uklonjena iz cenovnika!");
            } else {
                JOptionPane.showMessageDialog(this, "Izaberite stavku za uklanjanje!");
            }
        });
        desniPanel.add(btnUkloni, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leviPanel);
        splitPane.setRightComponent(desniPanel);
        splitPane.setDividerLocation(350);
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private boolean postojiIdCenovnika(int id) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_CENOVNICI))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length > 0) {
                    try {
                        int postojeciId = Integer.parseInt(delovi[0].trim());
                        if (postojeciId == id) {
                            return true;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean postojiPreklapanje(LocalDate noviOd, LocalDate noviDo) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_CENOVNICI))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length < 3) continue;

                try {
                    LocalDate od = LocalDate.parse(delovi[1]);
                    LocalDate doDat = LocalDate.parse(delovi[2]);

                    if (!noviOd.isAfter(doDat) && !noviDo.isBefore(od)) {
                        return true;
                    }
                } catch (DateTimeParseException e) {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sacuvaj() {
        try {
            int id = (Integer) spnId.getValue();

            Calendar calOd = Calendar.getInstance();
            calOd.setTime((java.util.Date) spnDatumOd.getValue());
            LocalDate datumOd = LocalDate.of(calOd.get(Calendar.YEAR), 
                                           calOd.get(Calendar.MONTH) + 1, 
                                           calOd.get(Calendar.DAY_OF_MONTH));
            
            Calendar calDo = Calendar.getInstance();
            calDo.setTime((java.util.Date) spnDatumDo.getValue());
            LocalDate datumDo = LocalDate.of(calDo.get(Calendar.YEAR), 
                                           calDo.get(Calendar.MONTH) + 1, 
                                           calDo.get(Calendar.DAY_OF_MONTH));

            if (postojiIdCenovnika(id)) {
                JOptionPane.showMessageDialog(this, "Cenovnik sa ID " + id + " već postoji! Izaberite drugi ID.", 
                                            "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (datumDo.isBefore(datumOd) || datumDo.equals(datumOd)) {
                JOptionPane.showMessageDialog(this, "Datum kraja mora biti posle datuma početka!", 
                                            "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (postojiPreklapanje(datumOd, datumDo)) {
                JOptionPane.showMessageDialog(this, "Ne možete dodati cenovnik – period se preklapa sa postojećim!", 
                                              "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean imaMesecnu = false;
            boolean imaGodisnju = false;
            
            for (int i = 0; i < listaOdabraneClanarine.size(); i++) {
                ClanarinaStavka stavka = listaOdabraneClanarine.getElementAt(i);
                if (stavka.getTip() == Clanarina.mesečna) {
                    imaMesecnu = true;
                } else if (stavka.getTip() == Clanarina.godišnja) {
                    imaGodisnju = true;
                }
            }
            
            if (!imaMesecnu && !imaGodisnju) {
                JOptionPane.showMessageDialog(this, 
                    "Morate dodati i MESEČNU i GODIŠNJU članarinu u cenovnik!\n" +
                    "Cenovnik mora imati oba tipa članarina.", 
                    "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (!imaMesecnu) {
                JOptionPane.showMessageDialog(this, 
                    "Nedostaje MESEČNA članarina!\n" +
                    "Cenovnik mora imati i mesečnu i godišnju članarinu.", 
                    "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (!imaGodisnju) {
                JOptionPane.showMessageDialog(this, 
                    "Nedostaje GODIŠNJA članarina!\n" +
                    "Cenovnik mora imati i mesečnu i godišnju članarinu.", 
                    "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            noviCenovnik = new Cenovnik(id, datumOd, datumDo);

            for (int i = 0; i < listaOdabraneClanarine.size(); i++) {
                noviCenovnik.dodajClanarinu(listaOdabraneClanarine.getElementAt(i));
            }

            for (int i = 0; i < listaOdabraneKazne.size(); i++) {
                noviCenovnik.dodajKaznu(listaOdabraneKazne.getElementAt(i));
            }

            for (int i = 0; i < listaOdabraneUsluge.size(); i++) {
                noviCenovnik.dodajUslugu(listaOdabraneUsluge.getElementAt(i));
            }

            try (java.io.FileWriter fw = new java.io.FileWriter(FILE_CENOVNICI, true);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
                 java.io.PrintWriter out = new java.io.PrintWriter(bw)) {

                StringBuilder sb = new StringBuilder();
                sb.append(noviCenovnik.getId()).append(";");
                sb.append(noviCenovnik.getDatumOd()).append(";");
                sb.append(noviCenovnik.getDatumDo()).append(";");

                for (int i = 0; i < noviCenovnik.getClanarine().size(); i++) {
                    ClanarinaStavka cs = noviCenovnik.getClanarine().get(i);
                    sb.append(cs.getTip()).append(",").append(cs.getCena());
                    if (i < noviCenovnik.getClanarine().size() - 1) sb.append("|");
                }
                sb.append(";");

                for (int i = 0; i < noviCenovnik.getKazne().size(); i++) {
                    Kazna k = noviCenovnik.getKazne().get(i);
                    sb.append(k.getOpis()).append(",").append(k.getIznos());
                    if (i < noviCenovnik.getKazne().size() - 1) sb.append("|");
                }
                sb.append(";");

                for (int i = 0; i < noviCenovnik.getUsluge().size(); i++) {
                    Usluga u = noviCenovnik.getUsluge().get(i);
                    sb.append(u.getNaziv()).append(",").append(u.getCena());
                    if (i < noviCenovnik.getUsluge().size() - 1) sb.append("|");
                }

                out.println(sb.toString());
                out.flush();
            }

            JOptionPane.showMessageDialog(this, 
                "Cenovnik uspešno sačuvan!\n\n" +
                "ID: " + id + "\n" +
                "Period: " + datumOd + " do " + datumDo + "\n" +
                "Članarine: " + listaOdabraneClanarine.size() + "\n" +
                "Kazne: " + listaOdabraneKazne.size() + "\n" +
                "Usluge: " + listaOdabraneUsluge.size(),
                "Uspeh", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Greška pri kreiranju cenovnika: " + ex.getMessage(), 
                                        "Greška", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public Cenovnik getNoviCenovnik() {
        return noviCenovnik;
    }
}