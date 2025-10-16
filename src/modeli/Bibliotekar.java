package modeli;

import java.time.LocalDate;
import enumi.StrucnaSprema;
import enumi.Pol;

public class Bibliotekar extends Zaposleni {
	
    public Bibliotekar(
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
                brojTelefona,
                adresa,
                username,
                password,
                strucnaSprema.name(),
                String.valueOf(godineStaza),
                String.valueOf(plata)
        );
    }
}