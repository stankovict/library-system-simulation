package modeli;

import enumi.Clanarina;

public class ClanarinaStavka {
    private Clanarina tip;
    private double cena;

    public ClanarinaStavka(Clanarina tip, double cena) {
        this.tip = tip;
        this.cena = cena;
    }

    public Clanarina getTip() {
        return tip;
    }

    public void setTip(Clanarina tip) {
        this.tip = tip;
    }

    public double getCena() {
        return cena;
    }

    public void setCena(double cena) {
        this.cena = cena;
    }

    public String toCSV() {
        return tip.name() + ";" + cena;
    }

    @Override
    public String toString() {
        return "ClanarinaStavka{" +
                "tip=" + tip +
                ", cena=" + cena +
                '}';
    }
}
