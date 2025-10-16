package manager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import modeli.Zanr;

public class ZanrMetode {

    private static String fajl = "podaci/zanrovi.csv";

    public static void setFilePath(String newPath) {
        fajl = newPath;
    }

    public static List<Zanr> ucitajZanrove() {
        List<Zanr> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fajl), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lista.add(new Zanr(line.trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static void dodajZanr(Zanr zanr) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fajl, true), StandardCharsets.UTF_8)) {
            writer.write(zanr.getNaziv());
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
