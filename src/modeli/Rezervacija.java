package modeli;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import enumi.StatusKnjige;
import enumi.StatusRezervacije;

public class Rezervacija {


	private int id;
    private Clan clan;
    private Knjiga knjiga;
    private LocalDate datumPocetka;
    private LocalDate datumKraja;
    private StatusRezervacije status;
    private double cena;
    private Bibliotekar bibliotekar;
    private List<Usluga> usluge = new ArrayList<>();

    public Rezervacija(int id, Clan clan, Knjiga knjiga, LocalDate datumPocetka, LocalDate datumKraja,
            double cena, Bibliotekar bibliotekar, List<Usluga> usluge) {
this.id = id;
this.clan = clan;
this.knjiga = knjiga;
this.datumPocetka = datumPocetka;
this.datumKraja = datumKraja;
this.status = StatusRezervacije.NA_ČEKANJU;
this.cena = cena;
this.bibliotekar = bibliotekar;
this.usluge = usluge != null ? usluge : new ArrayList<>();
}



    public Clan getClan() { return clan; }
    public Knjiga getKnjiga() { return knjiga; }
    public LocalDate getDatumPocetka() { return datumPocetka; }
    public LocalDate getDatumKraja() { return datumKraja; }
    public void setDatumKraja(LocalDate datumKraja) { this.datumKraja = datumKraja; }

    public StatusRezervacije getStatus() { return status; }
    public void setStatus(StatusRezervacije status) { this.status = status; }

    public double getCena() { return cena; }
    public void setCena(double cena) { this.cena = cena; }
    
    public Bibliotekar getBibliotekar() { return bibliotekar;}
    public void setBibliotekar(Bibliotekar bibliotekar) {
    	this.bibliotekar = bibliotekar;
    }
    public List<Usluga> getUsluge() { return usluge; }
    public void setUsluge(List<Usluga> usluge) { this.usluge = usluge; }
    public int getIdRez() {
    	return id;
    }
    
    public void setIdRez(int id) {
    	this.id = id;
    }


 
    public String toCSV() {

        String uslugeCSV = usluge.stream()
                .map(u -> u.getNaziv())
                .reduce((u1, u2) -> u1 + "|" + u2)
                .orElse("");

        return String.join(";",
        		String.valueOf(id),
                clan.getUsername(),
                String.valueOf(knjiga.getId()),
                knjiga.getNaslov(),
                datumPocetka.toString(),
                datumKraja.toString(),
                status.name(),
                String.valueOf(cena),
                bibliotekar.getUsername(),
                uslugeCSV
        );
    }


	public void setKnjiga(Knjiga novaKnjiga) {
		this.knjiga = novaKnjiga;
		
	}
}
