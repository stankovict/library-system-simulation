package modeli;

import java.time.LocalDate;
import enumi.Pol;

public abstract class Korisnik {
	
    protected String ime;
    protected String prezime;
    protected Pol pol;
    protected LocalDate datumRodjenja;
    protected String brojTelefona;
    protected String adresa;
    protected String username;
    protected String password;

    public Korisnik(
    		String ime,
    		String prezime,
    		Pol pol,
    		LocalDate datumRodjenja,
    		String brojTelefona,
    		String adresa,
    		String username,
    		String password) {
    	
    	this.ime = ime;
        this.prezime = prezime;
        this.pol = pol;
        this.datumRodjenja = datumRodjenja;
        this.brojTelefona = brojTelefona;
        this.adresa = adresa;
        this.username = username;
        this.password = password;
    }
    

    public String getIme() {
    	return ime;
    	}
    
    public String getPrezime() {
    	return prezime;
    	}
    
    public Pol getPol() {
    	return pol;
    	}
    
    public LocalDate getDatumRodjenja() {
    	return datumRodjenja;
    	}
    
    public String getBrojTelefona() {
    	return brojTelefona;
    	}
    
    public String getAdresa() {
    	return adresa;
    	}
    
    public String getUsername() {
    	return username;
    	}
    
    public String getPassword() {
    	return password;
    	}


    public void setIme(String ime) {
    	this.ime = ime;
    	}
    
    public void setPrezime(String prezime) {
    	this.prezime = prezime;
    	}
    
    public void setPol(Pol pol) {
    	this.pol = pol;
    	}
    
    public void setDatumRodjenja(LocalDate datumRodjenja) {
    	this.datumRodjenja = datumRodjenja;
    	}
    
    public void setBrojTelefona(String brojTelefona) {
    	this.brojTelefona = brojTelefona;
    	}
    
    public void setAdresa(String adresa) {
    	this.adresa = adresa;
    	}
    
    public void setUsername(String username) {
    	this.username = username;
    	}
    
    public void setPassword(String password) {
    	this.password = password;
    	}

}
