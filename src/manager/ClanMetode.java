package manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import enumi.Clanarina;
import enumi.Pol;
import modeli.Clan;
import modeli.PosebnaKategorija;

public class ClanMetode {
	
	public static Clan nadjiClanaPoUsername(String username) {
		
		String FAJL = "podaci/clanovi.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(FAJL))) {
            String linija;
            while ((linija = br.readLine()) != null) {
                String[] delovi = linija.split(";");

                if (delovi.length >= 10 && delovi[6].equals(username)) {

                    String ime = delovi[0];
                    String prezime = delovi[1];
                    Pol pol = delovi[2].isEmpty() ? null : Pol.valueOf(delovi[2]);
                    LocalDate datumRodjenja = null;

                    String[] patterns = {"d.M.yyyy", "dd.MM.yyyy", "yyyy-MM-dd"};
                    for (String p : patterns) {
                        try {
                            java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
                            datumRodjenja = LocalDate.parse(delovi[3].trim());
                            break;
                        } catch (Exception ignored) {}
                    }
                    
                    String telefon = delovi[4];
                    String adresa = delovi[5];
                    String user = delovi[6];
                    String lozinka = delovi[7];

                    PosebnaKategorija kategorija = null;
                    if (!delovi[8].isEmpty()) {

                    	PosebnaKategorijaMetode metode = new PosebnaKategorijaMetode();
                    	List<PosebnaKategorija> sveKat = metode.ucitajKategorije();

                        for (PosebnaKategorija pk : sveKat) {
                            if (pk.getKategorija().equalsIgnoreCase(delovi[8])) {
                                kategorija = pk;
                                break;
                            }
                        }
                        if (kategorija == null) {

                            kategorija = new PosebnaKategorija(delovi[8], 0);
                        }
                    }

                    Clanarina clanarina = delovi[9].isEmpty() ? null : Clanarina.valueOf(delovi[9]);

                    return new Clan(ime, prezime, pol, datumRodjenja, telefon, adresa, user, lozinka, kategorija, clanarina);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}


