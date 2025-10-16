package manager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import enumi.Clanarina;
import modeli.Cenovnik;
import modeli.ClanarinaModel;
import modeli.ClanarinaStavka;
import modeli.Kazna;
import modeli.Usluga;

public class CenovnikMetode {

    public static List<Cenovnik> ucitajCenovnike(String fajl) {
        List<Cenovnik> cenovnici = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fajl), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";", -1);
                if (delovi.length < 6) continue;

                int id = Integer.parseInt(delovi[0]);
                LocalDate datumOd = LocalDate.parse(delovi[1]);
                LocalDate datumDo = LocalDate.parse(delovi[2]);

                Cenovnik c = new Cenovnik(id, datumOd, datumDo);

                if (!delovi[3].isEmpty()) {
                    String[] stavke = delovi[3].split("\\|");
                    for (String s : stavke) {
                        String[] parts = s.split(",");
                        if (parts.length == 2) {
                            ClanarinaStavka cs = new ClanarinaStavka(
                                enumi.Clanarina.valueOf(parts[0]),
                                Double.parseDouble(parts[1])
                            );
                            c.dodajClanarinu(cs);
                        }
                    }
                }

                if (!delovi[4].isEmpty()) {
                    String[] stavke = delovi[4].split("\\|");
                    for (String s : stavke) {
                        String[] parts = s.split(",");
                        if (parts.length == 2) {
                            Kazna k = new Kazna(parts[0], Double.parseDouble(parts[1]));
                            c.dodajKaznu(k);
                        }
                    }
                }

                if (!delovi[5].isEmpty()) {
                    String[] stavke = delovi[5].split("\\|");
                    for (String s : stavke) {
                        String[] parts = s.split(",");
                        if (parts.length == 2) {
                            Usluga u = new Usluga(parts[0], Double.parseDouble(parts[1]));
                            c.dodajUslugu(u);
                        }
                    }
                }

                cenovnici.add(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cenovnici;
    }
    
    public static void sacuvajCenovnike(String fajl, List<Cenovnik> cenovnici) {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(fajl), StandardCharsets.UTF_8)) {

            for (Cenovnik c : cenovnici) {
                writer.write(c.toCSV());
                writer.write(System.lineSeparator());
                
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Kazna> ucitajKazne(String fajl) {
        List<Kazna> kazne = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fajl), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";", -1);
                if (delovi.length == 2) {
                    kazne.add(new Kazna(delovi[0], Double.parseDouble(delovi[1])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kazne;
    }

    public static void sacuvajKazne(String fajl, List<Kazna> kazne) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fajl), StandardCharsets.UTF_8)) {
            for (Kazna k : kazne) {
                writer.write(k.getOpis() + ";" + k.getIznos());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Usluga> ucitajUsluge(String fajl) {
        List<Usluga> usluge = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fajl), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";", -1);
                if (delovi.length == 2) {
                    usluge.add(new Usluga(delovi[0], Double.parseDouble(delovi[1])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return usluge;
    }

    public static void sacuvajUsluge(String fajl, List<Usluga> usluge) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fajl), StandardCharsets.UTF_8)) {
            for (Usluga u : usluge) {
                writer.write(u.getNaziv() + ";" + u.getCena());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public static Cenovnik ucitajTrenutniCenovnik(String fajl) {
        LocalDate danas = LocalDate.now();
        List<Cenovnik> cenovnici = ucitajCenovnike(fajl);

        for (Cenovnik c : cenovnici) {

            if ((danas.isEqual(c.getDatumOd()) || danas.isAfter(c.getDatumOd())) &&
                (danas.isEqual(c.getDatumDo()) || danas.isBefore(c.getDatumDo()))) {
                return c;
            }
        }
        return null;
    }
    
    public static List<ClanarinaModel> ucitajClanarine(String fajl) {
        List<ClanarinaModel> clanarine = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fajl), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";", -1);
                if (delovi.length == 2) {
                    clanarine.add(new ClanarinaModel(Clanarina.valueOf(delovi[0]), Double.parseDouble(delovi[1])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clanarine;
    }

    public static void sacuvajClanarine(String fajl, List<ClanarinaModel> clanarine) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fajl), StandardCharsets.UTF_8)) {
            for (ClanarinaModel c : clanarine) {
                writer.write(c.getTip() + ";" + c.getIznos());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    }

    



