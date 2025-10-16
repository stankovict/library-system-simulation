package gui;

import manager.CenovnikMetode;
import modeli.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CenovniciDialog extends JDialog {

    private JComboBox<Cenovnik> comboCenovnici;
    private JTable tableStavke;
    
    private List<Cenovnik> cenovnici;

    public CenovniciDialog(JFrame parent, String fajlCenovnika) {
        super(parent, "Pregled cenovnika", true);
        setSize(700, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());


        cenovnici = CenovnikMetode.ucitajCenovnike(fajlCenovnika);

        comboCenovnici = new JComboBox<>(cenovnici.toArray(new Cenovnik[0]));
        add(comboCenovnici, BorderLayout.NORTH);


        tableStavke = new JTable();
        add(new JScrollPane(tableStavke), BorderLayout.CENTER);


        JButton btnClose = new JButton("Zatvori");
        btnClose.addActionListener(e -> dispose());
        JPanel panelSouth = new JPanel();
        panelSouth.add(btnClose);
        add(panelSouth, BorderLayout.SOUTH);

        comboCenovnici.addActionListener(e -> prikaziStavke());

        if (!cenovnici.isEmpty()) {
            comboCenovnici.setSelectedIndex(0);
            prikaziStavke();
        }
    }

    private void prikaziStavke() {
        Cenovnik c = (Cenovnik) comboCenovnici.getSelectedItem();
        if (c == null) return;

        String[] kolone = {"Tip", "Naziv / Opis", "Vrednost"};
        DefaultTableModel model = new DefaultTableModel(kolone, 0);


        for (ClanarinaStavka cs : c.getClanarine()) {
            model.addRow(new Object[]{"Članarina", cs.getTip(), cs.getCena() + " RSD"});
        }


        for (Kazna k : c.getKazne()) {
            String[] stavke1 = k.getOpis().split("\\|");
            for (String s1 : stavke1) {
                String[] delovi1 = s1.split(",");
                if (delovi1.length == 2) {
                    String naziv = delovi1[0];
                    String cena = delovi1[1] + " RSD";
                    model.addRow(new Object[]{"Kazna", naziv, cena});

                } else {

                    model.addRow(new Object[]{"Kazna", s1, k.getIznos() + " RSD"});
                }
            }
        }
            


        for (Usluga u : c.getUsluge()) {
            String[] stavke = u.getNaziv().split("\\|");
            for (String s : stavke) {
                String[] delovi = s.split(",");
                if (delovi.length == 2) {
                    String naziv = delovi[0];
                    String cena = delovi[1] + " RSD";
                    model.addRow(new Object[]{"Usluga", naziv, cena});

                } else {

                    model.addRow(new Object[]{"Usluga", s, u.getCena() + " RSD"});
                }
            }
        }

        tableStavke.setModel(model);
    }


 
        
    }

