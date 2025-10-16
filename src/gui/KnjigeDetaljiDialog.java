package gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class KnjigeDetaljiDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTable table;
    private DefaultTableModel tableModel;


    public KnjigeDetaljiDialog(Dialog owner) {
        setTitle("Knjige - Pregled");
        setBounds(100, 100, 800, 500);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout());


        tableModel = new DefaultTableModel(new String[]{"Naziv", "Autor", "Žanr", "ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        ucitajKnjige();

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        JButton btnDetalji = new JButton("Prikaži detalje");
        btnDetalji.addActionListener(e -> prikaziDetalje());
        buttonPane.add(btnDetalji);

        JButton closeButton = new JButton("Zatvori");
        closeButton.addActionListener(e -> dispose());
        buttonPane.add(closeButton);
        getRootPane().setDefaultButton(closeButton);
    }

    private void ucitajKnjige() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream("podaci/knjige.csv"), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length >= 5) {
                    String naziv = delovi[0].trim();
                    String autor = delovi[1].trim();
                    String zanr = delovi[2].trim();
                    String id = delovi[3].trim();


                    tableModel.addRow(new Object[]{naziv, autor, zanr, id});
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Greška pri učitavanju knjiga!", "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prikaziDetalje() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Molimo izaberite knjigu!", "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String naziv = (String) tableModel.getValueAt(selectedRow, 0);
        String autor = (String) tableModel.getValueAt(selectedRow, 1);
        String zanr = (String) tableModel.getValueAt(selectedRow, 2);
        String id = (String) tableModel.getValueAt(selectedRow, 3);

        DetaljiKnjigeDialog detaljiDialog = new DetaljiKnjigeDialog(this, naziv, autor, zanr, id);
        detaljiDialog.setVisible(true);
    }

    class DetaljiKnjigeDialog extends JDialog {
        private JSpinner spinnerOd;
        private JSpinner spinnerDo;
        private JLabel lblBrojIzdavanja;
        private JLabel lblBrojRezervacija;
        private String bookId;

        public DetaljiKnjigeDialog(JDialog parent, String naziv, String autor, String zanr, String id) {
            super(parent, "Detalji knjige", true);
            this.bookId = id;

            setBounds(100, 100, 500, 400);
            getContentPane().setLayout(new BorderLayout(10, 10));

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

            JPanel infoPanel = new JPanel(new GridBagLayout());
            infoPanel.setBorder(new TitledBorder("Informacije o knjizi"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0;
            infoPanel.add(new JLabel("Naziv:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            infoPanel.add(new JLabel(naziv), gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0;
            infoPanel.add(new JLabel("Autor:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            infoPanel.add(new JLabel(autor), gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 0;
            infoPanel.add(new JLabel("Žanr:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            infoPanel.add(new JLabel(zanr), gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.weightx = 0;
            infoPanel.add(new JLabel("ID:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            infoPanel.add(new JLabel(id), gbc);

            mainPanel.add(infoPanel, BorderLayout.NORTH);

            JPanel statistikaPanel = new JPanel(new BorderLayout(10, 10));
            statistikaPanel.setBorder(new TitledBorder("Statistika"));

            JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            datePanel.add(new JLabel("Period od:"));
            
            spinnerOd = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor editorOd = new JSpinner.DateEditor(spinnerOd, "yyyy-MM-dd");
            spinnerOd.setEditor(editorOd);
            datePanel.add(spinnerOd);

            datePanel.add(new JLabel("do:"));
            
            spinnerDo = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor editorDo = new JSpinner.DateEditor(spinnerDo, "yyyy-MM-dd");
            spinnerDo.setEditor(editorDo);
            datePanel.add(spinnerDo);

            JButton btnPrikazi = new JButton("Prikaži statistiku");
            btnPrikazi.addActionListener(e -> izracunajStatistiku());
            datePanel.add(btnPrikazi);

            statistikaPanel.add(datePanel, BorderLayout.NORTH);

            JPanel rezultatiPanel = new JPanel(new GridBagLayout());
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0;
            gbc.gridy = 0;
            rezultatiPanel.add(new JLabel("Broj izdavanja:"), gbc);
            gbc.gridx = 1;
            lblBrojIzdavanja = new JLabel("--");
            rezultatiPanel.add(lblBrojIzdavanja, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            rezultatiPanel.add(new JLabel("Broj rezervacija:"), gbc);
            gbc.gridx = 1;
            lblBrojRezervacija = new JLabel("--");
            rezultatiPanel.add(lblBrojRezervacija, gbc);

            statistikaPanel.add(rezultatiPanel, BorderLayout.CENTER);

            mainPanel.add(statistikaPanel, BorderLayout.CENTER);

            getContentPane().add(mainPanel, BorderLayout.CENTER);

            JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeButton = new JButton("Zatvori");
            closeButton.addActionListener(e -> dispose());
            buttonPane.add(closeButton);
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
        }

        private void izracunajStatistiku() {
            java.util.Date dateOd = (java.util.Date) spinnerOd.getValue();
            java.util.Date dateDo = (java.util.Date) spinnerDo.getValue();

            LocalDate od = new java.sql.Date(dateOd.getTime()).toLocalDate();
            LocalDate do_ = new java.sql.Date(dateDo.getTime()).toLocalDate();

            int brojIzdavanja = prebrojIzdavanja(od, do_);
            int brojRezervacija = prebrojRezervacije(od, do_);

            lblBrojIzdavanja.setText(String.valueOf(brojIzdavanja));
            lblBrojRezervacija.setText(String.valueOf(brojRezervacija));
        }

        private int prebrojIzdavanja(LocalDate od, LocalDate do_) {
            int count = 0;
            DateTimeFormatter[] formateri = {
                DateTimeFormatter.ofPattern("d.M.yyyy"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            };

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream("podaci/izdavanje.csv"), StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    String[] delovi = line.split(";");
                    if (delovi.length >= 5) {
                        String idKnjige = delovi[0].trim();
                        String datumStr = delovi[4].trim();

                        if (!idKnjige.equals(bookId)) {
                            continue;
                        }

                        LocalDate datum = null;
                        for (DateTimeFormatter formatter : formateri) {
                            try {
                                datum = LocalDate.parse(datumStr, formatter);
                                break;
                            } catch (Exception ignored) {}
                        }

                        if (datum != null && !datum.isBefore(od) && !datum.isAfter(do_)) {
                            count++;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return count;
        }

        private int prebrojRezervacije(LocalDate od, LocalDate do_) {
            int count = 0;
            DateTimeFormatter[] formateri = {
                DateTimeFormatter.ofPattern("d.M.yyyy"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            };

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream("podaci/rezervacije_obradjene.csv"), StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    String[] delovi = line.split(";");
                    if (delovi.length >= 11) {
                        String idKnjige = delovi[2].trim();
                        String status = delovi[6].trim();
                        String datumStr = delovi[10].trim();

                        if (!idKnjige.equals(bookId) || !status.equals("POTVRĐENA")) {
                            continue;
                        }

                        LocalDate datum = null;
                        for (DateTimeFormatter formatter : formateri) {
                            try {
                                datum = LocalDate.parse(datumStr, formatter);
                                break;
                            } catch (Exception ignored) {}
                        }

                        if (datum != null && !datum.isBefore(od) && !datum.isAfter(do_)) {
                            count++;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return count;
        }
    }
}