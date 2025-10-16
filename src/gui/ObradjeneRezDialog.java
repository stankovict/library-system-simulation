package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

public class ObradjeneRezDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JSpinner spinnerOd;
    private JSpinner spinnerDo;

    public ObradjeneRezDialog(Dialog owner) {
        super(owner, "Obrađene rezervacije", true);
        setBounds(100, 100, 400, 200);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(null);

        JLabel lblOd = new JLabel("Datum od:");
        lblOd.setBounds(20, 20, 80, 25);
        contentPanel.add(lblOd);

        spinnerOd = new JSpinner(new SpinnerDateModel());
        spinnerOd.setEditor(new JSpinner.DateEditor(spinnerOd, "yyyy-MM-dd"));
        spinnerOd.setBounds(120, 20, 150, 25);
        contentPanel.add(spinnerOd);

        JLabel lblDo = new JLabel("Datum do:");
        lblDo.setBounds(20, 60, 80, 25);
        contentPanel.add(lblDo);

        spinnerDo = new JSpinner(new SpinnerDateModel());
        spinnerDo.setEditor(new JSpinner.DateEditor(spinnerDo, "yyyy-MM-dd"));
        spinnerDo.setBounds(120, 60, 150, 25);
        contentPanel.add(spinnerDo);

        JButton btnGenerisi = new JButton("Generiši izveštaj");
        btnGenerisi.setBounds(120, 96, 150, 30);
        contentPanel.add(btnGenerisi);

        btnGenerisi.addActionListener(e -> {
            LocalDate datumOd = ((java.util.Date) spinnerOd.getValue())
                    .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate datumDo = ((java.util.Date) spinnerDo.getValue())
                    .toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            int ukupno = 0, potvrdjene = 0, odbijene = 0, otkazane = 0;

            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("d.M.yyyy");

            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                            new java.io.FileInputStream("podaci/rezervacije_obradjene.csv"),
                            java.nio.charset.StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    String[] delovi = line.split(";");
                    if (delovi.length <= 10) continue;

                    String datumStr = delovi[10].trim();
                    if (datumStr.isEmpty()) continue;

                    LocalDate datumObrade = null;

                 String[] patterns = {"d.M.yyyy", "dd.MM.yyyy", "yyyy-MM-dd"};
                 for (String p : patterns) {
                     try {
                         java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                         datumObrade = LocalDate.parse(datumStr, f);
                         break;
                     } catch (Exception ignored) {}
                 }

                 if (datumObrade == null) {
                     System.out.println("Neprepoznat datum: " + datumStr);
                     continue;
                 }

                    if (!datumObrade.isBefore(datumOd) && !datumObrade.isAfter(datumDo)) {
                        ukupno++;
                        String status = delovi[6].trim().toUpperCase();
                        switch (status) {
                            case "POTVRĐENA":
                                potvrdjene++;
                                break;
                            case "ODBIJENA":
                                odbijene++;
                                break;
                            case "OTKAZANA":
                                otkazane++;
                                break;
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Greška pri čitanju fajla!");
                return;
            }

            String poruka = String.format(
                    "Za datume od %s do %s:\n" +
                    "Obrađeno ukupno: %d\n" +
                    "Potvrđene: %d\n" +
                    "Odbijene: %d\n" +
                    "Otkazane: %d",
                    datumOd, datumDo, ukupno, potvrdjene, odbijene, otkazane
            );

            JOptionPane.showMessageDialog(this, poruka);
        });


        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        JButton btnZatvori = new JButton("Zatvori");
        btnZatvori.addActionListener(e -> dispose());
        buttonPane.add(btnZatvori);
        
        
    }
}
