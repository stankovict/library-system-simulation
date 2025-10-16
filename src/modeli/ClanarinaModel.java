package modeli;

import enumi.Clanarina;

public class ClanarinaModel {
	private Clanarina tip;
    private double iznos;

    public ClanarinaModel(Clanarina tip, double iznos) {
        this.tip = tip;
        this.iznos = iznos;
    }

    public Clanarina getTip() {
        return tip;
    }

    

    public double getIznos() {
        return iznos;
    }

    public void setIznos(double iznos) {
        this.iznos = iznos;
    }


    public String toCSV() {
        return tip.name() + ";" + iznos;
    }

    public static Kazna fromCSV(String line) {
        String[] parts = line.split(";");
        String opis = parts[0];
        double iznos = Double.parseDouble(parts[1]);
        return new Kazna(opis, iznos);
    }

    @Override
    public String toString() {
        return "Članarina{" +
                "tip='" + tip.name() + '\'' +
                ", iznos=" + iznos +
                '}';
    }
}
