package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class PlateDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTable tabela;
    private JLabel lblUkupno;
    private DefaultTableModel tableModel;

    public PlateDialog() {
        setTitle("Isplata plata");
        setBounds(100, 100, 500, 400);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Ime", "Prezime", "Plata"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tabela);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        ucitajZaposlene();

        double suma = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            suma += (double) tableModel.getValueAt(i, 2);
        }

        lblUkupno = new JLabel("Ukupno za isplatu: " + suma + " RSD");
        contentPanel.add(lblUkupno, BorderLayout.SOUTH);

        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        JButton btnIsplati = new JButton("Reguliši plate");
        double finalSuma = suma;
        btnIsplati.addActionListener(e -> {
            LocalDate danas = LocalDate.now();

            if (plateVecIsplaceneOvajMesec(danas)) {
                JOptionPane.showMessageDialog(this, "Plate su već isplaćene za ovaj mesec!", "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("podaci/plate.csv", true), StandardCharsets.UTF_8))) {
                bw.write(finalSuma + ";" + danas);
                bw.newLine();
                JOptionPane.showMessageDialog(this, "Plate uspešno isplaćene!", "Uspeh", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Greška pri upisu u fajl: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
        });
        buttonPane.add(btnIsplati);

        JButton btnOtkazi = new JButton("Otkaži");
        btnOtkazi.addActionListener(e -> dispose());
        buttonPane.add(btnOtkazi);
    }

    private void ucitajZaposlene() {
        tableModel.setRowCount(0);
        ucitaj("podaci/admini.csv");
        ucitaj("podaci/bibliotekari.csv");
    }

    private void ucitaj(String fajl) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajl), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] delovi = line.split(";");
                if (delovi.length > 10) {
                    String ime = delovi[0].trim();
                    String prezime = delovi[1].trim();
                    double plata = Double.parseDouble(delovi[10].trim());
                    tableModel.addRow(new Object[]{ime, prezime, plata});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Greška pri čitanju fajla " + fajl + ": " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Greška u formatu plate u fajlu " + fajl + ": " + e.getMessage());
        }
    }

    private boolean plateVecIsplaceneOvajMesec(LocalDate danas) {
        File file = new File("podaci/plate.csv");
        if (!file.exists()) return false;

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
            	String[] delovi = line.split(";");
            	if (delovi.length == 2) {
            	    LocalDate datum = null;
            	    String[] patterns = {"d.M.yyyy", "dd.MM.yyyy", "yyyy-MM-dd"};
            	    
            	    for (String p : patterns) {
            	        try {
            	            DateTimeFormatter f = DateTimeFormatter.ofPattern(p);
            	            datum = LocalDate.parse(delovi[1].trim(), f);
            	            break;
            	        } catch (Exception ignored) {}
            	    }

            	    if (datum == null) {
            	        throw new IllegalArgumentException("Nepoznat format datuma: " + delovi[1]);
            	    }

            	    if (datum.getMonth() == danas.getMonth() && datum.getYear() == danas.getYear()) {
            	        return true;
            	    }
            	}

            }
        } catch (IOException | RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Greška pri proveri prethodnih isplata: " + e.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }
}
