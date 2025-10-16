package modeli;

import java.time.LocalDate;
import enumi.Pol;
import modeli.PosebnaKategorija;
import enumi.Clanarina;

public class Clan extends Korisnik {
	
	private PosebnaKategorija kategorija;
    private Clanarina clanarina;    
    
    public Clan(
    		String ime,
    		String prezime,
    		Pol pol,
    		LocalDate datumRodjenja,
    		String telefon,
    		String adresa,
    		String korisnickoIme,
    		String lozinka,
    		PosebnaKategorija kategorija,
    		Clanarina clanarina) {
        super(
        	ime,
        	prezime,
        	pol,
        	datumRodjenja,
        	telefon,
        	adresa,
        	korisnickoIme,
        	lozinka);
        this.kategorija = kategorija;
        this.clanarina = clanarina;
    }
    
    public PosebnaKategorija getKategorija() {
    	return kategorija;
    }
    
    public Clanarina getClanarina() {
    	return clanarina;
    }
    
    public String toCSV() {
    	
        return String.join(";",
                ime,
                prezime,
                pol.name(),
                datumRodjenja.toString(),
                brojTelefona,
                adresa,
                username,
                password,
                kategorija != null ? kategorija.getKategorija() : "",
                clanarina.name()
        );
    }
}