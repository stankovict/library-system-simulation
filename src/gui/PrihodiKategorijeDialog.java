package gui;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PrihodiKategorijeDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();

    public static Map<String, String> emailKategorija = new HashMap<>();
    
    private static final String[] MESECI_SRPSKI = {
        "Jan", "Feb", "Mar", "Apr", "Maj", "Jun",
        "Jul", "Avg", "Sep", "Okt", "Nov", "Dec"
    };

    public PrihodiKategorijeDialog(Dialog owner) {
        setTitle("Prihodi po kategoriji (12 meseci)");
        setBounds(100, 100, 800, 600);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        try {
            ucitajKategorije("podaci/clanovi.csv");
            System.out.println("Učitano kategorija: " + emailKategorija.size());

            Map<String, Map<String, Double>> prihodi = ucitajPrihode("podaci/prihodi.csv");
            System.out.println("Učitano kategorija sa prihodima: " + prihodi.size());

            if (prihodi.isEmpty()) {
                JLabel label = new JLabel("Nema podataka o prihodima za prikaz", SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 16));
                contentPanel.add(label, BorderLayout.CENTER);
            } else {
                XYChart chart = napraviLineChart(prihodi);
                XChartPanel<XYChart> chartPanel = new XChartPanel<>(chart);
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

    public void ucitajKategorije(String path) {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(";");
                
                if (parts.length >= 10) {
                    String email = parts[6].trim();
                    String kategorija = parts[8].trim();

                    if (kategorija.isEmpty()) {
                        kategorija = "BEZ_KATEGORIJE";
                    }
                    
                    emailKategorija.put(email, kategorija);
                    System.out.println("Dodao: " + email + " -> " + kategorija);
                }
            }
        } catch (Exception e) {
            System.err.println("Greška pri učitavanju kategorija: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Map<String, Map<String, Double>> ucitajPrihode(String path) {
        Map<String, Map<String, Double>> data = new HashMap<>();

        LocalDate startDate = LocalDate.of(2024, 10, 1);
        LocalDate endDate = LocalDate.now();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length < 3) continue;

                try {
                    String email = parts[0].trim();
                    String amountStr = parts[1].trim().replace(",", ".");
                    double amount = Double.parseDouble(amountStr);
                    LocalDate date = null;
                    String[] patterns = {"d.M.yyyy", "dd.MM.yyyy","yyyy-MM-dd"};
                    for (String p : patterns) {
                        try {
                            java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                            date = LocalDate.parse(parts[2].trim(),f);
                            break;
                        } catch (Exception ignored) {}
                    }

                    if (date == null) continue;

                    if (date.isBefore(startDate) || date.isAfter(endDate)) continue;

                    String kategorija = emailKategorija.get(email);
                    System.out.println(email);
                    System.out.println(kategorija);
                    if (kategorija == null) {
                        System.out.println(emailKategorija.get(email));
                        System.out.println(emailKategorija.get(kategorija));
                        System.out.println("Nema kategorije za email: " + email);
                        continue;
                    }

                    String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

                    data.putIfAbsent(kategorija, new HashMap<>());
                    data.get(kategorija).merge(monthKey, amount, Double::sum);
                    
                    System.out.println("Dodao prihod: " + kategorija + " " + monthKey + " " + amount);
                } catch (Exception e) {
                    System.err.println("Greška pri parsiranju linije: " + line + " - " + e.getMessage());
                    continue;
                }
            }
        } catch (Exception e) {
            System.err.println("Greška pri učitavanju prihoda: " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }

    private static XYChart napraviLineChart(Map<String, Map<String, Double>> prihodi) {

        List<String> months = new ArrayList<>();
        List<String> monthLabels = new ArrayList<>();
        
        LocalDate startDate = LocalDate.of(2024, 10, 1);
        LocalDate endDate = LocalDate.now();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            months.add(current.getYear() + "-" + String.format("%02d", current.getMonthValue()));

            int monthIndex = current.getMonthValue() - 1;
            String monthLabel = MESECI_SRPSKI[monthIndex] + " " + current.getYear();
            monthLabels.add(monthLabel);
            
            current = current.plusMonths(1);
        }

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Prihodi po kategoriji člana (Okt 2024 - Okt 2025)")
                .xAxisTitle("Meseci")
                .yAxisTitle("Iznos (RSD)")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setPlotGridLinesColor(Color.LIGHT_GRAY);
        chart.getStyler().setXAxisLabelRotation(45);

        Map<Double, Object> customLabels = new HashMap<>();
        for (int i = 0; i < monthLabels.size(); i++) {
            customLabels.put((double) i, monthLabels.get(i));
        }
        chart.setCustomXAxisTickLabelsFormatter(x -> {
            Object label = customLabels.get(x);
            return label != null ? label.toString() : "";
        });

        Color[] categoryColors = {
            new Color(70, 130, 180),
            new Color(255, 140, 0),
            new Color(128, 128, 128),
            new Color(255, 215, 0),
            new Color(0, 0, 255),
            new Color(0, 128, 0),
            new Color(255, 0, 255),
            new Color(255, 20, 147),
            new Color(0, 191, 255),
            new Color(50, 205, 50)
        };

        List<Integer> xValues = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            xValues.add(i);
        }

        boolean hasData = false;
        int colorIndex = 0;
        
        for (String kategorija : prihodi.keySet()) {
            Map<String, Double> katMap = prihodi.get(kategorija);
            List<Double> values = months.stream()
                    .map(m -> katMap.getOrDefault(m, 0.0))
                    .collect(Collectors.toList());

            boolean hasNonZeroValues = values.stream().anyMatch(v -> v > 0);
            
            if (hasNonZeroValues || kategorija.equals("BEZ_KATEGORIJE")) {
                String displayName = kategorija;
                if (kategorija.equals("BEZ_KATEGORIJE")) {
                    displayName = "Bez kategorije";
                } else {
                    displayName = kategorija.toLowerCase().replace("_", " ");
                    displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
                }
                
                XYSeries series = chart.addSeries(displayName, xValues, values);

                if (colorIndex < categoryColors.length) {
                    series.setMarkerColor(categoryColors[colorIndex]);
                    series.setLineColor(categoryColors[colorIndex]);
                }

                series.setMarker(SeriesMarkers.CIRCLE);
                
                hasData = true;
                colorIndex++;
                
                System.out.println("Dodao line seriju: " + displayName + " sa vrednostima: " + values);
            }
        }

        if (!hasData) {
            List<Double> zeroValues = Collections.nCopies(months.size(), 0.0);
            XYSeries series = chart.addSeries("Nema podataka", xValues, zeroValues);
            series.setLineColor(Color.GRAY);
            series.setMarkerColor(Color.GRAY);
        }

        return chart;
    }
}