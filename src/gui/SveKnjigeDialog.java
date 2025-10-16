package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.table.TableCellEditor;

import enumi.StatusKnjige;
import manager.CenovnikMetode;
import manager.ReturnManager;
import modeli.Cenovnik;
import modeli.Knjiga;

public class SveKnjigeDialog extends JDialog {

    private JTable table;
    private DefaultTableModel tableModel;
    private String fajl = "podaci/knjige.csv";

    public SveKnjigeDialog(JFrame parent, boolean modal) {
        super(parent, "Sve knjige", modal);
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Naslov", "Autor", "Žanr", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 4) {
                    JComboBox<StatusKnjige> comboBox = new JComboBox<>(StatusKnjige.values());
                    return new DefaultCellEditor(comboBox);
                }
                return super.getCellEditor(row, column);
            }
        };

        ucitajKnjige();

        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 4) {
                int row = e.getFirstRow();
                int id = (int) tableModel.getValueAt(row, 0);
                String username = (String) tableModel.getValueAt(row, 3);
                StatusKnjige noviStatus = (StatusKnjige) tableModel.getValueAt(row, 4);
                try {
                    azurirajStatusUFajlu(id, noviStatus);

                    if (noviStatus == StatusKnjige.IZDATA) {
                        Cenovnik trenutni = CenovnikMetode.ucitajTrenutniCenovnik("podaci/cenovnici.csv");
                        IzdavanjeDialog rezDialog = new IzdavanjeDialog((JFrame) this.getParent(), true, trenutni, id);
                        rezDialog.setVisible(true);

                        if (!rezDialog.isRezervacijaIzabrana()) {
                            tableModel.setValueAt(StatusKnjige.DOSTUPNA, row, 4);
                            azurirajStatusUFajlu(id, StatusKnjige.DOSTUPNA);
                        }
                    } else if (noviStatus == StatusKnjige.DOSTUPNA) {
                        Cenovnik c = CenovnikMetode.ucitajTrenutniCenovnik("podaci/cenovnici.csv");
                        ReturnManager.processBookReturnWithPenalties(String.valueOf(id), c);
                    }

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Greška pri ažuriranju fajla: " + ex.getMessage());
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnObrisi = new JButton("Obriši knjigu");
        btnObrisi.addActionListener(e -> obrisiKnjigu());
        buttonPanel.add(btnObrisi);

        JButton btnZatvori = new JButton("Zatvori");
        btnZatvori.addActionListener(e -> dispose());
        buttonPanel.add(btnZatvori);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void ucitajKnjige() {
        tableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fajl), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length >= 5) {
                    int id = Integer.parseInt(delovi[3]);
                    StatusKnjige status = StatusKnjige.valueOf(delovi[4]);
                    tableModel.addRow(new Object[]{id, delovi[0], delovi[1], delovi[2], status});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Greška pri učitavanju knjiga: " + e.getMessage());
        }
    }

    private void obrisiKnjigu() {
        int selectedRow = table.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Molimo izaberite knjigu za brisanje.", "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String naslov = (String) tableModel.getValueAt(selectedRow, 1);
        StatusKnjige status = (StatusKnjige) tableModel.getValueAt(selectedRow, 4);

        if (status == StatusKnjige.IZDATA) {
            JOptionPane.showMessageDialog(this, "Ne možete obrisati knjigu koja je trenutno izdata.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Da li ste sigurni da želite obrisati knjigu:\n\"" + naslov + "\" (ID: " + id + ")?", 
            "Potvrda brisanja", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                obrisiKnjiguIzFajla(id);
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Knjiga je uspešno obrisana.", "Uspeh", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Greška pri brisanju knjige: " + ex.getMessage(), "Greška", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void obrisiKnjiguIzFajla(int id) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fajl), StandardCharsets.UTF_8);
        lines.removeIf(line -> {
            String[] delovi = line.split(";");
            return delovi.length >= 4 && Integer.parseInt(delovi[3]) == id;
        });
        Files.write(Paths.get(fajl), lines, StandardCharsets.UTF_8);
    }

    private void azurirajStatusUFajlu(int id, StatusKnjige noviStatus) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fajl), StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String[] delovi = lines.get(i).split(";");
            if (Integer.parseInt(delovi[3]) == id) {
                delovi[4] = noviStatus.name();
                lines.set(i, String.join(";", delovi));
                break;
            }
        }
        Files.write(Paths.get(fajl), lines, StandardCharsets.UTF_8);
    }
}