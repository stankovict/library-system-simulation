package manager;

import enumi.Clanarina;
import modeli.ClanskaKarta;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClanskaKartaMetode {

    public static List<ClanskaKarta> ucitajKarte(String fajl) {
        List<ClanskaKarta> karte = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fajl), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length == 4) {
                    String korisnickoIme = delovi[0];
                    Clanarina tip = Clanarina.valueOf(delovi[1]);

                    LocalDate datumUplate = null;
                    LocalDate datumIsteka = null;
                    String[] patterns = {"d.M.yyyy", "dd.MM.yyyy", "yyyy-MM-dd"};
                    for (String p : patterns) {
                        try {
                            java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                            datumUplate = LocalDate.parse(delovi[2].trim(), f);
                            datumIsteka = LocalDate.parse(delovi[3].trim(), f);
                            break;
                        } catch (Exception ignored) {
                        }
                    }

                    ClanskaKarta karta = new ClanskaKarta(korisnickoIme, tip, datumUplate);

                    if (!karta.getDatumIsteka().equals(datumIsteka)) {
                        karta.setDatumIsteka(datumIsteka);
                    }

                    karte.add(karta);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return karte;
    }

    public static void sacuvajKarte(String fajl, List<ClanskaKarta> karte) {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(fajl), StandardCharsets.UTF_8)) {

            for (ClanskaKarta k : karte) {
                writer.write(k.toCSV());
                writer.write(System.lineSeparator());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dodajKartu(String fajl, ClanskaKarta karta) {
        List<ClanskaKarta> karte = ucitajKarte(fajl);
        karte.add(karta);
        sacuvajKarte(fajl, karte);
    }

    public static ClanskaKarta pronadjiPoKorisniku(String fajl, String korisnickoIme) {
        List<ClanskaKarta> karte = ucitajKarte(fajl);
        for (ClanskaKarta k : karte) {
            if (k.getKorisnickoIme().equalsIgnoreCase(korisnickoIme)) {
                return k;
            }
        }
        return null;
    }

    public static boolean korisnikImaVazecu(String fajl, String korisnickoIme) {
        ClanskaKarta karta = pronadjiPoKorisniku(fajl, korisnickoIme);
        return karta != null && karta.isVazeca();
    }
}