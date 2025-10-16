package gui;

import javax.swing.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import manager.AdminMetode;
import enumi.Pol;
import enumi.StrucnaSprema;

public class DodajBibliotekaraDialog extends JDialog {

    private JTextField tfIme, tfPrezime, tfTelefon, tfAdresa, tfKorisnickoIme, tfGodineStaza;
    private JPasswordField pfLozinka;
    private JComboBox<Pol> cbPol;
    private JComboBox<StrucnaSprema> cbStrucnaSprema;
    private JSpinner spDatumRodjenja;

    public DodajBibliotekaraDialog(JFrame parent, boolean modal) {
        super(parent, "Dodaj bibliotekara", modal);
        setSize(400, 500);
        setLocationRelativeTo(parent);
        setLayout(null);

        tfIme = dodajPolje("Ime:", 20);
        tfPrezime = dodajPolje("Prezime:", 60);
        cbPol = dodajComboBox("Pol:", Pol.values(), 100);
        spDatumRodjenja = dodajDatum("Datum rodjenja:", 140);
        tfTelefon = dodajPolje("Telefon:", 180);
        tfAdresa = dodajPolje("Adresa:", 220);
        tfKorisnickoIme = dodajPolje("Korisnicko ime:", 260);
        pfLozinka = dodajPasswordPolje("Lozinka:", 300);
        cbStrucnaSprema = dodajComboBox("Strucna sprema:", StrucnaSprema.values(), 340);
        tfGodineStaza = dodajPolje("Godine staza:", 380);

        JButton btnDodaj = new JButton("Dodaj");
        btnDodaj.setBounds(150, 420, 100, 30);
        add(btnDodaj);

        btnDodaj.addActionListener(e -> {
            try {
                LocalDate datum = ((Date) spDatumRodjenja.getValue())
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                AdminMetode.dodajAdmina("podaci/bibliotekari.csv",
                        tfIme.getText(),
                        tfPrezime.getText(),
                        (Pol) cbPol.getSelectedItem(),
                        datum,
                        tfTelefon.getText(),
                        tfAdresa.getText(),
                        tfKorisnickoIme.getText(),
                        new String(pfLozinka.getPassword()),
                        (StrucnaSprema) cbStrucnaSprema.getSelectedItem(),
                        Integer.parseInt(tfGodineStaza.getText())
                );

                JOptionPane.showMessageDialog(this, "Bibliotekar uspešno dodat!");
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Greška: " + ex.getMessage());
            }
        });
    }

    private JTextField dodajPolje(String label, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 180, 25);
        add(lbl);

        JTextField tf = new JTextField();
        tf.setBounds(200, y, 150, 25);
        add(tf);
        return tf;
    }

    private JPasswordField dodajPasswordPolje(String label, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 180, 25);
        add(lbl);

        JPasswordField pf = new JPasswordField();
        pf.setBounds(200, y, 150, 25);
        add(pf);
        return pf;
    }

    private <T> JComboBox<T> dodajComboBox(String label, T[] values, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 180, 25);
        add(lbl);

        JComboBox<T> cb = new JComboBox<>(values);
        cb.setBounds(200, y, 150, 25);
        add(cb);
        return cb;
    }

    private JSpinner dodajDatum(String label, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(20, y, 180, 25);
        add(lbl);

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SpinnerDateModel model = new SpinnerDateModel(today, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setBounds(200, y, 150, 25);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        add(spinner);
        return spinner;
    }
}
