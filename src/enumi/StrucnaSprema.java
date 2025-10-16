package enumi;

public enum StrucnaSprema {
    III(9.85),
    IV(11.15),
    V(13.65),
    VI(14.88),
    VII(17.32);

    private final double koeficijent;

    StrucnaSprema(double koeficijent) {
        this.koeficijent = koeficijent;
    }

    public double getKoeficijent() {
        return koeficijent;
    }
}
