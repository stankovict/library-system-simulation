package manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoginManager {

    public enum TipKorisnika {
        ADMIN, BIBLIOTEKAR, CLAN, NEPOSTOJI
    }

    public TipKorisnika proveriLogin(String email, String lozinka) {

        if(proveriFajl("podaci/admini.csv", email, lozinka)) {
            return TipKorisnika.ADMIN;
        } else if(proveriFajl("podaci/bibliotekari.csv", email, lozinka)) {
            return TipKorisnika.BIBLIOTEKAR;
        } else if(proveriFajl("podaci/clanovi.csv", email, lozinka)) {
            return TipKorisnika.CLAN;
        } else {
            return TipKorisnika.NEPOSTOJI;
        }
    }

    private boolean proveriFajl(String putanja, String email, String lozinka) {
        try (BufferedReader br = new BufferedReader(new FileReader(putanja))) {
            String linija;
            while ((linija = br.readLine()) != null) {
                String[] podaci = linija.split(";");
                if(podaci.length >= 8) {
                    String fajlEmail = podaci[6].trim();
                    String fajlPass = podaci[7].trim();
                    if(fajlEmail.equals(email) && fajlPass.equals(lozinka)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}