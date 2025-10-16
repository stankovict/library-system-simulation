package manager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import enumi.Clanarina;
import enumi.Pol;

import enumi.StatusKnjige;
import enumi.StatusRezervacije;
import enumi.StrucnaSprema;
import manager.NajamManager;
import modeli.Bibliotekar;
import modeli.Cenovnik;
import modeli.Clan;
import modeli.Knjiga;
import modeli.PosebnaKategorija;
import modeli.Rezervacija;
import modeli.Usluga;
public class RezervacijaMetode {

    private static final String FILE_REZERVACIJE = "podaci/rezervacije.csv";
    private static final String FILE_KNJIGE = "podaci/knjige.csv";
    private static final String FILE_CLANOVI = "podaci/clanovi.csv";

    public static boolean kreirajRezervaciju(Clan clan, String naslovKnjige, LocalDate datumPocetka, double cena, List<Usluga> usluge
                                             ) {

        Cenovnik cenovnik = CenovnikMetode.ucitajTrenutniCenovnik("podaci/cenovnici.csv");
        int trajanjeDana = NajamManager.ucitajDefaultNajam();
        LocalDate datumKraja = datumPocetka.plusDays(trajanjeDana);

        List<String[]> rezervacije = ucitajCSV(FILE_REZERVACIJE, 6);

        Knjiga izabranaKnjiga = pronadjiDostupnuKnjigu(naslovKnjige, datumPocetka, datumKraja, rezervacije);
        if (izabranaKnjiga == null) return false;

        Clan clan1 = new Clan(UlogovaniKorisnik.getUsername(), "", Pol.DRUGO, LocalDate.now(), "", "", UlogovaniKorisnik.getUsername(), "", new PosebnaKategorija("BEZ_KATEGORIJE",0), Clanarina.godišnja);
        Bibliotekar bibliotekar = new Bibliotekar("placeholder", "", Pol.DRUGO, LocalDate.now(), "", "", "placeholder", "", StrucnaSprema.III, 1);
        System.out.println(cena);
        RezervacijaMetode.ucitajPoslednjiId();
        int id = RezervacijaMetode.generisiId();
        Rezervacija rez = new Rezervacija(id, clan, izabranaKnjiga, datumPocetka, datumKraja, cena, bibliotekar, usluge);
        System.out.println(cena);
        System.out.println(rez.getCena());

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_REZERVACIJE, true), StandardCharsets.UTF_8))) {
            writer.write(rez.toCSV());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static List<Rezervacija> prikaziRezervacijeZaClana(String username) {
        List<Rezervacija> lista = new ArrayList<>();
        List<String[]> rezervacije = ucitajCSV(FILE_REZERVACIJE, 8);
        List<String[]> clanovi = ucitajCSV(FILE_CLANOVI, 7);

        Clan clan = new Clan(UlogovaniKorisnik.getUsername(), "", Pol.DRUGO, LocalDate.now(), "", "", UlogovaniKorisnik.getUsername(), "", new PosebnaKategorija("BEZ_KATEGORIJE",0), Clanarina.godišnja);
        for (String[] delovi : clanovi) {
            if (delovi[6].equalsIgnoreCase(username)) {
                clan = new Clan(delovi[0], "", Pol.DRUGO, LocalDate.now(), "", "", delovi[0], "", new PosebnaKategorija("BEZ_KATEGORIJE",0), Clanarina.godišnja);
                break;
            }
        }
        if (clan == null) return lista;

        for (String[] rezDelovi : rezervacije) {
            if (rezDelovi[1].equalsIgnoreCase(username)) {
                Knjiga knjiga = pronadjiKnjiguPoID(rezDelovi[2]);
                Bibliotekar bibliotekar = new Bibliotekar(rezDelovi[8], "", Pol.DRUGO, LocalDate.now(), "", "", rezDelovi[8], "", StrucnaSprema.III, 2);
                Double cena = Double.parseDouble(rezDelovi[7]);

                List<Usluga> usluge = new ArrayList<>();
                if (rezDelovi.length > 9 && !rezDelovi[9].isEmpty()) {
                    String[] nazivi = rezDelovi[9].split("\\|");
                    for (String naziv : nazivi) {
                        usluge.add(new Usluga(naziv, cena));
                    }
                }
                
                LocalDate datumPocetka = null;
           	 LocalDate datumKraja= null;

                if (knjiga != null) {
                	 String[] patterns = {"d.M.yyyy", "dd.MM.yyyy","yyyy-MM-dd"};
                     for (String p : patterns) {
                         try {
                             java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                             datumPocetka = LocalDate.parse(rezDelovi[4].trim(),f);
                              datumKraja = LocalDate.parse(rezDelovi[5].trim(),f);
                             break;
                         } catch (Exception ignored) {}
                     }
                	 
                    int id = Integer.parseInt(rezDelovi[0]);
                    
                    Rezervacija rez = new Rezervacija(
                        id,
                        clan,
                        knjiga,
                        datumPocetka,
                        datumKraja,
                        cena,
                        bibliotekar,
                        usluge
                    );
                    rez.setStatus(StatusRezervacije.valueOf(rezDelovi[6]));
                    lista.add(rez);
                }
            }
        }
        return lista;
    }



    private static List<String[]> ucitajCSV(String path, int minColumns) {
        List<String[]> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";", -1);
                if (delovi.length >= minColumns) lista.add(delovi);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private static Knjiga pronadjiDostupnuKnjigu(
            String naslov, LocalDate datumOd, LocalDate datumDo, List<String[]> rezervacije) {

        List<String[]> knjige = ucitajCSV(FILE_KNJIGE, 5);

        for (String[] knj : knjige) {
            String naziv = knj[0].trim();
            String autor = knj[1].trim();
            String zanr = knj[2].trim();
            String id = knj[3].trim();
            StatusKnjige status = StatusKnjige.valueOf(knj[4].trim().toUpperCase());

            if (naziv.equalsIgnoreCase(naslov)) {
                boolean zauzeta = false;

                for (String[] rez : rezervacije) {
                    String rezId = rez[2];
                    LocalDate rezOd = null;
                    LocalDate rezDo = null;
                    ;
                    
                    String[] patterns = {"d.M.yyyy", "dd.MM.yyyy","yyyy-MM-dd"};
                    for (String p : patterns) {
                        try {
                            java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                            rezOd = LocalDate.parse(rez[4].trim(),f);
                             rezDo = LocalDate.parse(rez[5].trim(),f);
                            break;
                        } catch (Exception ignored) {}
                    }
                    //StatusRezervacije rezStatus = StatusRezervacije.valueOf(rez[6].trim().toUpperCase());

                    //if (rezId.equals(id) && rezStatus == StatusRezervacije.POTVRĐENA) {
                      //  if (!(datumDo.isBefore(rezOd) || datumOd.isAfter(rezDo))) {
                        //    zauzeta = true;
                          //  break;
                        //}
                    //}
                }

                if (!zauzeta) {
                    return new Knjiga(naziv, autor, zanr, Integer.parseInt(id), StatusKnjige.DOSTUPNA);
                }
            }
        }
        return null;
    }

    
    public static List<Knjiga> pronadjiDostupneKnjige(LocalDate datumOd, LocalDate datumDo) {
        List<Knjiga> dostupne = new ArrayList<>();

        List<String[]> knjige = ucitajCSV(FILE_KNJIGE, 5);
        List<String[]> rezervacije = ucitajCSV(FILE_REZERVACIJE, 6);

        for (String[] knj : knjige) {
            String naziv = knj[0].trim();
            String autor = knj[1].trim();
            String zanr = knj[2].trim();
            String id = knj[3].trim();
            StatusKnjige status = StatusKnjige.valueOf(knj[4].trim().toUpperCase());

            

            boolean zauzeta = false;
            for (String[] rez : rezervacije) {
                String rezIdKnjige = rez[2].trim();
                StatusRezervacije rezStatus = StatusRezervacije.valueOf(rez[6].trim().toUpperCase());

                if (rezIdKnjige.equals(id) && rezStatus == StatusRezervacije.POTVRĐENA) {
                	
                    LocalDate rezOd =null;
                    LocalDate rezDo = null;
                    String[] patterns = {"d.M.yyyy", "dd.MM.yyyy","yyyy-MM-dd"};
                    for (String p : patterns) {
                        try {
                            java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                            rezOd = LocalDate.parse(rez[4].trim(),f);
                             rezDo = LocalDate.parse(rez[5].trim(),f);
                            break;
                        } catch (Exception ignored) {}
                    }

                    if (!(datumDo.isBefore(rezOd) || datumOd.isAfter(rezDo))) {
                        zauzeta = true;
                        break;
                    }
                }
            }

            if (!zauzeta) {
                dostupne.add(new Knjiga(naziv, autor, zanr, Integer.parseInt(id), StatusKnjige.DOSTUPNA));
            }
        }

        return dostupne;
    }


    private static Knjiga pronadjiKnjiguPoID(String id) {
        List<String[]> knjige = ucitajCSV(FILE_KNJIGE, 5);
        for (String[] knj : knjige) {
            if (knj[3].equals(id)) {
                StatusKnjige status = StatusKnjige.valueOf(knj[4].trim().toUpperCase());
                return new Knjiga(
                        knj[0],
                        knj[1],
                        knj[2],
                        Integer.parseInt(knj[3]),
                        status
                );
            }
        }
        return null;
    }
    
    public static List<Rezervacija> ucitajRezervacije(String fajlRez) {
        List<Rezervacija> lista = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajlRez), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";"); 
                if (delovi.length >= 9) {
                    int id = Integer.parseInt(delovi[0].trim());
                    String clanUsername = delovi[1].trim();
                    Clan clan = new Clan(clanUsername, "", Pol.DRUGO, LocalDate.now(), "", "", clanUsername, "", new PosebnaKategorija("BEZ_KATEGORIJE",0), Clanarina.godišnja);

                    int knjigaId = Integer.parseInt(delovi[2].trim());
                    String naslov = delovi[3].trim();
                    Knjiga knjiga = new Knjiga(naslov, "", "", knjigaId, StatusKnjige.DOSTUPNA);
                    
                    LocalDate datumPocetak = null;
                    LocalDate datumKraj = null;
                    
                    String[] patterns = {"d.M.yyyy", "dd.MM.yyyy","yyyy-MM-dd"};
                    for (String p : patterns) {
                        try {
                            java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                            datumPocetak = LocalDate.parse(delovi[4].trim(),f);
                             datumKraj = LocalDate.parse(delovi[5].trim(),f);
                            break;
                        } catch (Exception ignored) {}
                    }
                    

                    double cena = Double.parseDouble(delovi[7].trim());
                    StatusRezervacije status = StatusRezervacije.valueOf(delovi[6].trim());

                    Bibliotekar bibliotekar = null;
                    if (delovi.length > 8) { 
                        String biblioUsername = delovi[8].trim();
                        bibliotekar = new Bibliotekar(biblioUsername, "", Pol.DRUGO, LocalDate.now(), "", "", biblioUsername, "", StrucnaSprema.III, 2);
                    }
                    
                    Cenovnik trenutniCenovnik = CenovnikMetode.ucitajTrenutniCenovnik("podaci/cenovnici.csv");

                    List<Usluga> usluge = new ArrayList<>();
                    if (delovi.length > 9 && !delovi[9].isEmpty()) {
                        String[] nazivi = delovi[9].split("\\,");
                        for (String naziv : nazivi) {

                            double cenaUsluge = 0;
                            for (Usluga u : trenutniCenovnik.getUsluge()) {
                                if (u.getNaziv().equalsIgnoreCase(naziv.trim())) {
                                    cenaUsluge = u.getCena();
                                    break;
                                }
                            }
                            usluge.add(new Usluga(naziv.trim(), cenaUsluge));
                        }
                    
                    }

                    Rezervacija rez = new Rezervacija(id, clan, knjiga, datumPocetak, datumKraj, cena, bibliotekar, usluge);
                    rez.setStatus(status);
                    lista.add(rez);
                }
            }
        } catch (IOException e) {
            System.out.println("Greška pri čitanju rezervacija: " + e.getMessage());
        }

        return lista;
    }




    private static final String FAJL_REZERVACIJE = "podaci/rezervacije.csv";

    public static void sacuvajRezervacije(List<Rezervacija> rezervacije) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(FAJL_REZERVACIJE, false), StandardCharsets.UTF_8))) {

            for (Rezervacija r : rezervacije) {
                String uslugeStr = r.getUsluge().stream()
                        .map(Usluga::getNaziv)
                        .collect(Collectors.joining(","));
                
                String linija = r.getClan().getUsername() + ";" +
                                r.getKnjiga().getId() + ";" +
                                r.getKnjiga().getNaslov() + ";" +
                                r.getDatumPocetka() + ";" +
                                r.getDatumKraja() + ";" +
                                r.getCena() + ";" +
                                r.getStatus() + ";" +
                                r.getBibliotekar().getUsername() + ";" +
                                uslugeStr;
                bw.write(linija);
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static int poslednjiId = 0;

 public static void ucitajPoslednjiId() {
     List<Rezervacija> sve = ucitajRezervacije("podaci/rezervacije.csv");
     for (Rezervacija r : sve) {
         if (r.getIdRez() > poslednjiId) {
             poslednjiId = r.getIdRez();
         }
     }
 }

 public static int generisiId() {
     poslednjiId++;
     return poslednjiId;
 }


}
