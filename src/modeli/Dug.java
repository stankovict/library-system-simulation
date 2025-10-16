package modeli;

public class Dug {
    private String username;
    private double iznos;

    public Dug(String username, double iznos) {
        this.username = username;
        this.iznos = iznos;
    }

    public String getUsername() { return username; }
    public double getIznos() { return iznos; }
    public void setIznos(double iznos) { this.iznos = iznos; }
}
