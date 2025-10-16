package modeli;

import java.time.LocalDate;
import enumi.StrucnaSprema;
import enumi.Pol;

public class Administrator extends Zaposleni {
	
    public Administrator(
    		String ime,
    		String prezime, 
    		Pol pol,
    		LocalDate datumRodjenja,
    		String telefon, 
    		String adresa, 
    		String korisnickoIme,
    		String lozinka,
    		StrucnaSprema strucnaSprema,
    		int godineStaza) {
    		super(
    			ime,
        		prezime,
        		pol,
        		datumRodjenja,
        		telefon,
        		adresa,
        		korisnickoIme,
        		lozinka,
        		strucnaSprema,
        		godineStaza);
    }
    
    public void obracunajPlatu() {
        izracunajPlatu();
    }
    
    public String toCSV() {
    	
    	obracunajPlatu();
    	
        return String.join(";",
                ime,
                prezime,
                pol.name(),
                datumRodjenja.toString(),
                String.valueOf(brojTelefona),
                adresa,
                username,
                password,
                strucnaSprema.name(),
                String.valueOf(godineStaza),
                String.valueOf(getPlata())
        );
    }
}

