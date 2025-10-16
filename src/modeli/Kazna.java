package modeli;

public class Kazna {
    private String opis;
    private double iznos;

    public Kazna(String opis, double iznos) {
        this.opis = opis;
        this.iznos = iznos;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public double getIznos() {
        return iznos;
    }

    public void setIznos(double iznos) {
        this.iznos = iznos;
    }


    public String toCSV() {
        return opis + ";" + iznos;
    }


    public static Kazna fromCSV(String line) {
        String[] parts = line.split(";");
        String opis = parts[0];
        double iznos = Double.parseDouble(parts[1]);
        return new Kazna(opis, iznos);
    }

    @Override
    public String toString() {
        return "Kazna{" +
                "opis='" + opis + '\'' +
                ", iznos=" + iznos +
                '}';
    }
}
