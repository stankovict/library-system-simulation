package gui;

import enumi.Clanarina;

import manager.CenovnikMetode;
import modeli.ClanarinaModel;
import modeli.Kazna;
import modeli.Usluga;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DodajStavkeDialog extends JDialog {

    private String kazneFajl;
    private String uslugeFajl;
    private String clanarineFajl;

    public DodajStavkeDialog(JFrame parent, String kazneFajl, String uslugeFajl, String clanarineFajl) {
        super(parent, "Dodavanje kazni, usluga i clanarina", true);
        this.kazneFajl = kazneFajl;
        this.uslugeFajl = uslugeFajl;
        this.clanarineFajl = clanarineFajl;

        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.add("Kazne", napraviPanelKazne());
        tabbedPane.add("Usluge", napraviPanelUsluga());
        tabbedPane.add("Članarine", napraviPanelClanarine());

        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Zatvori");
        btnOk.addActionListener(e -> dispose());
        buttons.add(btnOk);
        add(buttons, BorderLayout.SOUTH);
    }

    private JPanel napraviPanelKazne() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<Kazna> model = new DefaultListModel<>();
        List<Kazna> lista = CenovnikMetode.ucitajKazne(kazneFajl);
        lista.forEach(model::addElement);

        JList<Kazna> jList = new JList<>(model);
        panel.add(new JScrollPane(jList), BorderLayout.CENTER);

        JButton btnDodaj = new JButton("Dodaj kaznu");
        btnDodaj.addActionListener(e -> {
            String opis = JOptionPane.showInputDialog(this, "Opis kazne:");
            if (opis == null || opis.trim().isEmpty()) return;
            
            String iznosStr = JOptionPane.showInputDialog(this, "Iznos:");
            if (iznosStr == null || iznosStr.trim().isEmpty()) return;
            
            try {
                double iznos = Double.parseDouble(iznosStr);
                Kazna k = new Kazna(opis, iznos);
                model.addElement(k);
                lista.add(k);
                CenovnikMetode.sacuvajKazne(kazneFajl, lista);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neispravan iznos!");
            }
        });
        panel.add(btnDodaj, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel napraviPanelUsluga() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<Usluga> model = new DefaultListModel<>();
        List<Usluga> lista = CenovnikMetode.ucitajUsluge(uslugeFajl);
        lista.forEach(model::addElement);

        JList<Usluga> jList = new JList<>(model);
        panel.add(new JScrollPane(jList), BorderLayout.CENTER);

        JButton btnDodaj = new JButton("Dodaj uslugu");
        btnDodaj.addActionListener(e -> {
            String naziv = JOptionPane.showInputDialog(this, "Naziv usluge:");
            if (naziv == null || naziv.trim().isEmpty()) return;
            
            String cenaStr = JOptionPane.showInputDialog(this, "Cena:");
            if (cenaStr == null || cenaStr.trim().isEmpty()) return;
            
            try {
                double cena = Double.parseDouble(cenaStr);
                Usluga u = new Usluga(naziv, cena);
                model.addElement(u);
                lista.add(u);
                CenovnikMetode.sacuvajUsluge(uslugeFajl, lista);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neispravna cena!");
            }
        });
        panel.add(btnDodaj, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel napraviPanelClanarine() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultListModel<String> model = new DefaultListModel<>();
        List<ClanarinaModel> clanarine = CenovnikMetode.ucitajClanarine(clanarineFajl);

        for (ClanarinaModel c : clanarine) {
            model.addElement(c.getTip().toString() + " - " + c.getIznos() + " RSD");
        }

        JList<String> jList = new JList<>(model);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(jList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        
        JButton btnAzuriraj = new JButton("Ažuriraj cenu članarine");
        btnAzuriraj.addActionListener(e -> {

            Clanarina[] opcije = Clanarina.values();
            Clanarina izabrana = (Clanarina) JOptionPane.showInputDialog(
                this,
                "Izaberite tip članarine:",
                "Tip članarine",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcije,
                opcije[0]
            );
            
            if (izabrana == null) return;

            String trenutnaCena = "0.0";
            for (ClanarinaModel c : clanarine) {
                if (c.getTip().equals(izabrana)) {
                    trenutnaCena = String.valueOf(c.getIznos());
                    break;
                }
            }

            String novaCenaStr = JOptionPane.showInputDialog(
                this, 
                "Nova cena za " + izabrana.toString().toLowerCase() + " članarinu:",
                trenutnaCena
            );
            
            if (novaCenaStr == null || novaCenaStr.trim().isEmpty()) return;
            
            try {
                double novaCena = Double.parseDouble(novaCenaStr);
                if (novaCena < 0) {
                    JOptionPane.showMessageDialog(this, "Cena ne može biti negativna!");
                    return;
                }

                boolean pronadjena = false;
                for (ClanarinaModel c : clanarine) {
                    if (c.getTip().equals(izabrana)) {
                        c.setIznos(novaCena);
                        pronadjena = true;
                        break;
                    }
                }

                if (!pronadjena) {
                    clanarine.add(new ClanarinaModel(izabrana, novaCena));
                }

                CenovnikMetode.sacuvajClanarine(clanarineFajl, clanarine);

                model.clear();
                for (ClanarinaModel c : clanarine) {
                    model.addElement(c.getTip().toString() + " - " + c.getIznos() + " RSD");
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Cena za " + izabrana.toString().toLowerCase() + " članarinu je ažurirana na " + novaCena + " RSD");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neispravna cena!");
            }
        });
        
        bottomPanel.add(btnAzuriraj);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Napomene"));
        JLabel infoLabel = new JLabel("<html><b>Dostupni tipovi članarina:</b><br/>" +
            "• MESEČNA - mesečna članarina<br/>" +
            "• GODIŠNJA - godišnja članarina</html>");
        infoPanel.add(infoLabel);
        panel.add(infoPanel, BorderLayout.NORTH);

        return panel;
    }
}