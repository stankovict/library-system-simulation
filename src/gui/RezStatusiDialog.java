package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;

public class RezStatusiDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();

   

    public RezStatusiDialog(Dialog owner) {
        setTitle("Status rezervacija (poslednjih 30 dana)");
        setBounds(100, 100, 600, 500);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        try {

            Map<String, Integer> statusCounts = ucitajStatuseRezervacija("podaci/rezervacije_obradjene.csv");
            
            if (statusCounts.isEmpty() || statusCounts.values().stream().allMatch(count -> count == 0)) {

                JLabel label = new JLabel("Nema podataka o rezervacijama za poslednjih 30 dana", SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 16));
                contentPanel.add(label, BorderLayout.CENTER);
            } else {

                PieChart chart = napraviPieChart(statusCounts);
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
    }

    private Map<String, Integer> ucitajStatuseRezervacija(String filePath) throws IOException {
        Map<String, Integer> statusCounts = new HashMap<>();
        LocalDate cutoffDate = LocalDate.now().minusDays(30);

        statusCounts.put("POTVRĐENA", 0);
        statusCounts.put("ODBIJENA", 0);
        statusCounts.put("OTKAZANA", 0);
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(";");

                if (parts.length >= 11) {
                    try {
                        String status = parts[6].trim();
                        String dateStr = parts[10].trim();

                        LocalDate processedDate = parseDate(dateStr);

                        if (processedDate != null && !processedDate.isBefore(cutoffDate)) {

                            if (status.equals("POTVRĐENA") || status.equals("ODBIJENA") || status.equals("OTKAZANA")) {
                                statusCounts.merge(status, 1, Integer::sum);
                                System.out.println("Dodao: " + status + " sa datumom " + processedDate);
                            }
                        }
                        
                    } catch (Exception e) {
                        System.err.println("Greška pri parsiranju linije: " + line + " - " + e.getMessage());
                        continue;
                    }
                }
            }
        }
        
        System.out.println("Ukupni rezultati: " + statusCounts);
        return statusCounts;
    }
    
    private LocalDate parseDate(String dateStr) {

        String[] formats = {"yyyy-MM-dd", "dd.MM.yyyy", "d.M.yyyy", "MM/dd/yyyy"};
        
        for (String format : formats) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {

            }
        }
        
        System.err.println("Ne mogu da parsirам datum: " + dateStr);
        return null;
    }

    private PieChart napraviPieChart(Map<String, Integer> statusCounts) {
        PieChart chart = new PieChartBuilder()
            .width(600)
            .height(500)
            .title("Status rezervacija u poslednjih 30 dana")
            .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setPlotContentSize(0.8);
        chart.getStyler().setStartAngleInDegrees(90);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendBackgroundColor(Color.WHITE);

        Color[] statusColors = {
            new Color(76, 175, 80),
            new Color(244, 67, 54),
            new Color(255, 152, 0)
        };

        int colorIndex = 0;
        int totalCount = statusCounts.values().stream().mapToInt(Integer::intValue).sum();
        
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            String status = entry.getKey();
            Integer count = entry.getValue();
            
            if (count > 0) {

                double percentage = (count * 100.0) / totalCount;
                String displayLabel = String.format("%s (%d - %.1f%%)", 
                    getStatusDisplayName(status), count, percentage);
                
                chart.addSeries(displayLabel, count);

                if (colorIndex < statusColors.length) {
                    chart.getStyler().setSeriesColors(statusColors);
                }
                colorIndex++;
            }
        }

        if (totalCount == 0) {
            chart.addSeries("Nema podataka", 1);
            chart.getStyler().setSeriesColors(new Color[]{Color.LIGHT_GRAY});
        }

        return chart;
    }
    
    private String getStatusDisplayName(String status) {
        switch (status) {
            case "POTVRĐENA":
                return "Potvrđene";
            case "ODBIJENA":
                return "Odbijene";
            case "OTKAZANA":
                return "Otkazane";
            default:
                return status;
        }
    }
}