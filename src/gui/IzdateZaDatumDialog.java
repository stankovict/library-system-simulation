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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class IzdateZaDatumDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JSpinner spinnerOd;
    private JSpinner spinnerDo;
    private JTable table;
    private DefaultTableModel tableModel;


    public IzdateZaDatumDialog(Dialog owner) {
        setTitle("Izdate knjige po bibliotekarima");
        setBounds(100, 100, 600, 400);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        
        contentPanel.setLayout(new BorderLayout(10, 10));
        
 
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        datePanel.add(new JLabel("Od:"), gbc);
        
        gbc.gridx = 1;
        spinnerOd = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorOd = new JSpinner.DateEditor(spinnerOd, "yyyy-MM-dd");
        spinnerOd.setEditor(editorOd);
        datePanel.add(spinnerOd, gbc);
        
        gbc.gridx = 2;
        datePanel.add(new JLabel("Do:"), gbc);
        
        gbc.gridx = 3;
        spinnerDo = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorDo = new JSpinner.DateEditor(spinnerDo, "yyyy-MM-dd");
        spinnerDo.setEditor(editorDo);
        datePanel.add(spinnerDo, gbc);
        
        gbc.gridx = 4;
        JButton btnPrikazi = new JButton("Prikaži");
        btnPrikazi.addActionListener(e -> prikaziStatistiku());
        datePanel.add(btnPrikazi, gbc);
        
        contentPanel.add(datePanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Bibliotekar", "Broj izdatih knjiga"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        
        JButton closeButton = new JButton("Zatvori");
        closeButton.addActionListener(e -> dispose());
        buttonPane.add(closeButton);
        getRootPane().setDefaultButton(closeButton);
    }
    
    private void prikaziStatistiku() {
        java.util.Date dateOd = (java.util.Date) spinnerOd.getValue();
        java.util.Date dateDo = (java.util.Date) spinnerDo.getValue();
        
        LocalDate od = new java.sql.Date(dateOd.getTime()).toLocalDate();
        LocalDate do_ = new java.sql.Date(dateDo.getTime()).toLocalDate();
        
        Map<String, Integer> bibliotekariBrojac = prebrojIzdateKnjige(od, do_);

        tableModel.setRowCount(0);

        for (Map.Entry<String, Integer> entry : bibliotekariBrojac.entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
    
    private Map<String, Integer> prebrojIzdateKnjige(LocalDate od, LocalDate do_) {
        Map<String, Integer> brojac = new HashMap<>();
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
                if (delovi.length >= 4) {
                    String datumStr = delovi[4].trim();
                    String bibliotekar = delovi[3].trim();
                    
                    LocalDate datum = null;
                    for (DateTimeFormatter formatter : formateri) {
                        try {
                            datum = LocalDate.parse(datumStr, formatter);
                            break;
                        } catch (Exception ignored) {}
                    }
                    
                    if (datum != null && !datum.isBefore(od) && !datum.isAfter(do_)) {
                        brojac.put(bibliotekar, brojac.getOrDefault(bibliotekar, 0) + 1);
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return brojac;
    }
}