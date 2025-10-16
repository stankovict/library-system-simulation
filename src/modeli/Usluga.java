package modeli;

public class Usluga {
    private String naziv;
    private double cena;

    public Usluga(String naziv, double cena) {
        this.naziv = naziv;
        this.cena = cena;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public double getCena() {
        return cena;
    }

    public void setCena(double cena) {
        this.cena = cena;
    }

    public String toCSV() {
        return naziv + "," + cena;
    }

    public static Usluga fromCSV(String line) {
        String[] parts = line.split(";");
        String naziv = parts[0];
        double cena = Double.parseDouble(parts[1]);
        return new Usluga(naziv, cena);
    }

    @Override
    public String toString() {
        return "Usluga{" +
                "naziv='" + naziv + '\'' +
                ", cena=" + cena +
                '}';
    }
}
