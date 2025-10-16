package gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.SwingConstants;

import manager.BibliotekarMetode;
import manager.UlogovaniKorisnik;

public class BibliotekarFrame extends JFrame {

    public BibliotekarFrame() {
    	
        setTitle("Bibliotekar panel");
        setSize(450, 347);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);


        JLabel lblWelcome = new JLabel("Dobrodošli, " + UlogovaniKorisnik.getUsername());
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblWelcome.setBounds(50, 10, 320, 30);
        getContentPane().add(lblWelcome);


        JButton btnDodajClana = new JButton("Dodaj novog člana");
        btnDodajClana.setBounds(29, 60, 171, 30);
        getContentPane().add(btnDodajClana);
        btnDodajClana.addActionListener(e -> {
            JDialog dialog = new DodajClanaDialog(this, true);
            dialog.setVisible(true);
        });

        JButton btnPotvrda = new JButton("Obrada rezervacija");
        btnPotvrda.setBounds(29, 110, 171, 30);
        getContentPane().add(btnPotvrda);

        btnPotvrda.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BibliotekarMetode.obradiRezervacijeGUI((JFrame) SwingUtilities.getWindowAncestor(btnPotvrda));
            }
        });


        JButton btnIzdavanje = new JButton("Dodaj knjigu");
        btnIzdavanje.setBounds(220, 110, 171, 30);
        getContentPane().add(btnIzdavanje);
        btnIzdavanje.addActionListener(e -> {
            DodajKnjiguDialog dialog = new DodajKnjiguDialog(this, true);
            dialog.setVisible(true);
        });

        JButton btnPregled = new JButton("Pregled");
        btnPregled.setBounds(220, 160, 171, 30);
        getContentPane().add(btnPregled);
        
        btnPregled.addActionListener(e -> {

            IzdateObradjeneDialog dialog = new IzdateObradjeneDialog(this);
            dialog.setModal(true);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });


        JButton btnKnjige = new JButton("Izdavanje knjiga");
        btnKnjige.setBounds(220, 60, 171, 30);
        getContentPane().add(btnKnjige);
        btnKnjige.addActionListener(e -> {
            SveKnjigeDialog dialog = new SveKnjigeDialog(this, true);
            dialog.setVisible(true);
        });


        JButton btnOdjava = new JButton("Odjava");
        btnOdjava.setBounds(137, 255, 150, 30);
        getContentPane().add(btnOdjava);
        
        JButton btnClanarinaZahtevi = new JButton("Zahtevi za članarinu");
        btnClanarinaZahtevi.setBounds(29, 160, 171, 30);
        getContentPane().add(btnClanarinaZahtevi);
        JButton btnSviClanovi = new JButton("Svi članovi");
        btnSviClanovi.addActionListener(e -> {
            SviClanoviDialog dialog = new SviClanoviDialog(this);
            dialog.setVisible(true);
        });
        btnSviClanovi.setBounds(137, 211, 150, 30);
        getContentPane().add(btnSviClanovi);

        
        btnClanarinaZahtevi.addActionListener(e -> {
            ClanarinaObradaDialog dialog = new ClanarinaObradaDialog();
            dialog.setModal(true);
            dialog.setVisible(true);
        });

        
        
        
        btnOdjava.addActionListener(e -> {
            UlogovaniKorisnik.setUsername(null);
            dispose();
            new MainFrame().setVisible(true);
        });
    }
}
