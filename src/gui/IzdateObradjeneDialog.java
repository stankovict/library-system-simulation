package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class IzdateObradjeneDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;
    private JSpinner spinnerDatum;
    private JButton btnRezervacije;
    private JButton btnIzdavanje;

    public IzdateObradjeneDialog(JFrame parent) {
        super(parent, "Obradjene rezervacije i izdate knjige", true);
        setSize(700, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        spinnerDatum = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerDatum, "dd.MM.yyyy");
        spinnerDatum.setEditor(dateEditor);

        btnRezervacije = new JButton("Pregled rezervacija");
        btnRezervacije.addActionListener(e -> prikaziRezervacije());

        btnIzdavanje = new JButton("Pregled izdatih knjiga");
        btnIzdavanje.addActionListener(e -> prikaziIzdavanje());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Datum:"));
        topPanel.add(spinnerDatum);
        topPanel.add(btnRezervacije);
        topPanel.add(btnIzdavanje);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Tip", "Korisnik", "Naslov", "Datum obrade/izdavanja"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private LocalDate getIzabraniDatum() {
        java.util.Date spinnerDate = (java.util.Date) spinnerDatum.getValue();
        return LocalDate.of(spinnerDate.getYear() + 1900,
                            spinnerDate.getMonth() + 1,
                            spinnerDate.getDate());
    }

    private void prikaziRezervacije() {
        tableModel.setRowCount(0);
        LocalDate datum = getIzabraniDatum();
        LocalDate datumRez = null;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream("podaci/rezervacije_obradjene.csv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length > 10) {
                    String datumStr = delovi[10].trim();
                    String[] formats = {"yyyy-MM-dd", "dd.MM.yyyy", "d.M.yyyy", "MM/dd/yyyy"};
                    
                    for (String format : formats) {
                        try {
                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(format);
                            datumRez = LocalDate.parse(datumStr, formatter);
                        } catch (DateTimeParseException e) {

                        }
                    }
                    if (datumRez.equals(datum)) {
                    	String id = delovi[0].trim();
                        String korisnik = delovi[1].trim();
                        String naslov = delovi[3].trim();
                        tableModel.addRow(new Object[]{"Rezervacija", korisnik, naslov, datumStr});
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Greška pri čitanju rezervacija: " + ex.getMessage());
        }
    }

    private void prikaziIzdavanje() {
        tableModel.setRowCount(0);
        LocalDate datum = getIzabraniDatum();
        LocalDate datumIzd = null;


        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream("podaci/izdavanje.csv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length > 3) {
                    String datumStr = delovi[4].trim();
                    String[] formats = {"yyyy-MM-dd", "dd.MM.yyyy", "d.M.yyyy", "MM/dd/yyyy"};
                    
                    for (String format : formats) {
                        try {
                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(format);
                            datumIzd = LocalDate.parse(datumStr, formatter);
                        } catch (DateTimeParseException e) {

                        }
                    }
                    if (datumIzd.equals(datum)) {
                        String idKnjige = delovi[0].trim();
                        String naslov = delovi[1].trim();
                        String clan = delovi[2].trim();
                        tableModel.addRow(new Object[]{"Izdavanje", clan, naslov, datumIzd});
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Greška pri čitanju izdatih knjiga: " + ex.getMessage());
        }
    }
}
