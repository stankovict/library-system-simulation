package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class PrihodiRashodiDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JSpinner dateFromSpinner;
    private JSpinner dateToSpinner;
    private JLabel lblTotalRevenue;
    private JLabel lblTotalExpenses;
    private JLabel lblNetProfit;
    private JLabel lblRevenueDetails;
    private JLabel lblExpenseDetails;

    public static void main(String[] args) {
        try {
            PrihodiRashodiDialog dialog = new PrihodiRashodiDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PrihodiRashodiDialog() {
        setTitle("Pregled prihoda i rashoda");
        setBounds(100, 100, 600, 500);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();

        calculateFinancials();
    }

    private void initializeComponents() {
 
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        java.util.Date startDate = java.sql.Date.valueOf(startOfMonth);
        java.util.Date endDate = java.sql.Date.valueOf(endOfMonth);
        
        dateFromSpinner = new JSpinner(new SpinnerDateModel(startDate, null, null, java.util.Calendar.DAY_OF_MONTH));
        dateToSpinner = new JSpinner(new SpinnerDateModel(endDate, null, null, java.util.Calendar.DAY_OF_MONTH));

        JSpinner.DateEditor dateFromEditor = new JSpinner.DateEditor(dateFromSpinner, "dd.MM.yyyy");
        JSpinner.DateEditor dateToEditor = new JSpinner.DateEditor(dateToSpinner, "dd.MM.yyyy");
        dateFromSpinner.setEditor(dateFromEditor);
        dateToSpinner.setEditor(dateToEditor);

        lblTotalRevenue = new JLabel("0.00 RSD");
        lblTotalExpenses = new JLabel("0.00 RSD");
        lblNetProfit = new JLabel("0.00 RSD");
        lblRevenueDetails = new JLabel("Sredstva: 0.00 RSD, Izdavanje: 0.00 RSD");
        lblExpenseDetails = new JLabel("Plate: 0.00 RSD");

        lblTotalRevenue.setFont(new Font("Dialog", Font.BOLD, 14));
        lblTotalRevenue.setForeground(new Color(0, 128, 0));
        
        lblTotalExpenses.setFont(new Font("Dialog", Font.BOLD, 14));
        lblTotalExpenses.setForeground(new Color(200, 0, 0));
        
        lblNetProfit.setFont(new Font("Dialog", Font.BOLD, 16));
        
        lblRevenueDetails.setFont(new Font("Dialog", Font.ITALIC, 12));
        lblExpenseDetails.setFont(new Font("Dialog", Font.ITALIC, 12));
    }

    private void setupLayout() {
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel datePanel = new JPanel(new GridBagLayout());
        datePanel.setBorder(new TitledBorder("Izaberite period"));
        
        GridBagConstraints datePanelGbc = new GridBagConstraints();
        datePanelGbc.insets = new Insets(5, 5, 5, 5);

        datePanelGbc.gridx = 0; datePanelGbc.gridy = 0;
        datePanelGbc.anchor = GridBagConstraints.WEST;
        datePanel.add(new JLabel("Od:"), datePanelGbc);
        
        datePanelGbc.gridx = 1;
        datePanelGbc.fill = GridBagConstraints.HORIZONTAL;
        datePanelGbc.weightx = 1.0;
        datePanel.add(dateFromSpinner, datePanelGbc);

        datePanelGbc.gridx = 0; datePanelGbc.gridy = 1;
        datePanelGbc.fill = GridBagConstraints.NONE;
        datePanelGbc.weightx = 0.0;
        datePanel.add(new JLabel("Do:"), datePanelGbc);
        
        datePanelGbc.gridx = 1;
        datePanelGbc.fill = GridBagConstraints.HORIZONTAL;
        datePanelGbc.weightx = 1.0;
        datePanel.add(dateToSpinner, datePanelGbc);

        JButton btnCalculate = new JButton("Izračunaj");
        btnCalculate.addActionListener(e -> calculateFinancials());
        datePanelGbc.gridx = 0; datePanelGbc.gridy = 2;
        datePanelGbc.gridwidth = 2;
        datePanelGbc.fill = GridBagConstraints.NONE;
        datePanelGbc.anchor = GridBagConstraints.CENTER;
        datePanelGbc.weightx = 0.0;
        datePanel.add(btnCalculate, datePanelGbc);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(datePanel, gbc);

        JPanel resultsPanel = new JPanel(new GridBagLayout());
        resultsPanel.setBorder(new TitledBorder("Rezultati"));
        
        GridBagConstraints resultGbc = new GridBagConstraints();
        resultGbc.insets = new Insets(8, 10, 8, 10);
        resultGbc.anchor = GridBagConstraints.WEST;

        resultGbc.gridx = 0; resultGbc.gridy = 0;
        resultsPanel.add(new JLabel("UKUPNI PRIHODI:"), resultGbc);
        
        resultGbc.gridx = 1;
        resultGbc.anchor = GridBagConstraints.EAST;
        resultsPanel.add(lblTotalRevenue, resultGbc);
        
        resultGbc.gridx = 0; resultGbc.gridy = 1;
        resultGbc.gridwidth = 2;
        resultGbc.anchor = GridBagConstraints.WEST;
        resultsPanel.add(lblRevenueDetails, resultGbc);

        resultGbc.gridx = 0; resultGbc.gridy = 2;
        resultGbc.gridwidth = 1;
        resultsPanel.add(new JLabel("UKUPNI RASHODI:"), resultGbc);
        
        resultGbc.gridx = 1;
        resultGbc.anchor = GridBagConstraints.EAST;
        resultsPanel.add(lblTotalExpenses, resultGbc);
        
        resultGbc.gridx = 0; resultGbc.gridy = 3;
        resultGbc.gridwidth = 2;
        resultGbc.anchor = GridBagConstraints.WEST;
        resultsPanel.add(lblExpenseDetails, resultGbc);

        resultGbc.gridx = 0; resultGbc.gridy = 4;
        resultGbc.gridwidth = 2;
        resultGbc.fill = GridBagConstraints.HORIZONTAL;
        resultGbc.insets = new Insets(15, 10, 10, 10);
        resultsPanel.add(new javax.swing.JSeparator(), resultGbc);

        resultGbc.gridx = 0; resultGbc.gridy = 5;
        resultGbc.gridwidth = 1;
        resultGbc.fill = GridBagConstraints.NONE;
        resultGbc.insets = new Insets(8, 10, 8, 10);
        resultsPanel.add(new JLabel("NETO DOBIT:"), resultGbc);
        
        resultGbc.gridx = 1;
        resultGbc.anchor = GridBagConstraints.EAST;
        resultsPanel.add(lblNetProfit, resultGbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        contentPanel.add(resultsPanel, gbc);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        
        JButton closeButton = new JButton("Zatvori");
        closeButton.addActionListener(e -> dispose());
        buttonPane.add(closeButton);
    }

    private void setupEventHandlers() {

        dateFromSpinner.addChangeListener(e -> calculateFinancials());
        dateToSpinner.addChangeListener(e -> calculateFinancials());
    }

    private void calculateFinancials() {
        try {

            java.util.Date fromDate = (java.util.Date) dateFromSpinner.getValue();
            java.util.Date toDate = (java.util.Date) dateToSpinner.getValue();
            
            LocalDate startDate = new java.sql.Date(fromDate.getTime()).toLocalDate();
            LocalDate endDate = new java.sql.Date(toDate.getTime()).toLocalDate();
            
            if (startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this, 
                    "Početni datum mora biti pre završnog datuma!", 
                    "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double sredstvaRevenue = calculateSredstvaRevenue(startDate, endDate);
            double prihodiRevenue = calculatePrihodiRevenue(startDate, endDate);
            System.out.println(prihodiRevenue);
            double salaryExpenses = calculateSalaryExpenses(startDate, endDate);
            
            double totalRevenue = sredstvaRevenue + prihodiRevenue;
            double totalExpenses = salaryExpenses;
            double netProfit = totalRevenue - totalExpenses;

            lblTotalRevenue.setText(String.format("%.2f RSD", totalRevenue));
            lblTotalExpenses.setText(String.format("%.2f RSD", totalExpenses));
            lblNetProfit.setText(String.format("%.2f RSD", netProfit));
            
            lblRevenueDetails.setText(String.format("Sredstva: %.2f RSD, Izdavanje: %.2f RSD", 
                sredstvaRevenue, prihodiRevenue));
            lblExpenseDetails.setText(String.format("Plate: %.2f RSD", salaryExpenses));

            if (netProfit > 0) {
                lblNetProfit.setForeground(new Color(0, 128, 0));
            } else if (netProfit < 0) {
                lblNetProfit.setForeground(new Color(200, 0, 0));
            } else {
                lblNetProfit.setForeground(Color.BLACK);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Greška pri izračunu finansija: " + e.getMessage(), 
                "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double calculateSredstvaRevenue(LocalDate startDate, LocalDate endDate) {
        double total = 0.0;

        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream("podaci/sredstva.csv"), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = br.readLine()) != null) {
            	String[] parts = line.split("[;\t]");
;
                if (parts.length >= 2) {
                    try {
                        String amountStr = parts[0].trim().replace(",", ".");
                        double amount = Double.parseDouble(amountStr);

                        LocalDate date = null;
                        
                        String[] patterns = {"d.M.yyyy", "dd.MM.yyyy", "yyyy-MM-dd"};
                        for (String p : patterns) {
                            try {
                                java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                                date = LocalDate.parse(parts[1].trim(),f);
                                break;
                            } catch (Exception ignored) {}
                        }

                        if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                            total += amount;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total;
    }


    private double calculatePrihodiRevenue(LocalDate startDate, LocalDate endDate) {
        double total = 0.0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream("podaci/prihodi.csv"), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
 
                if (parts.length >= 3) {
                    try {
                    	LocalDate issueDate = null;
                        
                        String[] patterns = {"d.M.yyyy", "dd.MM.yyyy", "yyyy-MM-dd"};
                        for (String p : patterns) {
                            try {
                                java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                                issueDate = LocalDate.parse(parts[2].trim(),f);
                                break;
                            } catch (Exception ignored) {}
                        }
                        
                        String priceStr = parts[1].trim().replace(",", ".");
                        double price = Double.parseDouble(priceStr);

                        System.out.println(price);
                        
                        if (!issueDate.isBefore(startDate) && !issueDate.isAfter(endDate)) {
                            total += price;
                        }
                    } catch (Exception e) {

                        continue;
                    }
                }
            }
        } catch (IOException e) {

        }
        return total;
    }

    private double calculateSalaryExpenses(LocalDate startDate, LocalDate endDate) {
        double total = 0.0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream("podaci/plate.csv"), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("[;\t]");
                if (parts.length >= 2) {
                    try {

                        String salaryString = parts[0].trim().replace(",", ".");
                        double salary = Double.parseDouble(salaryString);

                        LocalDate date = null;
                        String[] patterns = {"d.M.yyyy", "dd.MM.yyyy", "yyyy-MM-dd"};
                        for (String p : patterns) {
                            try {
                                DateTimeFormatter f = DateTimeFormatter.ofPattern(p);
                                date = LocalDate.parse(parts[1].trim(), f);
                                break;
                            } catch (Exception ignored) {}
                        }

                        if (date == null) {
                            throw new IllegalArgumentException("Nepoznat format datuma: " + parts[1]);
                        }

                        if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                            total += salary;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return total;
    }



}