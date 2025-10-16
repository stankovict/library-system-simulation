package modeli;

import enumi.StrucnaSprema;
import enumi.Pol;
import java.time.LocalDate;

public abstract class Zaposleni extends Korisnik {

    protected StrucnaSprema strucnaSprema;
    protected int godineStaza;
    protected double plata;

    public Zaposleni(
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
                lozinka);

        this.strucnaSprema = strucnaSprema;
        this.godineStaza = godineStaza;
    }

    public void izracunajPlatu() {
        double osnova = 7000;
        this.plata = osnova * (strucnaSprema.getKoeficijent() + 0.004 * godineStaza);
    }

    public double getPlata() {
        return plata;
    }

    public int getGodineStaza() {
        return godineStaza;
    }

    public StrucnaSprema getStrucnaSprema() {
        return strucnaSprema;
    }

    public void setStrucnaSprema(StrucnaSprema strucnaSprema) {
        this.strucnaSprema = strucnaSprema;
    }

    public void setGodineStaza(int godineStaza) {
        this.godineStaza = godineStaza;
    }
}
