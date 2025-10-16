package manager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import modeli.PosebnaKategorija;

public class PosebnaKategorijaMetode {

    private String fajl = "podaci/posebnekategorije.csv";

    public List<PosebnaKategorija> ucitajKategorije() {
        List<PosebnaKategorija> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajl), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(";");
                if (tokens.length >= 1) {
                    String naziv = tokens[0].trim();
                    double popust = tokens.length > 1 ? Double.parseDouble(tokens[1]) : 0;
                    lista.add(new PosebnaKategorija(naziv, popust));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private void sacuvajKategorije(List<PosebnaKategorija> lista) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fajl, false), StandardCharsets.UTF_8))) {
            for (PosebnaKategorija pk : lista) {
                bw.write(pk.getKategorija() + ";" + pk.getPopust());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dodajKategoriju(String naziv, double popust) {
        PosebnaKategorija nova = new PosebnaKategorija(naziv, popust);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fajl, true))) {
            bw.write(naziv + ";" + popust);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean izmeniPopust(String naziv, double noviPopust) {
        List<PosebnaKategorija> lista = ucitajKategorije();
        boolean nadjeno = false;

        for (PosebnaKategorija pk : lista) {
            if (pk.getKategorija().equalsIgnoreCase(naziv)) {
                pk.setPopust(noviPopust);
                nadjeno = true;
                break;
            }
        }

        if (nadjeno) {
            sacuvajKategorije(lista);
        }
        return nadjeno;
    }
}
