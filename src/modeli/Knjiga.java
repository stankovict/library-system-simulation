package modeli;

import enumi.StatusKnjige;

public class Knjiga {

    private String naslov;
    private String autor;
    private int id;
    private String zanr;
    private StatusKnjige status;

    public Knjiga(String naslov, String autor, String zanr, int id, StatusKnjige status) {
        this.naslov = naslov;
        this.autor = autor;
        this.zanr = zanr;
        this.id = id;
        this.status = status;
    }

    public String getNaslov() {
        return naslov;
    }

    public String getAutor() {
        return autor;
    }

    public int getId() {
        return id;
    }

    public String getZanr() {
        return zanr;
    }

    public StatusKnjige getStatus() {
        return status;
    }

    public void setStatus(StatusKnjige status) {
        this.status = status;
    }

    public String toCSV() {
        return String.join(";",
                naslov,
                autor,
                zanr,
                String.valueOf(id),
                status.name()
        );
    }
}
