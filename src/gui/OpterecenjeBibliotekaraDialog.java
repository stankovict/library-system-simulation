package gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;

public class OpterecenjeBibliotekaraDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();

    public OpterecenjeBibliotekaraDialog(Dialog owner) {
        super(owner, "Opterećenje bibliotekara (poslednjih 30 dana)", true);
        setBounds(100, 100, 700, 500);
        setLocationRelativeTo(owner);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        try {

            Map<String, Integer> brojPoBibliotekaru = ucitajPodatkeZaPoslednje30Dana();
            
            if (brojPoBibliotekaru.isEmpty()) {
                JLabel label = new JLabel("Nema podataka o radu bibliotekara za poslednjih 30 dana", SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 16));
                contentPanel.add(label, BorderLayout.CENTER);
            } else {

                PieChart chart = napraviPieChart(brojPoBibliotekaru);
                XChartPanel<PieChart> chartPanel = new XChartPanel<>(chart);
                contentPanel.add(chartPanel, BorderLayout.CENTER);
            }
            
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("<html><div style='text-align: center;'>" +
                    "Greška pri učitavanju podataka:<br>" +
                    e.getMessage() + "</div></html>", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            contentPanel.add(errorLabel, BorderLayout.CENTER);
            e.printStackTrace();
        }

        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnZatvori = new JButton("Zatvori");
        btnZatvori.addActionListener(e -> dispose());
        buttonPane.add(btnZatvori);
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
    }

    private Map<String, Integer> ucitajPodatkeZaPoslednje30Dana() throws IOException {
        Map<String, Integer> brojPoBibliotekaru = new HashMap<>();
        LocalDate cutoffDate = LocalDate.now().minusDays(30);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream("podaci/rezervacije_obradjene.csv"), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] delovi = line.split(";");
                if (delovi.length < 11) continue;
                
                String datumObradeStr = delovi[10].trim();
                if (datumObradeStr.isEmpty()) continue;

                LocalDate datumObrade = parseDate(datumObradeStr);
                if (datumObrade == null) continue;

                if (!datumObrade.isBefore(cutoffDate)) {
                    String bibliotekar = delovi[8].trim();

                    if (!bibliotekar.equalsIgnoreCase("placeholder") && !bibliotekar.isEmpty()) {
                        brojPoBibliotekaru.merge(bibliotekar, 1, Integer::sum);
                        System.out.println("Dodao: " + bibliotekar + " za datum " + datumObrade);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            throw new IOException("Fajl rezervacije_obradjene.csv nije pronađen");
        } catch (IOException e) {
            throw new IOException("Greška pri čitanju fajla: " + e.getMessage());
        }

        System.out.println("Ukupni rezultati: " + brojPoBibliotekaru);
        return brojPoBibliotekaru;
    }

    private LocalDate parseDate(String dateStr) {
        String[] patterns = {"yyyy-MM-dd", "dd.MM.yyyy", "d.M.yyyy", "MM/dd/yyyy"};
        
        for (String pattern : patterns) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {

            }
        }
        
        System.err.println("Ne mogu da parsiram datum: " + dateStr);
        return null;
    }

    private PieChart napraviPieChart(Map<String, Integer> brojPoBibliotekaru) {
        PieChart chart = new PieChartBuilder()
            .width(700)
            .height(500)
            .title("Opterećenje bibliotekara u poslednjih 30 dana")
            .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setPlotContentSize(0.8);
        chart.getStyler().setStartAngleInDegrees(90);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendBackgroundColor(Color.WHITE);

        Color[] librarianColors = {
            new Color(70, 130, 180),   
            new Color(255, 140, 0),
            new Color(60, 179, 113),
            new Color(220, 20, 60),
            new Color(138, 43, 226),
            new Color(255, 215, 0),
            new Color(30, 144, 255),
            new Color(255, 69, 0),
            new Color(50, 205, 50),
            new Color(199, 21, 133)
        };

        chart.getStyler().setSeriesColors(librarianColors);

        int totalReservations = brojPoBibliotekaru.values().stream().mapToInt(Integer::intValue).sum();
        
        for (Map.Entry<String, Integer> entry : brojPoBibliotekaru.entrySet()) {
            String bibliotekar = entry.getKey();
            Integer count = entry.getValue();

            double percentage = (count * 100.0) / totalReservations;

            String displayLabel = String.format("%s (%d - %.1f%%)", bibliotekar, count, percentage);
            
            chart.addSeries(displayLabel, count);
        }

        return chart;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            OpterecenjeBibliotekaraDialog dialog = new OpterecenjeBibliotekaraDialog(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        });
    }
}