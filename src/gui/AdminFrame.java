package gui;

import javax.swing.*;

import manager.CenovnikMetode;
import manager.PosebnaKategorijaMetode;
import manager.UlogovaniKorisnik;
import modeli.Cenovnik;
import modeli.PosebnaKategorija;

import java.awt.Font;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AdminFrame extends JFrame {

    public AdminFrame() {
        setTitle("Administratorski panel");
        setSize(444, 424);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);


        JLabel lblWelcome = new JLabel("Dobrodošli, ADMIN: " + UlogovaniKorisnik.getUsername(), SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblWelcome.setBounds(50, 10, 320, 30);
        getContentPane().add(lblWelcome);


        JButton btnDodajAdmina = new JButton("Dodaj novog admina");
        btnDodajAdmina.setBounds(29, 60, 171, 30);
        getContentPane().add(btnDodajAdmina);

        btnDodajAdmina.addActionListener(e -> {
            JDialog dialog = new DodajAdminaDialog(this, true);
            dialog.setVisible(true);
        });

        JButton btnSvi = new JButton("Svi zaposleni");
        btnSvi.setBounds(29, 110, 171, 30);
        getContentPane().add(btnSvi);
        btnSvi.addActionListener(e -> {
            SviZaposleniDialog dialog = new SviZaposleniDialog(this);
            dialog.setVisible(true);
        });


        JButton btnNajam = new JButton("Trajanje najma");
        btnNajam.setBounds(220, 110, 171, 30);
        getContentPane().add(btnNajam);

        btnNajam.addActionListener(e -> {
            PromeniNajamDialog dialog = new PromeniNajamDialog(this);
            dialog.setVisible(true);
        });
        JButton btnDodajCenovnik = new JButton("Dodaj novi cenovnik");
        btnDodajCenovnik.setBounds(29, 160, 171, 30);
        getContentPane().add(btnDodajCenovnik);

        btnDodajCenovnik.addActionListener(e -> {
            NoviCenovnikDialog dialog = new NoviCenovnikDialog(this);
            dialog.setVisible(true);

            Cenovnik novi = dialog.getNoviCenovnik();
            if (novi != null) {

                JOptionPane.showMessageDialog(this, "Novi cenovnik dodat!");
            }
        });

        JButton btnCenovnik = new JButton("Cenovnici");
        btnCenovnik.setBounds(220, 160, 171, 30);
        getContentPane().add(btnCenovnik);
        btnCenovnik.addActionListener(e -> {
            CenovniciDialog dialog = new CenovniciDialog(this, "podaci/cenovnici.csv");
            dialog.setVisible(true);
        });


        JButton btnDodajBibliotekara = new JButton("Dodaj novog bibliotekara");
        btnDodajBibliotekara.setBounds(220, 60, 171, 30);
        getContentPane().add(btnDodajBibliotekara);

        btnDodajBibliotekara.addActionListener(e -> {
            JDialog dialog = new DodajBibliotekaraDialog(this, true);
            dialog.setVisible(true);
        });

        JButton btnOdjava = new JButton("Odjava");
        btnOdjava.setBounds(136, 342, 150, 30);
        getContentPane().add(btnOdjava);
        
        JButton btnDodavanjeKazniusluga = new JButton("Kazne/Usluge");
        btnDodavanjeKazniusluga.setBounds(29, 207, 171, 30);
        getContentPane().add(btnDodavanjeKazniusluga);
        
        JButton btnIzmenaCenovnika = new JButton("Izmena trenutnog cenovnika");
        btnIzmenaCenovnika.setBounds(220, 207, 171, 30);
        getContentPane().add(btnIzmenaCenovnika);
        
        JButton btnNovaKategorija = new JButton(" Nova kategorija člana");
        btnNovaKategorija.addActionListener(e -> {
            DodajKategorijuDialog dialog = new DodajKategorijuDialog();
            dialog.setVisible(true);

            if (dialog.isPotvrđeno()) {
                String naziv = dialog.getNaziv();
                double popust = dialog.getPopust();

                PosebnaKategorijaMetode metode = new PosebnaKategorijaMetode();
                metode.dodajKategoriju(naziv, popust);


            }
        });

        btnNovaKategorija.setBounds(29, 255, 171, 30);
        getContentPane().add(btnNovaKategorija);
        
        JButton btnIsplataPlate = new JButton("Reguliši plate");
        btnIsplataPlate.setBounds(220, 255, 171, 30);
        getContentPane().add(btnIsplataPlate);

        btnIsplataPlate.addActionListener(e -> {
            PlateDialog dialog = new PlateDialog();
            dialog.setModal(true);
            dialog.setVisible(true);
        });

        
        JButton btnMesecnaSredstva = new JButton("Priliv novca (budžet)");
        btnMesecnaSredstva.setBounds(220, 301, 171, 30);
        getContentPane().add(btnMesecnaSredstva);
        
        JButton btnIzvestaji = new JButton("Izveštaji");
        btnIzvestaji.setBounds(29, 301, 171, 30);
        getContentPane().add(btnIzvestaji);

        btnIzvestaji.addActionListener(e -> {
            IzvestajiDialog dialog = new IzvestajiDialog();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        btnMesecnaSredstva.addActionListener(e -> {
            PrilivNovcaDialog dialog = new PrilivNovcaDialog();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        
        
        btnIzmenaCenovnika.addActionListener(e -> {
        	Cenovnik trenutniCenovnik = CenovnikMetode.ucitajTrenutniCenovnik("podaci/cenovnici.csv");
            if (trenutniCenovnik == null) {
                JOptionPane.showMessageDialog(this, "Trenutni cenovnik nije učitan!", "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            IzmenaTrenutnogCenovnika dialog = new IzmenaTrenutnogCenovnika((JFrame) SwingUtilities.getWindowAncestor(this), trenutniCenovnik);
            dialog.setVisible(true);
        });

        
        
        btnDodavanjeKazniusluga.addActionListener(e -> {
            DodajStavkeDialog dialog = new DodajStavkeDialog(
                this, 
                "podaci/kazne.csv", 
                "podaci/usluge.csv",
                "podaci/clanarine.csv"
            );
            dialog.setVisible(true);
        });


        btnOdjava.addActionListener(e -> {
            UlogovaniKorisnik.setUsername(null);
            dispose();
            new MainFrame().setVisible(true);
        });
    }
}
