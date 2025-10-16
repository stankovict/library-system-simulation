package gui;

import javax.swing.*;
import java.awt.*;

import manager.BibliotekarMetode;
import manager.ClanskaKartaMetode;
import manager.LoginManager;
import manager.UlogovaniKorisnik;

import gui.ClanFrame;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin;

    private LoginManager loginManager;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MainFrame() {
        loginManager = new LoginManager();

        setTitle("Login - Biblioteka");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblUsername = new JLabel("Korisničko ime:");
        lblUsername.setBounds(50, 50, 120, 25);
        add(lblUsername);

        tfUsername = new JTextField();
        tfUsername.setBounds(180, 50, 150, 25);
        add(tfUsername);

        JLabel lblPassword = new JLabel("Lozinka:");
        lblPassword.setBounds(50, 100, 120, 25);
        add(lblPassword);

        pfPassword = new JPasswordField();
        pfPassword.setBounds(180, 100, 150, 25);
        add(pfPassword);

        btnLogin = new JButton("Prijavi se");
        btnLogin.setBounds(130, 150, 120, 30);
        add(btnLogin);

        btnLogin.addActionListener(e -> login());
    }

    private void login() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();

        try {

            LoginManager.TipKorisnika tip = loginManager.proveriLogin(username, password);

            if (tip != LoginManager.TipKorisnika.NEPOSTOJI) {
                loginSuccess(username, tip);
            } else {
                JOptionPane.showMessageDialog(this, "Pogrešno korisničko ime ili lozinka!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Greška: " + ex.getMessage());
        }
    }

    private void loginSuccess(String username, LoginManager.TipKorisnika tip) {
        UlogovaniKorisnik.setUsername(username);
        dispose();

        switch (tip) {
            case ADMIN:
                new AdminFrame().setVisible(true);
                BibliotekarMetode.odbijNepotvrdeneRezervacije();
                ClanFrame cf = new ClanFrame();
                cf.proveraAutomatskogOtkazivanja(username);
                break;
            case BIBLIOTEKAR:
                new BibliotekarFrame().setVisible(true);
                BibliotekarMetode.odbijNepotvrdeneRezervacije();
                    

                break;
            case CLAN:
            	 BibliotekarMetode.odbijNepotvrdeneRezervacije();
                String fajlKarte = "podaci/clanskekarte.csv";
                boolean vazeca = ClanskaKartaMetode.korisnikImaVazecu(fajlKarte, username);

                if (vazeca) {
                    new ClanFrame().setVisible(true);
                } else {

                    NeVazecaFrame nevazecaFrame = new NeVazecaFrame(username);
                    nevazecaFrame.setVisible(true);
                }
                break;

            default:
                JOptionPane.showMessageDialog(this, "Nepoznat tip korisnika!");
        }
    }
}
