package modeli;

import enumi.Clanarina;
import java.time.LocalDate;

public class ClanskaKarta {

    private String korisnickoIme;
    private Clanarina tipClanarine;
    private LocalDate datumUplate;
    private LocalDate datumIsteka;

    public ClanskaKarta(String korisnickoIme, Clanarina tipClanarine, LocalDate datumUplate) {
        this.korisnickoIme = korisnickoIme;
        this.tipClanarine = tipClanarine;
        this.datumUplate = datumUplate;

        if (tipClanarine == Clanarina.mesečna) {
            this.datumIsteka = datumUplate.plusMonths(1);
        } else if (tipClanarine == Clanarina.godišnja) {
            this.datumIsteka = datumUplate.plusYears(1);
        }
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public Clanarina getTipClanarine() {
        return tipClanarine;
    }

    public LocalDate getDatumUplate() {
        return datumUplate;
    }

    public LocalDate getDatumIsteka() {
        return datumIsteka;
    }

    public boolean isVazeca() {
        LocalDate danas = LocalDate.now();
        return !danas.isBefore(datumUplate) && !danas.isAfter(datumIsteka);
    }

    public String toCSV() {
        return String.join(";",
                korisnickoIme,
                tipClanarine != null ? tipClanarine.name() : "",
                datumUplate.toString(),
                datumIsteka.toString()
        );
    }

	public void setTip(Clanarina tipClanarine) {
		
		this.tipClanarine = tipClanarine;

		
	}

	public void setDatumUplate(LocalDate datumUplate) {
		this.datumUplate = datumUplate;
		
	}

	public void setDatumIsteka(LocalDate datumIsteka) {

		this.datumIsteka = datumIsteka;
	}
}

