package modeli;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cenovnik {
    private int id;
    private LocalDate datumOd;
    private LocalDate datumDo;

    private List<ClanarinaStavka> clanarine = new ArrayList<>();
    private List<Kazna> kazne = new ArrayList<>();
    private List<Usluga> usluge = new ArrayList<>();

    public Cenovnik(int id, LocalDate datumOd, LocalDate datumDo) {
        this.id = id;
        this.datumOd = datumOd;
        this.datumDo = datumDo;
    }

    public void dodajClanarinu(ClanarinaStavka c) {
        clanarine.add(c);
    }

    public void dodajKaznu(Kazna k) {
        kazne.add(k);
    }

    public void dodajUslugu(Usluga u) {
        usluge.add(u);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDatumOd() {
        return datumOd;
    }

    public void setDatumOd(LocalDate datumOd) {
        this.datumOd = datumOd;
    }

    public LocalDate getDatumDo() {
        return datumDo;
    }

    public void setDatumDo(LocalDate datumDo) {
        this.datumDo = datumDo;
    }

    public List<ClanarinaStavka> getClanarine() {
        return clanarine;
    }

    public void setClanarine(List<ClanarinaStavka> clanarine) {
        this.clanarine = clanarine;
    }

    public List<Kazna> getKazne() {
        return kazne;
    }

    public void setKazne(List<Kazna> kazne) {
        this.kazne = kazne;
    }

    public List<Usluga> getUsluge() {
        return usluge;
    }

    public void setUsluge(List<Usluga> usluge) {
        this.usluge = usluge;
    }


    public String toCSV() {

        return id + ";" +
               datumOd + ";" +
               datumDo + ";" +
               clanarine.stream().map(ClanarinaStavka::toCSV).collect(Collectors.joining("|")) + ";" +
               kazne.stream().map(Kazna::toCSV).collect(Collectors.joining("|")) + ";" +
               usluge.stream().map(Usluga::toCSV).collect(Collectors.joining("|"));
    }

    @Override
    public String toString() {
        return "Cenovnik [ID=" + id + ", Datum od=" + datumOd + ", Datum do=" + datumDo + "]";
    }
}
