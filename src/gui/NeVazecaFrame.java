package gui;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import javax.swing.*;
import enumi.Clanarina;

public class NeVazecaFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private String korisnik;
    private JComboBox<Clanarina> comboClanarina;

    public NeVazecaFrame(String korisnik) {
        this.korisnik = korisnik;

        setTitle("Članarina nevažeća");
        setSize(450, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel lblInfo = new JLabel(
            "<html>Vaša članarina je istekla.<br>Molimo pošaljite zahtev za produženje.</html>",
            SwingConstants.CENTER);
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 14));
        add(lblInfo, BorderLayout.NORTH);

        comboClanarina = new JComboBox<>(Clanarina.values());
        JPanel panelCombo = new JPanel();
        panelCombo.add(new JLabel("Izaberite vrstu članarine: "));
        panelCombo.add(comboClanarina);
        add(panelCombo, BorderLayout.CENTER);

        JButton btnZahtev = new JButton("Pošalji zahtev za produženje");
        btnZahtev.addActionListener(e -> posaljiZahtev());
        JPanel panelDugme = new JPanel();
        panelDugme.add(btnZahtev);
        add(panelDugme, BorderLayout.SOUTH);
    }

    private void posaljiZahtev() {
        LocalDate danas = LocalDate.now();
        Clanarina tip = (Clanarina) comboClanarina.getSelectedItem();
        String fajlZahteva = "podaci/zahtevi_produzenja.csv";
        String fajlKasnjenja = "podaci/kasnjenja.csv";

        int brojKasnjenja = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlKasnjenja), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 0 && parts[0].equalsIgnoreCase(korisnik)) {
                    brojKasnjenja++;
                }
            }
        } catch (FileNotFoundException ex) {

            brojKasnjenja = 0;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Greška pri čitanju kašnjenja: " + ex.getMessage());
            return;
        }


        if (brojKasnjenja >= 5) {
            JOptionPane.showMessageDialog(this,
                "❌ Ne možete produžiti članarinu.\n" +
                "Imate " + brojKasnjenja + " kašnjenja sa vraćanjem knjiga.",
                "Produženje odbijeno", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fajlZahteva, true), StandardCharsets.UTF_8))) {
            bw.write(korisnik + ";" + danas + ";" + tip);
            bw.newLine();
            JOptionPane.showMessageDialog(this, "✅ Zahtev za produženje poslat.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Greška pri slanju zahteva: " + ex.getMessage());
        }

        dispose();
    }

}
